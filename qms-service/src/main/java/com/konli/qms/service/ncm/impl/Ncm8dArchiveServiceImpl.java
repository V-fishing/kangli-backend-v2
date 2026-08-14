package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.audit.AuditLogRecorder;
import com.konli.qms.domain.ncm.entity.Qms8dArchivedReport;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.entity.Qms8dStageDetail;
import com.konli.qms.domain.ncm.mapper.Qms8dArchivedReportMapper;
import com.konli.qms.domain.ncm.mapper.Qms8dReportMapper;
import com.konli.qms.domain.ncm.mapper.Qms8dStageDetailMapper;
import com.konli.qms.service.ncm.Ncm8dArchiveService;
import com.konli.qms.service.support.CjkFontUtil;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 8D 整改归档实现。参考 {@link com.konli.qms.service.sqm.impl.SqmAuditReportArchiveServiceImpl} 的
 * generatePdf + buildReportHtml 写法。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Ncm8dArchiveServiceImpl implements Ncm8dArchiveService {

    private static final String[] STAGES = {"D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8"};

    private final Qms8dArchivedReportMapper archiveMapper;
    private final Qms8dStageDetailMapper stageDetailMapper;
    private final Qms8dReportMapper qms8dReportMapper;
    private final AuditLogRecorder auditLogRecorder;

    @Override
    @Transactional
    public Qms8dArchivedReport archive(Qms8dReport report) {
        if (report == null || report.getId() == null) {
            return null;
        }
        // 取最新归档记录,按现有归档状态决定"覆盖"还是"新增版本"
        Qms8dArchivedReport existing = getByReportId(report.getId());
        List<Qms8dStageDetail> stages = stageDetailMapper.selectList(
                new LambdaQueryWrapper<Qms8dStageDetail>()
                        .eq(Qms8dStageDetail::getD8Id, report.getId())
                        .orderByAsc(Qms8dStageDetail::getStageCode));

        // 归档哈希源:核心字段 + 各阶段完成状态
        StringBuilder hashSrc = new StringBuilder()
                .append(str(report.getD8No())).append('|')
                .append(str(report.getIssue())).append('|')
                .append(str(report.getSeverity())).append('|')
                .append(str(report.getSource())).append('|')
                .append(str(report.getSourceRefId())).append('|')
                .append(str(report.getFlowType())).append('|')
                .append(str(report.getStatus()));
        for (Qms8dStageDetail s : stages) {
            hashSrc.append('|').append(str(s.getStageCode())).append(':')
                   .append(str(s.getApprovalStatus())).append(':')
                   .append(str(s.getOwner()));
        }
        String reportHash = sha256(hashSrc.toString());

        LocalDate now = LocalDate.now();
        Qms8dArchivedReport archive;
        boolean isNew;
        if (existing != null && "已归档".equals(existing.getStatus())) {
            // 已归档 + 重复归档(正常重复归档 / 历史 backfill 补归档) → 幂等覆盖原记录
            archive = existing;
            isNew = false;
        } else {
            // 首次归档 或 重开后重新闭环(旧档已作废) → 新增一条归档版本,
            // 预置 UUID 主键,保证 PDF 文件名唯一、磁盘文件互不覆盖
            archive = new Qms8dArchivedReport();
            archive.setId(UUID.randomUUID().toString());
            isNew = true;
        }
        archive.setOrgId(report.getOrgId());
        archive.setReportId(report.getId());
        archive.setD8No(report.getD8No());
        // archive_no 有唯一约束:首次归档用 "8D-<d8No>";重开重新闭环(新增版本)加 -R<序号> 后缀避免冲突
        if (existing == null) {
            archive.setArchiveNo("8D-" + report.getD8No());
        } else {
            long existCnt = archiveMapper.selectCount(
                    new LambdaQueryWrapper<Qms8dArchivedReport>()
                            .eq(Qms8dArchivedReport::getReportId, report.getId()));
            archive.setArchiveNo("8D-" + report.getD8No() + "-R" + (existCnt + 1));
        }
        archive.setArchiveDate(now);
        archive.setStatus("已归档");
        archive.setReportHash(reportHash);
        archive.setRetentionUntil(now.plusYears(15));

        // 生成 PDF(openhtmltopdf),失败抛 BusinessException
        String pdfPath = generatePdf(report, stages, archive);
        archive.setPdfRef(pdfPath);

        if (isNew) {
            archiveMapper.insert(archive);
        } else {
            archiveMapper.updateById(archive);
        }
        auditLogRecorder.record("NCM", "ARCHIVE", "Ncm8dArchiveServiceImpl.archive",
                report.getId(), "8D 报告归档: " + report.getD8No(), "SUCCESS", null, 0L);
        return archive;
    }

    @Override
    public Qms8dArchivedReport getByReportId(String reportId) {
        if (reportId == null) return null;
        return archiveMapper.selectOne(
                new LambdaQueryWrapper<Qms8dArchivedReport>()
                        .eq(Qms8dArchivedReport::getReportId, reportId)
                        .orderByDesc(Qms8dArchivedReport::getArchiveDate)
                        .last("LIMIT 1"));
    }

    @Override
    public void invalidate(String reportId, String reason) {
        if (reportId == null) return;
        Qms8dArchivedReport existing = getByReportId(reportId);
        // 幂等:仅当存在"已归档"记录时才作废;已作废/无记录直接返回
        if (existing == null || !"已归档".equals(existing.getStatus())) return;
        existing.setStatus("已作废");
        archiveMapper.updateById(existing);
        auditLogRecorder.record("NCM", "ARCHIVE_INVALIDATE", "Ncm8dArchiveServiceImpl.invalidate",
                reportId, "8D 归档作废(重开): " + reason, "SUCCESS", null, 0L);
        log.info("8D 归档软作废, reportId={}, archiveId={}, reason={}", reportId, existing.getId(), reason);
    }

    @Override
    public int backfill() {
        List<Qms8dReport> reports = qms8dReportMapper.selectList(
                new LambdaQueryWrapper<Qms8dReport>().eq(Qms8dReport::getStatus, "已闭环"));
        int ok = 0;
        for (Qms8dReport r : reports) {
            try {
                archive(r);
                ok++;
            } catch (Exception e) {
                log.warn("8D 补归档失败, reportId={}: {}", r.getId(), e.getMessage(), e);
            }
        }
        log.info("8D 历史数据补归档完成, 扫描 {} 条已闭环报告, 成功 {} 条", reports.size(), ok);
        return ok;
    }

    /** 生成 8D 归档报告 PDF,返回本地路径;失败抛 BusinessException。 */
    private String generatePdf(Qms8dReport report, List<Qms8dStageDetail> stages, Qms8dArchivedReport archive) {
        try {
            String html = buildReportHtml(report, stages, archive);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            CjkFontUtil.register(builder);
            builder.withHtmlContent(html, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            builder.toStream(os);
            builder.run();
            byte[] pdfBytes = os.toByteArray();
            // 文件名带归档记录 id,保证不同版本(重开重归档)的 PDF 磁盘文件互不覆盖
            String idSuffix = archive.getId() != null ? archive.getId() : (report.getD8No() != null ? report.getD8No() : report.getId());
            String fileName = "8d-" + (report.getD8No() != null ? report.getD8No() : report.getId()) + "-" + idSuffix + ".pdf";
            Path path = Paths.get("logs", "reports", fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, pdfBytes);
            return path.toString();
        } catch (Exception e) {
            log.warn("8D 归档 PDF 生成失败, reportId={}: {}", report.getId(), e.getMessage(), e);
            throw new BusinessException(500, "8D 归档 PDF 生成失败: " + e.getMessage());
        }
    }

    /** 构建 8D 归档报告 HTML(内联样式,不依赖外部 CSS;openhtmltopdf 渲染)。 */
    private String buildReportHtml(Qms8dReport report, List<Qms8dStageDetail> stages, Qms8dArchivedReport archive) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><style>")
          .append("body{font-family:'SimHei',sans-serif;font-size:12px;color:#000;margin:24px;}")
          .append("h1{text-align:center;font-size:20px;margin:8px 0 16px;}")
          .append("h2{font-size:14px;margin:12px 0 4px;border-left:4px solid #333;padding-left:6px;}")
          .append("table{border-collapse:collapse;width:100%;margin:4px 0;}")
          .append("th,td{border:1px solid #333;padding:4px 6px;text-align:left;font-size:11px;vertical-align:top;}")
          .append("th{background:#eee;}")
          .append(".info{margin:2px 0;}")
          .append(".label{display:inline-block;width:110px;font-weight:bold;}")
          .append("</style></head><body>");
        // 标题
        sb.append("<h1>8D 整改报告归档</h1>");
        // 归档元信息
        sb.append("<div class=\"info\"><span class=\"label\">归档编号:</span>").append(str(archive.getArchiveNo())).append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">归档日期:</span>").append(archive.getArchiveDate() != null ? archive.getArchiveDate().format(df) : "").append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">留存至:</span>").append(archive.getRetentionUntil() != null ? archive.getRetentionUntil().format(df) : "").append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">报告哈希:</span>").append(str(archive.getReportHash())).append("</div>");
        // 8D 基本信息
        sb.append("<h2>8D 报告基本信息</h2>");
        sb.append("<table>");
        appendRow(sb, "8D 单号", str(report.getD8No()), "问题概述", str(report.getIssue()));
        appendRow(sb, "严重度", str(report.getSeverity()), "来源", str(report.getSource()));
        appendRow(sb, "来源单号", str(report.getSourceRefId()), "流程类型", str(report.getFlowType()));
        appendRow(sb, "团队", str(report.getTeam()), "状态", str(report.getStatus()));
        sb.append("</table>");
        // 各阶段内容
        sb.append("<h2>8D 各阶段(D1-D8)</h2>");
        sb.append("<table><tr><th>阶段</th><th>状态</th><th>责任人</th><th>内容摘要</th></tr>");
        for (String code : STAGES) {
            Qms8dStageDetail s = stages.stream().filter(x -> code.equals(x.getStageCode())).findFirst().orElse(null);
            String content = s == null ? "（未填写）" : (s.getContent() != null ? s.getContent() : "（无内容）");
            String approval = s == null ? "-" : str(s.getApprovalStatus());
            String owner = s == null ? "-" : str(s.getOwner());
            sb.append("<tr>")
              .append("<td>").append(code).append("</td>")
              .append("<td>").append(escape(approval)).append("</td>")
              .append("<td>").append(escape(owner)).append("</td>")
              .append("<td>").append(escape(content)).append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, String l1, String v1, String l2, String v2) {
        sb.append("<tr><td><b>").append(l1).append("</b></td><td>").append(escape(v1)).append("</td>")
          .append("<td><b>").append(l2).append("</b></td><td>").append(escape(v2)).append("</td></tr>");
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder h = new StringBuilder();
            for (byte x : b) {
                h.append(String.format("%02x", x));
            }
            return h.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
