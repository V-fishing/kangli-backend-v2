package com.konli.qms.service.sqm.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.sqm.SqmAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQM 分析报表实现:用 JdbcTemplate 聚合 sqm_incoming_lot / sqm_incoming_abnormal /
 * sqm_supplier_performance + sqm_supplier。
 *
 * <p>JdbcTemplate 不走 MyBatis-Plus 拦截器,org_id 过滤需手工拼接(对齐
 * {@code DataScopeInterceptor} 语义);维度列经白名单校验后内联(防 SQL 注入)。</p>
 */
@Service
@RequiredArgsConstructor
public class SqmAnalysisServiceImpl implements SqmAnalysisService {

    private final JdbcTemplate jdbcTemplate;

    // ==================== 来料多维分析 ====================

    @Override
    public List<Map<String, Object>> incomingAnalysis(String dim, String startTime, String endTime) {
        String dimCol = resolveIncomingDimColumn(dim);
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT ").append(dimCol).append(" AS dim_value, ");
        sql.append("COUNT(*) AS total_count, ");
        sql.append("COUNT(CASE WHEN iqc_pass = true THEN 1 END) AS pass_count, ");
        sql.append("COUNT(CASE WHEN iqc_pass = false THEN 1 END) AS fail_count ");
        sql.append("FROM ops.sqm_incoming_lot WHERE is_deleted = false ");
        appendDateFilter(sql, args, "incoming_date", startTime, endTime);
        sql.append(orgFilter());
        sql.append("GROUP BY ").append(dimCol).append(" ORDER BY total_count DESC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            long total = toLong(row.get("total_count"));
            long pass = toLong(row.get("pass_count"));
            long fail = toLong(row.get("fail_count"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("dimValue", row.get("dim_value"));
            item.put("totalCount", total);
            item.put("passCount", pass);
            item.put("failCount", fail);
            item.put("passRate", passRate(pass, total));
            result.add(item);
        }
        return result;
    }

    // ==================== 来料异常多维分析 ====================

    @Override
    public List<Map<String, Object>> abnormalAnalysis(String dim, String startTime, String endTime) {
        String dimCol = resolveAbnormalDimColumn(dim);
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT ").append(dimCol).append(" AS dim_value, ");
        sql.append("COUNT(*) AS total_count, ");
        sql.append("COUNT(CASE WHEN level = '严重' THEN 1 END) AS severe_count, ");
        sql.append("COUNT(CASE WHEN level = '一般' THEN 1 END) AS normal_count ");
        sql.append("FROM ops.sqm_incoming_abnormal WHERE is_deleted = false ");
        appendDateFilter(sql, args, "occur_date", startTime, endTime);
        sql.append(orgFilter());
        sql.append("GROUP BY ").append(dimCol).append(" ORDER BY total_count DESC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("dimValue", row.get("dim_value"));
            item.put("totalCount", toLong(row.get("total_count")));
            Map<String, Object> severityCount = new LinkedHashMap<>();
            severityCount.put("严重", toLong(row.get("severe_count")));
            severityCount.put("一般", toLong(row.get("normal_count")));
            item.put("severityCount", severityCount);
            result.add(item);
        }
        return result;
    }

    // ==================== 来料看板 ====================

    @Override
    public Map<String, Object> dashboard() {
        // 今日批次数 + 今日合格率
        StringBuilder todaySql = new StringBuilder();
        todaySql.append("SELECT COUNT(*) AS total, ");
        todaySql.append("COUNT(CASE WHEN iqc_pass = true THEN 1 END) AS pass ");
        todaySql.append("FROM ops.sqm_incoming_lot ");
        todaySql.append("WHERE is_deleted = false AND incoming_date = CURRENT_DATE ");
        todaySql.append(orgFilter());
        Map<String, Object> todayRow = jdbcTemplate.queryForMap(todaySql.toString());
        long todayTotal = toLong(todayRow.get("total"));
        long todayPass = toLong(todayRow.get("pass"));
        BigDecimal passRate = passRate(todayPass, todayTotal);

        // 待处理异常数
        StringBuilder pendingSql = new StringBuilder();
        pendingSql.append("SELECT COUNT(*) AS cnt FROM ops.sqm_incoming_abnormal ");
        pendingSql.append("WHERE is_deleted = false AND status = '待处理' ");
        pendingSql.append(orgFilter());
        long pendingAbnormals = toLong(jdbcTemplate.queryForMap(pendingSql.toString()).get("cnt"));

        // Top5 不良供应商(按不合格批次数)
        StringBuilder topSql = new StringBuilder();
        topSql.append("SELECT l.supplier_id AS supplier_id, s.name AS supplier_name, COUNT(*) AS cnt ");
        topSql.append("FROM ops.sqm_incoming_lot l ");
        topSql.append("LEFT JOIN ops.sqm_supplier s ON s.id = l.supplier_id AND s.is_deleted = false ");
        topSql.append("WHERE l.is_deleted = false AND l.iqc_pass = false ");
        topSql.append(orgFilter("l"));
        topSql.append("GROUP BY l.supplier_id, s.name ORDER BY cnt DESC LIMIT 5");
        List<Map<String, Object>> topRows = jdbcTemplate.queryForList(topSql.toString());
        List<Map<String, Object>> top5BadSuppliers = new ArrayList<>(topRows.size());
        for (Map<String, Object> row : topRows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("supplierId", row.get("supplier_id"));
            item.put("supplierName", row.get("supplier_name"));
            item.put("failCount", toLong(row.get("cnt")));
            top5BadSuppliers.add(item);
        }

        // 近 7 日趋势
        StringBuilder trendSql = new StringBuilder();
        trendSql.append("SELECT to_char(incoming_date, 'YYYY-MM-DD') AS d, ");
        trendSql.append("COUNT(*) AS total, ");
        trendSql.append("COUNT(CASE WHEN iqc_pass = true THEN 1 END) AS pass ");
        trendSql.append("FROM ops.sqm_incoming_lot ");
        trendSql.append("WHERE is_deleted = false AND incoming_date >= CURRENT_DATE - INTERVAL '6 days' ");
        trendSql.append(orgFilter());
        trendSql.append("GROUP BY incoming_date ORDER BY incoming_date ASC");
        List<Map<String, Object>> trendRows = jdbcTemplate.queryForList(trendSql.toString());
        List<Map<String, Object>> trend7d = new ArrayList<>(trendRows.size());
        for (Map<String, Object> row : trendRows) {
            long total = toLong(row.get("total"));
            long pass = toLong(row.get("pass"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", row.get("d"));
            item.put("total", total);
            item.put("pass", pass);
            item.put("passRate", passRate(pass, total));
            trend7d.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayLots", todayTotal);
        result.put("passRate", passRate);
        result.put("pendingAbnormals", pendingAbnormals);
        result.put("top5BadSuppliers", top5BadSuppliers);
        result.put("trend7d", trend7d);
        return result;
    }

    // ==================== 供应商绩效排名 ====================

    @Override
    public List<Map<String, Object>> ranking(String period) {
        if (period == null || period.isBlank()) {
            throw new BusinessException(400, "period 不能为空, 格式: YYYY-MM");
        }
        try {
            YearMonth.parse(period);
        } catch (Exception e) {
            throw new BusinessException(400, "period 格式错误, 应为 YYYY-MM: " + period);
        }

        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT sp.supplier_id AS supplier_id, s.name AS supplier_name, ");
        sql.append("sp.score AS score, sp.level AS level, sp.incoming_pass_rate AS incoming_pass_rate ");
        sql.append("FROM ops.sqm_supplier_performance sp ");
        sql.append("LEFT JOIN ops.sqm_supplier s ON s.id = sp.supplier_id AND s.is_deleted = false ");
        sql.append("WHERE sp.period = ? ");
        args.add(period);
        sql.append(orgFilter("sp"));
        sql.append("ORDER BY sp.score DESC NULLS LAST");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("supplierId", row.get("supplier_id"));
            item.put("supplierName", row.get("supplier_name"));
            item.put("score", row.get("score"));
            item.put("level", row.get("level"));
            item.put("incomingPassRate", row.get("incoming_pass_rate"));
            result.add(item);
        }
        return result;
    }

    // ==================== 私有辅助 ====================

    private String resolveIncomingDimColumn(String dim) {
        if (dim == null) {
            throw new BusinessException(400, "dim 不能为空, 可选: supplierId/partNo/inspectResult");
        }
        return switch (dim) {
            case "supplierId" -> "supplier_id";
            case "partNo" -> "part_no";
            case "inspectResult" -> "inspect_result";
            default -> throw new BusinessException(400,
                    "无效的维度: " + dim + ", 可选: supplierId/partNo/inspectResult");
        };
    }

    private String resolveAbnormalDimColumn(String dim) {
        if (dim == null) {
            throw new BusinessException(400, "dim 不能为空, 可选: supplierId/partNo/level");
        }
        return switch (dim) {
            case "supplierId" -> "supplier_id";
            case "partNo" -> "part_no";
            case "level" -> "level";
            default -> throw new BusinessException(400,
                    "无效的维度: " + dim + ", 可选: supplierId/partNo/level");
        };
    }

    private void appendDateFilter(StringBuilder sql, List<Object> args,
                                  String dateColumn, String startTime, String endTime) {
        LocalDate start = parseDate(startTime);
        LocalDate end = parseDate(endTime);
        if (start != null) {
            sql.append("AND ").append(dateColumn).append(" >= ? ");
            args.add(Date.valueOf(start));
        }
        if (end != null) {
            sql.append("AND ").append(dateColumn).append(" <= ? ");
            args.add(Date.valueOf(end));
        }
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return parseTime(s).toLocalDate();
    }

    private LocalDateTime parseTime(String s) {
        // epoch 毫秒
        try {
            long ms = Long.parseLong(s);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
        } catch (NumberFormatException ignored) {
            // 继续尝试其他格式
        }
        // ISO 日期时间(含 T)
        try {
            return LocalDateTime.parse(s);
        } catch (Exception ignored) {
            // 继续尝试
        }
        // ISO 日期(yyyy-MM-dd)
        try {
            return LocalDate.parse(s).atStartOfDay();
        } catch (Exception ignored) {
            // 继续尝试
        }
        throw new BusinessException(400, "无效的时间格式: " + s + ", 支持: epoch毫秒 / ISO日期时间 / ISO日期");
    }

    /** 合格率 = pass / total * 100,保留 2 位;total<=0 返回 0.00 */
    private BigDecimal passRate(long pass, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(pass)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private long toLong(Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(o.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 应用级 org_id 过滤(对齐 DataScopeInterceptor 语义,但 JdbcTemplate 不走 MyBatis 拦截器,需手工拼接)。
     * orgId 来自签名 JWT(可信),defensively escape。alias 为 JOIN 场景的表别名。
     */
    private String orgFilter() {
        return orgFilter(null);
    }

    private String orgFilter(String alias) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null || CompanyContext.isAdmin()) {
            return "";
        }
        String orgId = u.orgId();
        if (orgId == null || orgId.isBlank()) {
            return "";
        }
        String safe = orgId.replace("'", "''");
        String col = (alias == null || alias.isBlank()) ? "org_id" : alias + ".org_id";
        return " AND " + col + " = '" + safe + "' ";
    }
}
