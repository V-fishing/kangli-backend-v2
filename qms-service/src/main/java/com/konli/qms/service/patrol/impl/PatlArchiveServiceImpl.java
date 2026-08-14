package com.konli.qms.service.patrol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.patrol.entity.PatlArchivedReport;
import com.konli.qms.domain.patrol.entity.PatlRecord;
import com.konli.qms.domain.patrol.entity.PatlTask;
import com.konli.qms.domain.patrol.mapper.PatlArchivedReportMapper;
import com.konli.qms.domain.patrol.mapper.PatlRecordMapper;
import com.konli.qms.domain.patrol.mapper.PatlRouteMapper;
import com.konli.qms.domain.patrol.mapper.PatlTaskMapper;
import com.konli.qms.service.patrol.PatlArchiveService;
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

/**
 * 巡检任务归档实现。参考 {@link com.konli.qms.service.ncm.impl.Ncm8dArchiveServiceImpl}。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatlArchiveServiceImpl implements PatlArchiveService {

    private final PatlArchivedReportMapper archiveMapper;
    private final PatlTaskMapper patlTaskMapper;
    private final PatlRecordMapper patlRecordMapper;
    private final PatlRouteMapper patlRouteMapper;

    @Override
    @Transactional
    public PatlArchivedReport archive(String taskId) {
        if (taskId == null) return null;
        PatlTask task = patlTaskMapper.selectById(taskId);
        if (task == null) return null;
        // 幂等:同一 taskId 重复归档时覆盖更新
        PatlArchivedReport existing = getByTaskId(taskId);

        List<PatlRecord> records = patlRecordMapper.selectList(
                new LambdaQueryWrapper<PatlRecord>()
                        .eq(PatlRecord::getTaskId, taskId)
                        .orderByAsc(PatlRecord::getCheckTime));
        int abnormalCount = 0;
        for (PatlRecord r : records) {
            if ("异常".equals(r.getResult())) abnormalCount++;
        }

        // 归档哈希源:任务核心字段 + 各点位检查结果
        StringBuilder hashSrc = new StringBuilder()
                .append(str(task.getTaskNo())).append('|')
                .append(str(task.getRouteId())).append('|')
                .append(str(task.getShift())).append('|')
                .append(str(task.getPlanTime())).append('|')
                .append(str(task.getFinishTime())).append('|')
                .append(str(task.getInspectorId())).append('|')
                .append(str(task.getTotalPoints())).append('|')
                .append(str(task.getDonePoints())).append('|')
                .append(abnormalCount);
        for (PatlRecord r : records) {
            hashSrc.append('|').append(str(r.getCheckpointName())).append(':')
                   .append(str(r.getResult())).append(':')
                   .append(str(r.getOperatorId()));
        }
        String reportHash = sha256(hashSrc.toString());

        LocalDate now = LocalDate.now();
        PatlArchivedReport archive = existing != null ? existing : new PatlArchivedReport();
        archive.setOrgId(task.getOrgId());
        archive.setTaskId(task.getId());
        archive.setTaskNo(task.getTaskNo());
        archive.setRouteId(task.getRouteId());
        archive.setArchiveNo("PT-" + task.getTaskNo());
        archive.setArchiveDate(now);
        archive.setStatus("已归档");
        archive.setReportHash(reportHash);
        archive.setRetentionUntil(now.plusYears(15));

        // 生成 PDF(openhtmltopdf),失败抛 BusinessException
        String pdfPath = generatePdf(task, records, archive);
        archive.setPdfRef(pdfPath);

        if (existing != null) {
            archiveMapper.updateById(archive);
        } else {
            archiveMapper.insert(archive);
        }
        return archive;
    }

    @Override
    public PatlArchivedReport getByTaskId(String taskId) {
        if (taskId == null) return null;
        return archiveMapper.selectOne(
                new LambdaQueryWrapper<PatlArchivedReport>()
                        .eq(PatlArchivedReport::getTaskId, taskId)
                        .last("LIMIT 1"));
    }

    @Override
    public int backfill() {
        List<PatlTask> tasks = patlTaskMapper.selectList(
                new LambdaQueryWrapper<PatlTask>().eq(PatlTask::getStatus, "已完成"));
        int ok = 0;
        for (PatlTask t : tasks) {
            try {
                archive(t.getId());
                ok++;
            } catch (Exception e) {
                log.warn("巡检补归档失败, taskId={}: {}", t.getId(), e.getMessage(), e);
            }
        }
        log.info("巡检历史数据补归档完成, 扫描 {} 条已完成任务, 成功 {} 条", tasks.size(), ok);
        return ok;
    }

    /** 生成巡检归档报告 PDF,返回本地路径;失败抛 BusinessException。 */
    private String generatePdf(PatlTask task, List<PatlRecord> records, PatlArchivedReport archive) {
        try {
            String routeName = null;
            if (task.getRouteId() != null) {
                var route = patlRouteMapper.selectById(task.getRouteId());
                routeName = route != null ? route.getRouteName() : null;
            }
            String html = buildReportHtml(task, records, archive, routeName);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            CjkFontUtil.register(builder);
            builder.withHtmlContent(html, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            builder.toStream(os);
            builder.run();
            byte[] pdfBytes = os.toByteArray();
            String fileName = "patrol-" + (task.getTaskNo() != null ? task.getTaskNo() : task.getId()) + ".pdf";
            Path path = Paths.get("logs", "reports", fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, pdfBytes);
            return path.toString();
        } catch (Exception e) {
            log.warn("巡检归档 PDF 生成失败, taskId={}: {}", task.getId(), e.getMessage(), e);
            throw new BusinessException(500, "巡检归档 PDF 生成失败: " + e.getMessage());
        }
    }

    /** 构建巡检归档报告 HTML(内联样式,不依赖外部 CSS;openhtmltopdf 渲染)。 */
    private String buildReportHtml(PatlTask task, List<PatlRecord> records, PatlArchivedReport archive, String routeName) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
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
        sb.append("<h1>巡检任务归档</h1>");
        sb.append("<div class=\"info\"><span class=\"label\">归档编号:</span>").append(str(archive.getArchiveNo())).append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">归档日期:</span>").append(archive.getArchiveDate() != null ? archive.getArchiveDate().format(df) : "").append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">留存至:</span>").append(archive.getRetentionUntil() != null ? archive.getRetentionUntil().format(df) : "").append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">报告哈希:</span>").append(str(archive.getReportHash())).append("</div>");
        sb.append("<h2>巡检任务信息</h2>");
        sb.append("<table>");
        appendRow(sb, "任务编号", str(task.getTaskNo()), "巡检路线", str(routeName));
        appendRow(sb, "班次", str(task.getShift()), "巡检员", str(task.getInspectorId()));
        appendRow(sb, "计划时间", fmt(task.getPlanTime(), dtf), "完成时间", fmt(task.getFinishTime(), dtf));
        appendRow(sb, "应检点位", str(task.getTotalPoints()), "已检点位", str(task.getDonePoints()));
        appendRow(sb, "异常点数", str(task.getAbnormalCount()), "状态", str(task.getStatus()));
        sb.append("</table>");
        sb.append("<h2>巡检点位结果</h2>");
        if (records == null || records.isEmpty()) {
            sb.append("<p>无巡检记录</p>");
        } else {
            sb.append("<table><tr><th>序号</th><th>巡检点</th><th>结果</th><th>检查时间</th><th>操作人</th><th>备注</th></tr>");
            int seq = 1;
            for (PatlRecord r : records) {
                sb.append("<tr>")
                  .append("<td>").append(seq++).append("</td>")
                  .append("<td>").append(escape(str(r.getCheckpointName()))).append("</td>")
                  .append("<td>").append(escape(str(r.getResult()))).append("</td>")
                  .append("<td>").append(fmt(r.getCheckTime(), dtf)).append("</td>")
                  .append("<td>").append(escape(str(r.getOperatorId()))).append("</td>")
                  .append("<td>").append(escape(str(r.getRemark()))).append("</td>")
                  .append("</tr>");
            }
            sb.append("</table>");
        }
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

    private static String fmt(Object o, DateTimeFormatter f) {
        if (o == null) return "";
        if (o instanceof java.time.LocalDateTime ldt) return ldt.format(f);
        return String.valueOf(o);
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
