package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmAuditNc;
import com.konli.qms.domain.sqm.entity.SqmAuditRecord;
import com.konli.qms.domain.sqm.entity.SqmAuditReportArchive;
import com.konli.qms.domain.sqm.mapper.SqmAuditNcMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditRecordMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditReportArchiveMapper;
import com.konli.qms.service.sqm.SqmAuditReportArchiveService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 审核报告归档实现。参考 {@link com.konli.qms.service.fia.impl.FiaTaskServiceImpl} 的
 * {@code generatePdf + buildReportHtml} 写法。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SqmAuditReportArchiveServiceImpl implements SqmAuditReportArchiveService {

    private final SqmAuditReportArchiveMapper sqmAuditReportArchiveMapper;
    private final SqmAuditRecordMapper sqmAuditRecordMapper;
    private final SqmAuditNcMapper sqmAuditNcMapper;

    @Override
    public List<SqmAuditReportArchive> list(String recordId) {
        LambdaQueryWrapper<SqmAuditReportArchive> qw = new LambdaQueryWrapper<>();
        if (recordId != null && !recordId.isBlank()) {
            qw.eq(SqmAuditReportArchive::getRecordId, recordId);
        }
        qw.orderByDesc(SqmAuditReportArchive::getArchiveDate);
        return sqmAuditReportArchiveMapper.selectList(qw);
    }

    @Override
    public SqmAuditReportArchive get(String id) {
        return sqmAuditReportArchiveMapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmAuditReportArchive create(SqmAuditReportArchive archive) {
        if (archive.getArchiveNo() == null || archive.getArchiveNo().isBlank()) {
            archive.setArchiveNo("SA-" + System.currentTimeMillis());
        }
        sqmAuditReportArchiveMapper.insert(archive);
        return archive;
    }

    /**
     * 生成审核报告 PDF 并归档。失败抛 BusinessException。
     */
    @Override
    @Transactional
    public SqmAuditReportArchive generatePdf(String recordId) {
        SqmAuditRecord record = sqmAuditRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "审核记录不存在");
        }
        List<SqmAuditNc> ncs = sqmAuditNcMapper.selectList(
                new LambdaQueryWrapper<SqmAuditNc>()
                        .eq(SqmAuditNc::getRecordId, recordId)
                        .orderByAsc(SqmAuditNc::getNcNo));

        // 归档哈希源:recordNo + 审核结论 + NC 列表摘要
        StringBuilder hashSrc = new StringBuilder()
                .append(str(record.getRecordNo())).append('|')
                .append(str(record.getResult())).append('|')
                .append(str(record.getScore())).append('|')
                .append(str(record.getNcCount())).append('|')
                .append(str(record.getConclusion()));
        for (SqmAuditNc nc : ncs) {
            hashSrc.append('|').append(str(nc.getNcNo())).append(':')
                   .append(str(nc.getClause())).append(':')
                   .append(str(nc.getLevel())).append(':')
                   .append(str(nc.getStatus()));
        }
        String reportHash = sha256(hashSrc.toString());

        LocalDateTime now = LocalDateTime.now();
        SqmAuditReportArchive archive = new SqmAuditReportArchive();
        archive.setOrgId(record.getOrgId());
        archive.setArchiveNo("SA-" + System.currentTimeMillis());
        archive.setRecordId(recordId);
        archive.setPlanId(record.getPlanId());
        archive.setSupplierId(record.getSupplierId());
        archive.setReportHash(reportHash);
        archive.setAssembledAt(now);
        archive.setArchiveDate(now);
        archive.setRetentionUntil(LocalDate.now().plusYears(15));

        // 生成 PDF(openhtmltopdf),失败抛 BusinessException
        String pdfPath;
        try {
            String html = buildReportHtml(record, ncs, archive);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // 注册中文字体,避免归档报告中文显示为方块/乱码
            CjkFontUtil.register(builder);
            builder.withHtmlContent(html, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            builder.toStream(os);
            builder.run();
            byte[] pdfBytes = os.toByteArray();
            String fileName = "audit-" + (record.getRecordNo() != null ? record.getRecordNo() : recordId) + ".pdf";
            Path path = Paths.get("logs", "reports", fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, pdfBytes);
            pdfPath = path.toString();
        } catch (Exception e) {
            log.warn("SQM 审核报告归档 PDF 生成失败, recordId={}: {}", recordId, e.getMessage(), e);
            throw new BusinessException(500, "审核报告归档 PDF 生成失败: " + e.getMessage());
        }
        archive.setReportFilePath(pdfPath);
        sqmAuditReportArchiveMapper.insert(archive);

        // 回填 sqm_audit_record.archive_id(便于反查归档记录)
        SqmAuditRecord upd = new SqmAuditRecord();
        upd.setId(recordId);
        upd.setArchiveId(archive.getId());
        sqmAuditRecordMapper.updateById(upd);

        return archive;
    }

    /** 构建归档报告 HTML(内联样式,不依赖外部 CSS;openhtmltopdf 渲染)。 */
    private String buildReportHtml(SqmAuditRecord record, List<SqmAuditNc> ncs, SqmAuditReportArchive archive) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><style>")
          .append("body{font-family:'SimHei',sans-serif;font-size:12px;color:#000;margin:24px;}")
          .append("h1{text-align:center;font-size:20px;margin:8px 0 16px;}")
          .append("h2{font-size:14px;margin:12px 0 4px;border-left:4px solid #333;padding-left:6px;}")
          .append("table{border-collapse:collapse;width:100%;margin:4px 0;}")
          .append("th,td{border:1px solid #333;padding:4px 6px;text-align:left;font-size:11px;}")
          .append("th{background:#eee;}")
          .append(".info{margin:2px 0;}")
          .append(".label{display:inline-block;width:110px;font-weight:bold;}")
          .append("</style></head><body>");
        // 标题
        sb.append("<h1>供应商审核报告归档</h1>");
        // 归档元信息
        sb.append("<div class=\"info\"><span class=\"label\">归档编号:</span>").append(str(archive.getArchiveNo())).append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">归档时间:</span>").append(archive.getArchiveDate() != null ? archive.getArchiveDate().format(dtf) : "").append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">留存至:</span>").append(archive.getRetentionUntil() != null ? archive.getRetentionUntil().format(df) : "").append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">报告哈希:</span>").append(str(archive.getReportHash())).append("</div>");
        // 审核记录信息
        sb.append("<h2>审核记录信息</h2>");
        sb.append("<table>");
        appendRow(sb, "记录编号", str(record.getRecordNo()), "审核类型", str(record.getAuditType()));
        appendRow(sb, "审核日期", str(record.getAuditDate()), "审核组长", str(record.getAuditLead()));
        appendRow(sb, "审核组", str(record.getAuditorTeam()), "审核结果", str(record.getResult()));
        appendRow(sb, "审核得分", str(record.getScore()), "NC 数量", str(record.getNcCount()));
        appendRow(sb, "审核结论", str(record.getConclusion()), "状态", str(record.getStatus()));
        sb.append("</table>");
        // NC 列表
        sb.append("<h2>不符合项列表</h2>");
        if (ncs == null || ncs.isEmpty()) {
            sb.append("<p>无不符合项</p>");
        } else {
            sb.append("<table><tr><th>序号</th><th>NC 编号</th><th>条款</th><th>描述</th><th>级别</th><th>状态</th><th>责任人</th><th>整改措施</th><th>验证结论</th></tr>");
            int seq = 1;
            for (SqmAuditNc nc : ncs) {
                sb.append("<tr>")
                  .append("<td>").append(seq++).append("</td>")
                  .append("<td>").append(str(nc.getNcNo())).append("</td>")
                  .append("<td>").append(str(nc.getClause())).append("</td>")
                  .append("<td>").append(str(nc.getDescription())).append("</td>")
                  .append("<td>").append(str(nc.getLevel())).append("</td>")
                  .append("<td>").append(str(nc.getStatus())).append("</td>")
                  .append("<td>").append(str(nc.getResponsible())).append("</td>")
                  .append("<td>").append(str(nc.getRectifyMeasure())).append("</td>")
                  .append("<td>").append(str(nc.getVerifyResult())).append("</td>")
                  .append("</tr>");
            }
            sb.append("</table>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, String l1, String v1, String l2, String v2) {
        sb.append("<tr><td><b>").append(l1).append("</b></td><td>").append(v1).append("</td>")
          .append("<td><b>").append(l2).append("</b></td><td>").append(v2).append("</td></tr>");
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
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
