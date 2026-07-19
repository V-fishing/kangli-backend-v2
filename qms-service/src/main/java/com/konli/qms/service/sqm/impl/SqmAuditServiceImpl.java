package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmAuditNc;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.SqmAuditRecord;
import com.konli.qms.domain.sqm.mapper.SqmAuditNcMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditPlanMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditRecordMapper;
import com.konli.qms.service.sqm.SqmAuditService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqmAuditServiceImpl implements SqmAuditService {

    private final SqmAuditPlanMapper sqmAuditPlanMapper;
    private final SqmAuditRecordMapper sqmAuditRecordMapper;
    private final SqmAuditNcMapper sqmAuditNcMapper;

    @Override
    public List<SqmAuditPlan> listPlans() {
        return sqmAuditPlanMapper.selectList(null);
    }

    @Override
    public List<SqmAuditRecord> listRecords() {
        return sqmAuditRecordMapper.selectList(null);
    }

    @Override
    public List<SqmAuditNc> listNcs() {
        return sqmAuditNcMapper.selectList(null);
    }

    @Override
    @Transactional
    public SqmAuditPlan createPlan(SqmAuditPlan plan) {
        plan.setPlanNo("AP-" + System.currentTimeMillis());
        if (plan.getStatus() == null) {
            plan.setStatus("待执行");
        }
        sqmAuditPlanMapper.insert(plan);
        return plan;
    }

    @Override
    @Transactional
    public SqmAuditRecord createRecord(SqmAuditRecord record) {
        record.setRecordNo("AR-" + System.currentTimeMillis());
        if (record.getStatus() == null) {
            record.setStatus("已完成");
        }
        if (record.getNcCount() == null) {
            record.setNcCount(0);
        }
        sqmAuditRecordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public SqmAuditNc createNc(SqmAuditNc nc) {
        nc.setNcNo("NC-" + System.currentTimeMillis());
        if (nc.getStatus() == null) {
            nc.setStatus("待整改");
        }
        sqmAuditNcMapper.insert(nc);
        return nc;
    }

    @Override
    @Transactional
    public void closeNc(String ncId, String verifyResult, String verifyComment) {
        SqmAuditNc nc = sqmAuditNcMapper.selectById(ncId);
        if (nc == null) {
            throw new BusinessException(404, "审核不符合项不存在");
        }
        SqmAuditNc upd = new SqmAuditNc();
        upd.setId(ncId);
        upd.setVerifyResult(verifyResult);
        upd.setVerifyComment(verifyComment);
        upd.setVerifyDate(LocalDateTime.now());
        upd.setStatus("已闭环");
        upd.setCloseDate(LocalDateTime.now());
        sqmAuditNcMapper.updateById(upd);
    }

    /**
     * 生成审核报告 PDF(openhtmltopdf)。
     * 查 sqm_audit_record + sqm_audit_nc(by recordId),构建 HTML 渲染为 PDF 返回 byte[]。
     * 渲染失败抛 BusinessException,由 GlobalExceptionHandler 转 R<T>。
     */
    @Override
    public byte[] generateReport(String recordId) {
        SqmAuditRecord record = sqmAuditRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "审核记录不存在");
        }
        List<SqmAuditNc> ncs = sqmAuditNcMapper.selectList(
                new LambdaQueryWrapper<SqmAuditNc>()
                        .eq(SqmAuditNc::getRecordId, recordId)
                        .orderByAsc(SqmAuditNc::getNcNo));
        try {
            String html = buildReportHtml(record, ncs);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            log.warn("SQM 审核报告 PDF 生成失败, recordId={}: {}", recordId, e.getMessage(), e);
            throw new BusinessException(500, "审核报告 PDF 生成失败: " + e.getMessage());
        }
    }

    /** 构建审核报告 HTML(内联样式,不依赖外部 CSS;openhtmltopdf 渲染)。 */
    private String buildReportHtml(SqmAuditRecord record, List<SqmAuditNc> ncs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><style>")
          .append("body{font-family:'SimSun',serif;font-size:12px;color:#000;margin:24px;}")
          .append("h1{text-align:center;font-size:20px;margin:8px 0 16px;}")
          .append("h2{font-size:14px;margin:12px 0 4px;border-left:4px solid #333;padding-left:6px;}")
          .append("table{border-collapse:collapse;width:100%;margin:4px 0;}")
          .append("th,td{border:1px solid #333;padding:4px 6px;text-align:left;font-size:11px;}")
          .append("th{background:#eee;}")
          .append(".info{margin:2px 0;}")
          .append(".label{display:inline-block;width:110px;font-weight:bold;}")
          .append("</style></head><body>");
        // 标题
        sb.append("<h1>供应商审核报告</h1>");
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
}
