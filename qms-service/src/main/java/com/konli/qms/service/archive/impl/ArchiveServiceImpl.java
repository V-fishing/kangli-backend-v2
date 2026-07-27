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

        // 8D 归档表暂未建,type=8d 返回空
        if ("8d".equals(t)) {
            return new ArrayList<>();
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
