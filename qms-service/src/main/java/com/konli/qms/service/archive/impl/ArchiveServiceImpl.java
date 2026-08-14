package com.konli.qms.service.archive.impl;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.archive.ArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一归档查询实现:跨表 UNION 走 JdbcTemplate(参考 SqmAnalysisServiceImpl 聚合写法)。
 *
 * <p>JdbcTemplate 不走 MyBatis-Plus 拦截器,org_id 过滤需手工拼接(对齐 DataScopeInterceptor 语义)。</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArchiveServiceImpl implements ArchiveService {

    private final JdbcTemplate jdbcTemplate;

    // ==================== 统一归档查询 ====================

    @Override
    public List<Map<String, Object>> list(String type, String keyword, Integer page, Integer size) {
        String t = type == null ? "" : type.trim().toLowerCase();
        int pageNo = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 20 : size;
        int offset = (pageNo - 1) * pageSize;

        List<String> unionParts = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String orgClause = orgFilter(); // 含前导 "AND " 或空

        // FIA 子查询:archiveNo=report_no, refId=task_id, refNo=wo_no
        if (t.isEmpty() || "fia".equals(t)) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT 'fia' AS archive_type, ")
               .append("report_no AS archive_no, ")
               .append("task_id AS ref_id, ")
               .append("wo_no AS ref_no, ")
               .append("archive_date, ")
               .append("retention_until, ")
               .append("report_hash ")
               .append("FROM ops.fia_archived_report WHERE 1=1");
            if (kw != null) {
                sql.append(" AND (report_no LIKE ? OR wo_no LIKE ?)");
                args.add("%" + kw + "%");
                args.add("%" + kw + "%");
            }
            if (!orgClause.isEmpty()) {
                sql.append(" ").append(orgClause);
            }
            unionParts.add(sql.toString());
        }

        // Audit 子查询:archiveNo=archive_no, refId=record_id, refNo=record_no(LEFT JOIN)
        if (t.isEmpty() || "audit".equals(t)) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT 'audit' AS archive_type, ")
               .append("a.archive_no AS archive_no, ")
               .append("a.record_id AS ref_id, ")
               .append("r.record_no AS ref_no, ")
               .append("a.archive_date AS archive_date, ")
               .append("a.retention_until AS retention_until, ")
               .append("a.report_hash AS report_hash ")
               .append("FROM ops.sqm_audit_report_archive a ")
               .append("LEFT JOIN ops.sqm_audit_record r ON r.id = a.record_id AND r.is_deleted = false ")
               .append("WHERE 1=1");
            if (kw != null) {
                sql.append(" AND (a.archive_no LIKE ? OR r.record_no LIKE ?)");
                args.add("%" + kw + "%");
                args.add("%" + kw + "%");
            }
            if (!orgClause.isEmpty()) {
                sql.append(" AND a.org_id = ").append(orgIdLiteral());
            }
            unionParts.add(sql.toString());
        }

        // 8D 归档子查询:archiveNo=archive_no, refId=report_id, refNo=d8_no
        if (t.isEmpty() || "8d".equals(t)) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT '8d' AS archive_type, ")
               .append("a.archive_no AS archive_no, ")
               .append("a.report_id AS ref_id, ")
               .append("a.d8_no AS ref_no, ")
               .append("a.archive_date AS archive_date, ")
               .append("a.retention_until AS retention_until, ")
               .append("a.report_hash AS report_hash ")
               .append("FROM ops.qms_8d_archived_report a ")
               .append("WHERE a.status != '已作废'");
            if (kw != null) {
                sql.append(" AND (a.archive_no LIKE ? OR a.d8_no LIKE ?)");
                args.add("%" + kw + "%");
                args.add("%" + kw + "%");
            }
            if (!orgClause.isEmpty()) {
                sql.append(" AND a.org_id = ").append(orgIdLiteral());
            }
            unionParts.add(sql.toString());
        }

        // Patrol 归档子查询:archiveNo=archive_no, refId=task_id, refNo=task_no
        if (t.isEmpty() || "patrol".equals(t)) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT 'patrol' AS archive_type, ")
               .append("a.archive_no AS archive_no, ")
               .append("a.task_id AS ref_id, ")
               .append("a.task_no AS ref_no, ")
               .append("a.archive_date AS archive_date, ")
               .append("a.retention_until AS retention_until, ")
               .append("a.report_hash AS report_hash ")
               .append("FROM ops.patl_archived_report a ")
               .append("WHERE 1=1");
            if (kw != null) {
                sql.append(" AND (a.archive_no LIKE ? OR a.task_no LIKE ?)");
                args.add("%" + kw + "%");
                args.add("%" + kw + "%");
            }
            if (!orgClause.isEmpty()) {
                sql.append(" AND a.org_id = ").append(orgIdLiteral());
            }
            unionParts.add(sql.toString());
        }

        if (unionParts.isEmpty()) {
            return new ArrayList<>();
        }

        String unionSql = String.join(" UNION ALL ", unionParts);
        // 排序 + 分页(外层包装)
        String pagedSql = "SELECT * FROM (" + unionSql + ") u ORDER BY archive_date DESC NULLS LAST LIMIT ? OFFSET ?";
        args.add(pageSize);
        args.add(offset);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(pagedSql, args.toArray());
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("archiveType", row.get("archive_type"));
            m.put("archiveNo", row.get("archive_no"));
            m.put("refId", row.get("ref_id"));
            m.put("refNo", row.get("ref_no"));
            m.put("archiveDate", toDateStr(row.get("archive_date")));
            m.put("retentionUntil", toDateStr(row.get("retention_until")));
            m.put("reportHash", row.get("report_hash"));
            result.add(m);
        }
        return result;
    }

    // ==================== 留存到期提醒 ====================

    @Override
    public List<Map<String, Object>> expiring(Integer days) {
        int d = (days == null || days < 0) ? 30 : days;
        String orgClause = orgFilter();
        List<Object> args = new ArrayList<>();
        args.add(d);

        List<String> unionParts = new ArrayList<>();

        // FIA
        {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT 'fia' AS archive_type, ")
               .append("report_no AS archive_no, ")
               .append("task_id AS ref_id, ")
               .append("retention_until ")
               .append("FROM ops.fia_archived_report WHERE retention_until <= CURRENT_DATE + ?::int");
            if (!orgClause.isEmpty()) {
                sql.append(" ").append(orgClause);
            }
            unionParts.add(sql.toString());
        }
        // Audit
        {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT 'audit' AS archive_type, ")
               .append("archive_no AS archive_no, ")
               .append("record_id AS ref_id, ")
               .append("retention_until ")
               .append("FROM ops.sqm_audit_report_archive WHERE retention_until <= CURRENT_DATE + ?::int");
            if (!orgClause.isEmpty()) {
                sql.append(" AND org_id = ").append(orgIdLiteral());
            }
            unionParts.add(sql.toString());
            args.add(d);
        }

        // 8D 归档到期提醒
        {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT '8d' AS archive_type, ")
               .append("archive_no AS archive_no, ")
               .append("report_id AS ref_id, ")
               .append("retention_until ")
               .append("FROM ops.qms_8d_archived_report WHERE status != '已作废' AND retention_until <= CURRENT_DATE + ?::int");
            if (!orgClause.isEmpty()) {
                sql.append(" AND org_id = ").append(orgIdLiteral());
            }
            unionParts.add(sql.toString());
            args.add(d);
        }
        // Patrol 归档到期提醒
        {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT 'patrol' AS archive_type, ")
               .append("archive_no AS archive_no, ")
               .append("task_id AS ref_id, ")
               .append("retention_until ")
               .append("FROM ops.patl_archived_report WHERE retention_until <= CURRENT_DATE + ?::int");
            if (!orgClause.isEmpty()) {
                sql.append(" AND org_id = ").append(orgIdLiteral());
            }
            unionParts.add(sql.toString());
            args.add(d);
        }

        String unionSql = String.join(" UNION ALL ", unionParts);
        String finalSql = "SELECT * FROM (" + unionSql + ") u ORDER BY retention_until ASC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(finalSql, args.toArray());

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            LocalDate retentionUntil = toLocalDate(row.get("retention_until"));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("archiveType", row.get("archive_type"));
            m.put("archiveNo", row.get("archive_no"));
            m.put("refId", row.get("ref_id"));
            m.put("retentionUntil", toDateStr(row.get("retention_until")));
            m.put("daysRemaining", retentionUntil != null ? ChronoUnit.DAYS.between(today, retentionUntil) : null);
            result.add(m);
        }
        return result;
    }

    // ==================== 档案详情 / 下载 ====================

    @Override
    public Map<String, Object> detail(String type, String refId) {
        if (refId == null || refId.isBlank()) return null;
        String t = type == null ? "" : type.trim().toLowerCase();
        if ("audit".equals(t)) {
            CompanyContext.CurrentUser au = CompanyContext.get();
            String auditOrg = (au != null && !CompanyContext.isAdmin() && au.orgId() != null && !au.orgId().isBlank())
                    ? " AND a.org_id = '" + au.orgId().replace("'", "''") + "'" : "";
            String sql = "SELECT a.archive_no, a.record_id, a.archive_date, a.report_hash, a.retention_until, "
                    + "a.report_file_path AS pdf_ref, "
                    + "r.record_no, r.audit_type, r.audit_lead AS auditor, r.audit_date, r.conclusion, r.status, r.plan_id "
                    + "FROM ops.sqm_audit_report_archive a "
                    + "LEFT JOIN ops.sqm_audit_record r ON r.id = a.record_id AND r.is_deleted = false "
                    + "WHERE a.record_id = ?" + auditOrg;
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, refId);
            if (rows.isEmpty()) return null;
            Map<String, Object> m = mapDetail("audit", rows.get(0));
            Object planObj = rows.get(0).get("plan_id");
            String planId = planObj == null ? null : String.valueOf(planObj);
            m.put("log", buildAuditLog(planId, rows.get(0)));
            return m;
        }
        if ("8d".equals(t)) {
            String archOrg = orgQualified();
            String sql = "SELECT a.archive_no, a.report_id, a.d8_no, a.archive_date, a.report_hash, a.retention_until, "
                    + "a.status, a.pdf_ref, "
                    + "r.issue, r.severity, r.source, r.source_ref_id, r.flow_type, r.status AS report_status, r.team, r.close_date "
                    + "FROM ops.qms_8d_archived_report a "
                    + "LEFT JOIN ops.qms_8d_report r ON r.id = a.report_id "
                    + "WHERE a.report_id = ? AND a.status != '已作废' ORDER BY a.archive_date DESC LIMIT 1" + archOrg;
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, refId);
            if (rows.isEmpty()) return null;
            Map<String, Object> m = mapDetail8d(rows.get(0));
            m.put("log", build8dLog(refId, rows.get(0)));
            return m;
        }
        if ("patrol".equals(t)) {
            String archOrg = orgQualified();
            String sql = "SELECT a.archive_no, a.task_id, a.task_no, a.route_id, a.archive_date, a.report_hash, a.retention_until, "
                    + "a.pdf_ref, "
                    + "t.shift, t.plan_time, t.finish_time, t.inspector_id, t.total_points, t.done_points, t.abnormal_count, t.status AS task_status, "
                    + "rt.name AS route_name "
                    + "FROM ops.patl_archived_report a "
                    + "LEFT JOIN ops.patl_task t ON t.id = a.task_id "
                    + "LEFT JOIN ops.patl_route rt ON rt.id = a.route_id "
                    + "WHERE a.task_id = ?" + archOrg;
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, refId);
            if (rows.isEmpty()) return null;
            return mapDetailPatrol(rows.get(0));
        }
        // 默认 fia
        String sql = "SELECT r.report_no, r.task_id, r.wo_no, r.archive_date, r.status, r.pdf_ref, r.report_hash, r.retention_until, "
                + "t.code AS task_code, t.line_name, t.proc_name, t.product_name, t.supplier_id, t.overall_judge, t.disposition, t.status AS task_status "
                + "FROM ops.fia_archived_report r "
                + "LEFT JOIN ops.fia_task t ON t.id = r.task_id "
                + "WHERE r.task_id = ? " + orgFilter();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, refId);
        if (rows.isEmpty()) return null;
        Map<String, Object> m = mapDetail("fia", rows.get(0));
        m.put("log", queryLog(refId));        // 流程轨迹(建单→录项→各级签名→归档)
        m.put("items", queryItems(refId));    // 检验项明细
        return m;
    }

    private Map<String, Object> mapDetail(String type, Map<String, Object> row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("archiveType", type);
        if ("audit".equals(type)) {
            m.put("archiveNo", row.get("archive_no"));
            m.put("refId", row.get("record_id"));
            m.put("refNo", row.get("record_no"));
            m.put("auditType", row.get("audit_type"));
            m.put("deptName", row.get("dept_name"));
            m.put("auditor", row.get("auditor"));
            m.put("auditDate", toDateStr(row.get("audit_date")));
            m.put("conclusion", row.get("conclusion"));
        } else {
            m.put("archiveNo", row.get("report_no"));
            m.put("refId", row.get("task_id"));
            m.put("refNo", row.get("wo_no"));
            m.put("taskCode", row.get("task_code"));
            m.put("lineName", row.get("line_name"));
            m.put("procName", row.get("proc_name"));
            m.put("productName", row.get("product_name"));
            m.put("supplierId", row.get("supplier_id"));
            m.put("overallJudge", row.get("overall_judge"));
            m.put("disposition", row.get("disposition"));
            m.put("taskStatus", row.get("task_status"));
        }
        m.put("archiveDate", toDateStr(row.get("archive_date")));
        m.put("status", row.get("status"));
        m.put("reportHash", row.get("report_hash"));
        m.put("retentionUntil", toDateStr(row.get("retention_until")));
        m.put("pdfRef", row.get("pdf_ref"));
        Object pdfRef = row.get("pdf_ref");
        m.put("hasPdf", pdfRef != null && !String.valueOf(pdfRef).startsWith("placeholder://"));
        return m;
    }

    private Map<String, Object> mapDetail8d(Map<String, Object> row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("archiveType", "8d");
        m.put("archiveNo", row.get("archive_no"));
        m.put("refId", row.get("report_id"));
        m.put("refNo", row.get("d8_no"));
        m.put("issue", row.get("issue"));
        m.put("severity", row.get("severity"));
        m.put("source", row.get("source"));
        m.put("sourceRefId", row.get("source_ref_id"));
        m.put("flowType", row.get("flow_type"));
        m.put("reportStatus", row.get("report_status"));
        m.put("team", row.get("team"));
        m.put("closeDate", toDateStr(row.get("close_date")));
        m.put("archiveDate", toDateStr(row.get("archive_date")));
        m.put("status", row.get("status"));
        m.put("reportHash", row.get("report_hash"));
        m.put("retentionUntil", toDateStr(row.get("retention_until")));
        m.put("pdfRef", row.get("pdf_ref"));
        Object pdfRef = row.get("pdf_ref");
        m.put("hasPdf", pdfRef != null && !String.valueOf(pdfRef).startsWith("placeholder://"));
        return m;
    }

    private Map<String, Object> mapDetailPatrol(Map<String, Object> row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("archiveType", "patrol");
        m.put("archiveNo", row.get("archive_no"));
        m.put("refId", row.get("task_id"));
        m.put("refNo", row.get("task_no"));
        m.put("routeId", row.get("route_id"));
        m.put("routeName", row.get("route_name"));
        m.put("shift", row.get("shift"));
        m.put("planTime", toDateTimeStr(row.get("plan_time")));
        m.put("finishTime", toDateTimeStr(row.get("finish_time")));
        m.put("inspectorId", row.get("inspector_id"));
        m.put("totalPoints", row.get("total_points"));
        m.put("donePoints", row.get("done_points"));
        m.put("abnormalCount", row.get("abnormal_count"));
        m.put("archiveDate", toDateStr(row.get("archive_date")));
        m.put("status", row.get("task_status"));
        m.put("reportHash", row.get("report_hash"));
        m.put("retentionUntil", toDateStr(row.get("retention_until")));
        m.put("pdfRef", row.get("pdf_ref"));
        Object pdfRef = row.get("pdf_ref");
        m.put("hasPdf", pdfRef != null && !String.valueOf(pdfRef).startsWith("placeholder://"));
        return m;
    }

    @Override
    public String pdfRef(String type, String refId) {
        Map<String, Object> d = detail(type, refId);
        return d == null ? null : (String) d.get("pdfRef");
    }

    private List<Map<String, Object>> queryLog(String taskId) {
        String sql = "SELECT node_name, op_time, operator, is_done FROM ops.fia_task_log WHERE task_id = ? ORDER BY node_seq";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, taskId);
        List<Map<String, Object>> res = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("node", r.get("node_name"));
            m.put("time", toDateTimeStr(r.get("op_time")));
            m.put("operator", r.get("operator"));
            m.put("done", r.get("is_done"));
            res.add(m);
        }
        return res;
    }

    private List<Map<String, Object>> queryItems(String taskId) {
        String sql = "SELECT item_name, is_ctq, std_value, tolerance, unit, measured_value, judge "
                + "FROM ops.fia_insp_item WHERE task_id = ? ORDER BY seq";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, taskId);
        List<Map<String, Object>> res = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("itemName", r.get("item_name"));
            m.put("isCtq", r.get("is_ctq"));
            m.put("stdValue", r.get("std_value"));
            m.put("tolerance", r.get("tolerance"));
            m.put("unit", r.get("unit"));
            m.put("measuredValue", r.get("measured_value"));
            m.put("judge", r.get("judge"));
            res.add(m);
        }
        return res;
    }

    /** 构造审核归档的流程轨迹:审核计划创建 → 各角色会签 → 现场审核执行 → 审核结论出具 → 归档报告。 */
    private List<Map<String, Object>> buildAuditLog(String planId, Map<String, Object> mainRow) {
        List<Map<String, Object>> log = new ArrayList<>();
        if (planId != null && !planId.isBlank()) {
            try {
                String planSql = "SELECT plan_date, audit_lead FROM ops.sqm_audit_plan WHERE id = ?";
                List<Map<String, Object>> pr = jdbcTemplate.queryForList(planSql, planId);
                if (!pr.isEmpty()) {
                    Map<String, Object> p = pr.get(0);
                    addLog(log, "审核计划创建", toDateStr(p.get("plan_date")), p.get("audit_lead"));
                    String apprSql = "SELECT role_label, status, operator, operate_date, seq_order "
                            + "FROM ops.sqm_audit_approval WHERE audit_id = ? ORDER BY seq_order, operate_date";
                    List<Map<String, Object>> aps = jdbcTemplate.queryForList(apprSql, planId);
                    for (Map<String, Object> a : aps) {
                        String st = a.get("status") == null ? "" : a.get("status").toString();
                        String suffix = "done".equals(st) ? "会签通过" : ("rejected".equals(st) ? "会签驳回" : "会签待处理");
                        String label = a.get("role_label") == null ? "会签" : a.get("role_label").toString();
                        addLog(log, label + suffix, toDateTimeStr(a.get("operate_date")), a.get("operator"));
                    }
                }
            } catch (Exception ignored) {
                // 轨迹缺失不影响详情主体返回
            }
        }
        Object auditor = mainRow.get("auditor");
        addLog(log, "现场审核执行", toDateTimeStr(mainRow.get("audit_date")), auditor);
        addLog(log, "审核结论出具", toDateTimeStr(mainRow.get("audit_date")), auditor);
        addLog(log, "归档报告", toDateStr(mainRow.get("archive_date")), null);
        return log;
    }

    /**
     * 构造 8D 整改归档的流程轨迹:各阶段(D1-D8)审批节点 → 报告闭环 → 归档报告。
     * 阶段节点时间优先取审批时间,否则取计划日期;操作人优先取审批人,否则取责任人。
     * 阶段明细缺失时,仍保留「报告闭环」「归档报告」两个节点,避免轨迹恒为空。
     */
    private List<Map<String, Object>> build8dLog(String reportId, Map<String, Object> mainRow) {
        List<Map<String, Object>> log = new ArrayList<>();
        try {
            String sql = "SELECT stage_code, approval_status, owner, approved_by, approved_at, plan_date "
                    + "FROM ops.qms_8d_stage_detail WHERE d8_id = ? ORDER BY stage_code";
            List<Map<String, Object>> stages = jdbcTemplate.queryForList(sql, reportId);
            for (Map<String, Object> s : stages) {
                String code = s.get("stage_code") == null ? "" : String.valueOf(s.get("stage_code"));
                String appr = s.get("approval_status") == null ? null : String.valueOf(s.get("approval_status"));
                String node = code + " 阶段" + (appr == null || appr.isEmpty() ? "" : ("·" + appr));
                Object time = s.get("approved_at") != null ? s.get("approved_at") : s.get("plan_date");
                Object operator = s.get("approved_by") != null ? s.get("approved_by") : s.get("owner");
                addLog(log, node, toDateTimeStr(time), operator);
            }
        } catch (Exception ignored) {
            // 轨迹缺失不影响详情主体返回
        }
        Object close = mainRow.get("close_date");
        if (close != null) addLog(log, "报告闭环", toDateStr(close), null);
        addLog(log, "归档报告", toDateStr(mainRow.get("archive_date")), null);
        return log;
    }

    private void addLog(List<Map<String, Object>> log, String node, String time, Object operator) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("node", node);
        m.put("time", time);
        m.put("operator", operator);
        log.add(m);
    }

    // ==================== 私有辅助 ====================

    /**
     * 应用级 org_id 过滤(对齐 DataScopeInterceptor 语义,JdbcTemplate 不走 MyBatis 拦截器,需手工拼接)。
     * 返回形如 "AND org_id = '...'" 的片段(含前导 AND);管理员返回空串。
     * 注意:调用方需将其附加在合适的位置(子查询中需用别名/表名限定)。
     */
    private String orgFilter() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null || CompanyContext.isAdmin()) {
            return "";
        }
        String orgId = u.orgId();
        if (orgId == null || orgId.isBlank()) {
            return "";
        }
        return "AND org_id = '" + orgId.replace("'", "''") + "'";
    }

    /** 同 {@link #orgFilter()} 但限定为归档表别名 a(用于含 LEFT JOIN 的详情查询,避免列歧义)。 */
    private String orgQualified() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null || CompanyContext.isAdmin()) {
            return "";
        }
        String orgId = u.orgId();
        if (orgId == null || orgId.isBlank()) {
            return "";
        }
        return "AND a.org_id = '" + orgId.replace("'", "''") + "'";
    }

    /** 返回 org_id 字面量(形如 '...'),非管理员场景下使用;管理员场景调用方不应使用此方法。 */
    private String orgIdLiteral() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null || CompanyContext.isAdmin()) {
            return "NULL"; // 不应被使用(管理员时 orgFilter 返回空,不会拼此字面量)
        }
        String orgId = u.orgId();
        return "'" + (orgId == null ? "" : orgId.replace("'", "''")) + "'";
    }

    private String toDateStr(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld.toString();
        if (o instanceof LocalDateTime ldt) return ldt.toLocalDate().toString();
        if (o instanceof Timestamp ts) return ts.toLocalDateTime().toLocalDate().toString();
        if (o instanceof java.sql.Date d) return d.toLocalDate().toString();
        return String.valueOf(o);
    }

    private String toDateTimeStr(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDateTime ldt) return ldt.toString().replace('T', ' ');
        if (o instanceof Timestamp ts) return ts.toLocalDateTime().toString().replace('T', ' ');
        if (o instanceof LocalDate ld) return ld.toString();
        if (o instanceof java.sql.Date d) return d.toLocalDate().toString();
        return String.valueOf(o);
    }

    private LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof LocalDateTime ldt) return ldt.toLocalDate();
        if (o instanceof Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        try {
            return LocalDate.parse(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }
}
