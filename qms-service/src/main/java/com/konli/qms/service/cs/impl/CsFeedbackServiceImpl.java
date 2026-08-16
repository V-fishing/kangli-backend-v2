package com.konli.qms.service.cs.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.cs.entity.CsFeedback;
import com.konli.qms.domain.cs.mapper.CsFeedbackMapper;
import com.konli.qms.domain.ncm.entity.NcmCorrectiveAction;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.service.cs.CsFeedbackService;
import com.konli.qms.service.cs.dto.TriggerNcmRequest;
import com.konli.qms.service.ncm.Ncm8dService;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.ncm.NcmCorrectiveActionService;
import com.konli.qms.service.notify.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsFeedbackServiceImpl implements CsFeedbackService {

    private final CsFeedbackMapper mapper;
    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;
    private final Ncm8dService ncm8dService;
    private final NcmCapaService ncmCapaService;
    private final NcmCorrectiveActionService ncmCaService;

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
        } catch (Exception e) {
            return null;
        }
    }

    private String defaultOrgId() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM ops.sys_org WHERE is_deleted = false ORDER BY created_at LIMIT 1", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String curUser() {
        try {
            return CompanyContext.get().userId();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public PageResult<CsFeedback> page(String keyword, String fbType, String status, int page, int size) {
        LambdaQueryWrapper<CsFeedback> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(CsFeedback::getCustomerName, keyword)
                    .or().like(CsFeedback::getContent, keyword)
                    .or().like(CsFeedback::getRelatedWoNo, keyword));
        }
        if (fbType != null && !fbType.isBlank()) w.eq(CsFeedback::getFbType, fbType);
        if (status != null && !status.isBlank()) w.eq(CsFeedback::getStatus, status);
        w.orderByDesc(CsFeedback::getCreatedAt);
        IPage<CsFeedback> p = mapper.selectPage(new Page<>(page, size), w);
        return new PageResult<CsFeedback>(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public CsFeedback get(String id) {
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public CsFeedback create(CsFeedback fb) {
        if (fb.getOrgId() == null) {
            String o = curOrg();
            fb.setOrgId(o != null ? o : defaultOrgId());
        }
        if (fb.getStatus() == null || fb.getStatus().isBlank()) fb.setStatus("OPEN");
        if (fb.getFbType() == null || fb.getFbType().isBlank()) fb.setFbType("COMPLAINT");
        fb.setCreatedBy(curUser());
        fb.setUpdatedBy(curUser());
        mapper.insert(fb);
        try {
            notificationService.notify("cs", "cs_fb_created", "客户反馈登记",
                    "客户 " + fb.getCustomerName() + " 登记了新反馈(" + fb.getFbType()
                            + "),请及时跟进处理。", "cs_feedback", fb.getId(), "/cs/feedback");
        } catch (Exception e) {
            log.warn("[CS] 客户反馈通知发送失败: {}", e.getMessage());
        }
        return fb;
    }

    @Override
    @Transactional
    public CsFeedback update(CsFeedback fb) {
        CsFeedback exist = mapper.selectById(fb.getId());
        if (exist == null) throw new com.konli.qms.common.exception.BusinessException("反馈不存在");
        exist.setCustomerName(fb.getCustomerName());
        exist.setCustomerContact(fb.getCustomerContact());
        exist.setFbType(fb.getFbType());
        exist.setContent(fb.getContent());
        exist.setRelatedWoNo(fb.getRelatedWoNo());
        exist.setSatisfaction(fb.getSatisfaction());
        exist.setCause(fb.getCause());
        exist.setRelatedNcmId(fb.getRelatedNcmId());
        exist.setUpdatedBy(curUser());
        mapper.updateById(exist);
        return exist;
    }

    @Override
    @Transactional
    public void delete(String id) {
        mapper.deleteById(id);
    }

    /** 低分自动流转阈值: 满意度评分 <= 该值视为低分, 自动流转至质量负责人(sqe)。 */
    private static final int LOW_SCORE_THRESHOLD = 2;

    @Override
    @Transactional
    public void handle(String id, String handleDetail, String ownerName) {
        CsFeedback f = mapper.selectById(id);
        if (f == null) throw new com.konli.qms.common.exception.BusinessException("反馈不存在");
        f.setStatus("DONE");
        f.setHandleDetail(handleDetail);
        // 低满意度自动流转负责人(需求 2.4.2.3): 未手动指定负责人且评分 <= 阈值时, 流转至质量改进并通知 sqe
        if ((ownerName == null || ownerName.isBlank())
                && f.getSatisfaction() != null && f.getSatisfaction() <= LOW_SCORE_THRESHOLD) {
            f.setOwnerName("质量改进(低分自动流转)");
            try {
                notificationService.notify("cs", "cs_fb_lowscore", "低分反馈自动流转",
                        "客户 " + f.getCustomerName() + " 的反馈评分仅 " + f.getSatisfaction()
                                + "★,已自动流转至质量负责人跟进处理。", "cs_feedback", f.getId(), "/cs/feedback");
            } catch (Exception e) {
                log.warn("[CS] 低分反馈流转通知发送失败: {}", e.getMessage());
            }
        } else {
            f.setOwnerName(ownerName);
        }
        f.setHandleAt(LocalDateTime.now());
        f.setUpdatedBy(curUser());
        mapper.updateById(f);
    }

    @Override
    @Transactional
    public void markHandling(String id, String ownerName) {
        CsFeedback f = mapper.selectById(id);
        if (f == null) throw new com.konli.qms.common.exception.BusinessException("反馈不存在");
        if (!"OPEN".equals(f.getStatus())) {
            throw new com.konli.qms.common.exception.BusinessException("仅待处理反馈可标记为处理中");
        }
        f.setStatus("HANDLING");
        if (ownerName != null && !ownerName.isBlank()) f.setOwnerName(ownerName);
        f.setUpdatedBy(curUser());
        mapper.updateById(f);
    }

    @Override
    @Transactional
    public void linkNcm(String id, String ncmId) {
        CsFeedback f = mapper.selectById(id);
        if (f == null) throw new com.konli.qms.common.exception.BusinessException("反馈不存在");
        f.setRelatedNcmId(ncmId);
        f.setUpdatedBy(curUser());
        mapper.updateById(f);
        try {
            notificationService.notify("cs", "cs_fb_ncm", "反馈联动质量改进",
                    "客户 " + f.getCustomerName() + " 的反馈已联动纠正措施(" + ncmId + "),请跟进闭环。",
                    "cs_feedback", f.getId(), "/cs/feedback");
        } catch (Exception e) {
            log.warn("[CS] 反馈联动通知发送失败: {}", e.getMessage());
        }
    }

    /**
     * 从客户反馈直接触发质量改进纠正措施(需求 2.4.2.5 闭环升级)。
     * 替代原"手动填写 NCM ID"的弱联动: 实际创建 8D / CAPA / CA 记录, 并把新记录 ID 回填到
     * 反馈对应关联字段(related_8d_id / related_capa_id / related_ca_id), 同时兼容写入 related_ncm_id,
     * 实现"反馈 → 纠正措施"真正的双向追溯闭环。
     */
    @Override
    @Transactional
    public CsFeedback triggerNcm(String id, TriggerNcmRequest req) {
        CsFeedback f = mapper.selectById(id);
        if (f == null) throw new BusinessException("反馈不存在");
        if (req == null || req.getType() == null || req.getType().isBlank()) {
            throw new BusinessException("触发类型不能为空(8D/CAPA/CA)");
        }
        String issue = (req.getIssue() != null && !req.getIssue().isBlank()) ? req.getIssue() : f.getContent();
        String ownerName = req.getOwnerName();
        if (ownerName == null && req.getOwnerUserId() != null) {
            ownerName = queryUserName(req.getOwnerUserId());
        }
        String type = req.getType().toUpperCase();
        String link = "/cs/feedback";
        switch (type) {
            case "8D": {
                Qms8dReport r = new Qms8dReport();
                r.setId(java.util.UUID.randomUUID().toString());
                r.setOrgId(f.getOrgId());
                r.setSource("CS反馈");
                r.setSourceRefId(f.getId());
                r.setIssue(issue);
                r.setSeverity("一般");
                r.setFlowType("8D");
                if (req.getOwnerUserId() != null) r.setOwnerUserId(req.getOwnerUserId());
                if (ownerName != null) { r.setOwnerUserName(ownerName); r.setTeam(ownerName); }
                Qms8dReport created = ncm8dService.create(r);
                f.setRelated8dId(created.getId());
                f.setRelatedNcmId(created.getId());
                link = "/ncm/8d-reports/" + created.getId();
                break;
            }
            case "CAPA": {
                QmsCapa c = new QmsCapa();
                c.setOrgId(f.getOrgId());
                c.setSourceRefId(f.getId());
                c.setSourceType("CS_FEEDBACK");
                c.setIssue(issue);
                if (req.getOwnerUserId() != null) { c.setOwnerUserId(req.getOwnerUserId()); c.setOwner(req.getOwnerUserId()); }
                if (req.getDueDate() != null && !req.getDueDate().isBlank()) {
                    try { c.setDueDate(LocalDate.parse(req.getDueDate())); } catch (Exception ignored) {}
                }
                QmsCapa created = ncmCapaService.create(c);
                f.setRelatedCapaId(created.getId());
                f.setRelatedNcmId(created.getId());
                link = "/ncm/capas/" + created.getId();
                break;
            }
            case "CA": {
                NcmCorrectiveAction ca = new NcmCorrectiveAction();
                ca.setOrgId(f.getOrgId());
                ca.setSourceRefId(f.getId());
                ca.setSourceType("CS_FEEDBACK");
                ca.setIssue(issue);
                if (req.getOwnerUserId() != null) { ca.setOwnerUserId(req.getOwnerUserId()); ca.setOwner(req.getOwnerUserId()); }
                if (ownerName != null) ca.setOwnerName(ownerName);
                if (req.getDueDate() != null && !req.getDueDate().isBlank()) {
                    try { ca.setDueDate(LocalDate.parse(req.getDueDate())); } catch (Exception ignored) {}
                }
                NcmCorrectiveAction created = ncmCaService.create(ca);
                f.setRelatedCaId(created.getId());
                f.setRelatedNcmId(created.getId());
                link = "/ncm/corrective-actions/" + created.getId();
                break;
            }
            default:
                throw new BusinessException("不支持的触发类型: " + type + "(应为 8D/CAPA/CA)");
        }
        f.setUpdatedBy(curUser());
        mapper.updateById(f);
        try {
            String bizNo = type + " 纠正措施";
            notificationService.notify("cs", "cs_fb_ncm", "反馈触发质量改进",
                    "客户 " + f.getCustomerName() + " 的反馈已触发 " + type + " 纠正措施,请跟进闭环。",
                    "cs_feedback", f.getId(), link);
        } catch (Exception e) {
            log.warn("[CS] 反馈触发质量改进通知发送失败: {}", e.getMessage());
        }
        return f;
    }

    /** 解析用户姓名(批量/单查封装, 兜底返回原 id)。 */
    private String queryUserName(String userId) {
        if (userId == null || userId.isBlank()) return null;
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT real_name FROM ops.sys_user WHERE id = ?", String.class, userId);
        } catch (Exception e) {
            return userId;
        }
    }

    @Override
    public void exportCsv(HttpServletResponse response, String keyword, String fbType, String status) {
        String org = curOrg();
        StringBuilder sql = new StringBuilder(
                "SELECT customer_name, customer_contact, fb_type, content, related_wo_no, status, " +
                "handle_detail, owner_name, satisfaction, cause, created_at FROM ops.cs_feedback WHERE is_deleted = false");
        if (org != null) sql.append(" AND org_id = '").append(org.replace("'", "''")).append("'");
        if (keyword != null && !keyword.isBlank()) sql.append(" AND (customer_name ILIKE '%").append(keyword.replace("'", "''")).append("%' OR content ILIKE '%").append(keyword.replace("'", "''")).append("%')");
        if (fbType != null && !fbType.isBlank()) sql.append(" AND fb_type = '").append(fbType.replace("'", "''")).append("'");
        if (status != null && !status.isBlank()) sql.append(" AND status = '").append(status.replace("'", "''")).append("'");
        sql.append(" ORDER BY created_at DESC");
        writeCsv(response, "客户反馈",
                new String[]{"客户名称", "联系方式", "反馈类型", "反馈内容", "关联工单", "状态", "处理详情", "责任人", "满意度", "低分诱因", "登记时间"},
                jdbcTemplate.queryForList(sql.toString()),
                r -> new String[]{str(r.get("customer_name")), str(r.get("customer_contact")), str(r.get("fb_type")),
                        str(r.get("content")), str(r.get("related_wo_no")), str(r.get("status")), str(r.get("handle_detail")),
                        str(r.get("owner_name")), str(r.get("satisfaction")), str(r.get("cause")), str(r.get("created_at"))});
    }

    private void writeCsv(HttpServletResponse response, String fileName, String[] headers,
                          java.util.List<Map<String, Object>> rows,
                          java.util.function.Function<Map<String, Object>, String[]> rowFn) {
        try {
            response.setContentType("text/csv;charset=GBK");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + new String((fileName + ".csv").getBytes("GBK"), "ISO-8859-1") + "\"");
            try (java.io.OutputStream os = response.getOutputStream()) {
                os.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
                StringBuilder sb = new StringBuilder();
                sb.append(String.join(",", headers)).append("\r\n");
                for (Map<String, Object> r : rows) {
                    String[] cells = rowFn.apply(r);
                    for (int i = 0; i < cells.length; i++) {
                        if (i > 0) sb.append(",");
                        sb.append(escapeCsv(cells[i]));
                    }
                    sb.append("\r\n");
                }
                os.write(sb.toString().getBytes("UTF-8"));
                os.flush();
            }
        } catch (java.io.IOException e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    private String str(Object o) { return o == null ? "" : String.valueOf(o); }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\r") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
