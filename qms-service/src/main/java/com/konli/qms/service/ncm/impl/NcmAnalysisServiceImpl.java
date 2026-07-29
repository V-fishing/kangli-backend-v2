package com.konli.qms.service.ncm.impl;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.ncm.NcmAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** NCM 不良分析聚合实现：按维度分组 + 交叉表 + 时间趋势（按当前用户分公司隔离） */
@Slf4j
@Service
@RequiredArgsConstructor
public class NcmAnalysisServiceImpl implements NcmAnalysisService {

    private final JdbcTemplate jdbc;

    private static final String BASE = """
        FROM ops.ncm_defect_record r
        LEFT JOIN ops.ncm_defect_dict d ON d.code = r.defect_dict_code
        """;

    /** 维度映射：防止 SQL 注入 */
    private String col(String dim) {
        return switch (dim) {
            case "supplier" -> "r.org_id::text";
            case "type" -> "d.name";
            case "proc" -> "r.process_code";
            case "dev" -> "r.device_code";
            case "batch" -> "r.batch_no";
            case "product" -> "r.product_model";
            case "severity" -> "r.severity";
            case "wo" -> "r.wo_no";
            default -> throw new IllegalArgumentException("不支持维度: " + dim);
        };
    }

    /**
     * 分公司隔离过滤：JdbcTemplate 不走 MyBatis 拦截器，须手工按当前用户 org_id 过滤。
     * sysadmin（orgId=null，跨公司）不过滤。
     */
    private String orgFilter(List<Object> params) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        // 跨公司管理员(dataScope=all, JWT orgId="ROOT")不过滤
        if (u == null || CompanyContext.isAdmin() || u.orgId() == null || "ROOT".equals(u.orgId())) {
            return "";
        }
        params.add(u.orgId());
        return " AND r.org_id = ?::uuid";
    }

    @Override
    public List<Map<String, Object>> aggregate(String dim) {
        String c = col(dim);
        List<Object> params = new ArrayList<>();
        String where = " WHERE 1=1" + orgFilter(params);
        String sql = "SELECT " + c + " AS name, COUNT(*) AS cnt, " +
                     "SUM(r.defect_count) AS total_qty, " +
                     "AVG(COALESCE(r.defect_rate, 0)) AS avg_rate " +
                     BASE + where + " GROUP BY " + c + " ORDER BY cnt DESC LIMIT 50";
        return jdbc.queryForList(sql, params.toArray());
    }

    @Override
    public List<Map<String, Object>> crossTable(String dim1, String dim2) {
        String c1 = col(dim1), c2 = col(dim2);
        List<Object> params = new ArrayList<>();
        String where = " WHERE 1=1" + orgFilter(params);
        String sql = "SELECT " + c1 + " AS x, " + c2 + " AS y, " +
                     "COUNT(*) AS cnt, SUM(r.defect_count) AS total_qty " +
                     BASE + where + " GROUP BY " + c1 + ", " + c2 + " ORDER BY cnt DESC LIMIT 200";
        return jdbc.queryForList(sql, params.toArray());
    }

    @Override
    public List<Map<String, Object>> trend(String period, String start, String end) {
        String trunc = switch (period) {
            case "day" -> "DATE(r.occurred_at)";
            case "week" -> "DATE_TRUNC('week', r.occurred_at)";
            case "month" -> "DATE_TRUNC('month', r.occurred_at)";
            default -> throw new IllegalArgumentException("period: day/week/month");
        };
        String where = "";
        List<Object> params = new ArrayList<>();
        where += orgFilter(params);
        if (start != null && !start.isBlank()) { where += " AND r.occurred_at >= ?::timestamptz"; params.add(start); }
        if (end != null && !end.isBlank()) { where += " AND r.occurred_at <= ?::timestamptz"; params.add(end); }
        String sql = "SELECT " + trunc + " AS dt, COUNT(*) AS cnt, " +
                     "SUM(r.defect_count) AS total_qty, " +
                     "AVG(COALESCE(r.defect_rate, 0)) AS avg_rate " +
                     BASE + " WHERE 1=1 " + where +
                     " GROUP BY " + trunc + " ORDER BY " + trunc + " LIMIT 200";
        return jdbc.queryForList(sql, params.toArray());
    }
}
