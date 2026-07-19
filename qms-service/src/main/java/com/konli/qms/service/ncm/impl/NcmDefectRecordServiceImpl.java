package com.konli.qms.service.ncm.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.domain.ncm.mapper.NcmDefectRecordMapper;
import com.konli.qms.service.ncm.NcmDefectRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NcmDefectRecordServiceImpl implements NcmDefectRecordService {

    private final NcmDefectRecordMapper ncmDefectRecordMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<NcmDefectRecord> list() {
        return ncmDefectRecordMapper.selectList(null);
    }

    @Override
    public NcmDefectRecord get(String id) {
        return ncmDefectRecordMapper.selectById(id);
    }

    @Override
    @Transactional
    public NcmDefectRecord create(NcmDefectRecord record) {
        record.setDefectNo("DF-" + System.currentTimeMillis());
        if (record.getOccurredAt() == null) {
            record.setOccurredAt(LocalDateTime.now());
        }
        if (record.getSource() == null) {
            record.setSource("手动");
        }
        if (record.getOperatorId() == null) {
            record.setOperatorId(currentOperator());
        }
        // 自动计算缺陷率 = defectCount / batchTotal
        if (record.getDefectCount() != null && record.getBatchTotal() != null
                && record.getBatchTotal() > 0 && record.getDefectRate() == null) {
            record.setDefectRate(BigDecimal.valueOf(record.getDefectCount())
                    .divide(BigDecimal.valueOf(record.getBatchTotal()), 4, RoundingMode.HALF_UP));
        }
        ncmDefectRecordMapper.insert(record);
        return record;
    }

    // ==================== 分析报表 ====================

    @Override
    public List<Map<String, Object>> multiDimAnalysis(String dim, String startTime, String endTime) {
        String dimCol = resolveDimColumn(dim);
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT ").append(dimCol).append(" AS dim_value, ");
        sql.append("COUNT(*) AS total_count, ");
        sql.append("COUNT(CASE WHEN severity = '严重' THEN 1 END) AS severe_count, ");
        sql.append("COUNT(CASE WHEN severity = '一般' THEN 1 END) AS normal_count ");
        sql.append("FROM ops.ncm_defect_record WHERE is_deleted = false ");
        appendTimeFilter(sql, args, startTime, endTime);
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

    @Override
    public List<Map<String, Object>> trendAnalysis(String granularity, String startTime, String endTime) {
        // 维度经白名单校验,可安全内联(防 SQL 注入)
        String gran = resolveGranularity(granularity);
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT to_char(date_trunc('").append(gran).append("', occurred_at), 'YYYY-MM-DD') AS period, ");
        sql.append("COUNT(*) AS cnt, ");
        sql.append("COALESCE(SUM(defect_count), 0) AS total_defect, ");
        sql.append("COALESCE(SUM(batch_total), 0) AS total_batch ");
        sql.append("FROM ops.ncm_defect_record WHERE is_deleted = false ");
        appendTimeFilter(sql, args, startTime, endTime);
        sql.append(orgFilter());
        sql.append("GROUP BY date_trunc('").append(gran).append("', occurred_at) ");
        sql.append("ORDER BY date_trunc('").append(gran).append("', occurred_at) ASC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("period", row.get("period"));
            item.put("count", toLong(row.get("cnt")));
            long totalDefect = toLong(row.get("total_defect"));
            long totalBatch = toLong(row.get("total_batch"));
            BigDecimal rate = totalBatch > 0
                    ? BigDecimal.valueOf(totalDefect).divide(BigDecimal.valueOf(totalBatch), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            item.put("defectRate", rate);
            result.add(item);
        }
        return result;
    }

    @Override
    public Map<String, Object> compareAnalysis(String period) {
        if (period == null || period.isBlank()) {
            throw new BusinessException(400, "period 不能为空, 格式: YYYY-MM");
        }
        YearMonth ym;
        try {
            ym = YearMonth.parse(period);
        } catch (Exception e) {
            throw new BusinessException(400, "period 格式错误, 应为 YYYY-MM: " + period);
        }
        LocalDateTime curStart = ym.atDay(1).atStartOfDay();
        LocalDateTime curEnd = ym.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime prevStart = ym.minusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime yoyStart = ym.minusYears(1).atDay(1).atStartOfDay();
        LocalDateTime yoyEnd = ym.minusYears(1).plusMonths(1).atDay(1).atStartOfDay();

        long current = countByRange(curStart, curEnd);
        long previous = countByRange(prevStart, curStart);
        long yoy = countByRange(yoyStart, yoyEnd);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("current", current);
        result.put("previous", previous);
        result.put("yoy", yoy);
        result.put("mom", rate(current, previous));
        result.put("changeRate", rate(current, yoy));
        return result;
    }

    @Override
    public Map<String, Object> dashboard() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();

        // 今日不良数 + 当前班次(简化为今日)不良率
        long todayCount = countByRange(startOfToday, startOfTomorrow);
        BigDecimal shiftDefectRate = shiftDefectRate(startOfToday, startOfTomorrow);

        // Top5 不良类型
        StringBuilder topSql = new StringBuilder();
        topSql.append("SELECT defect_dict_code AS defect_dict_code, COUNT(*) AS cnt ");
        topSql.append("FROM ops.ncm_defect_record WHERE is_deleted = false ");
        topSql.append(orgFilter());
        topSql.append("GROUP BY defect_dict_code ORDER BY cnt DESC LIMIT 5");
        List<Map<String, Object>> topRows = jdbcTemplate.queryForList(topSql.toString());
        List<Map<String, Object>> top5 = new ArrayList<>(topRows.size());
        for (Map<String, Object> row : topRows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("defectDictCode", row.get("defect_dict_code"));
            item.put("count", toLong(row.get("cnt")));
            top5.add(item);
        }

        // 工序热力图
        StringBuilder heatSql = new StringBuilder();
        heatSql.append("SELECT process_code AS process_code, COUNT(*) AS cnt ");
        heatSql.append("FROM ops.ncm_defect_record WHERE is_deleted = false ");
        heatSql.append(orgFilter());
        heatSql.append("GROUP BY process_code ORDER BY cnt DESC");
        List<Map<String, Object>> heatRows = jdbcTemplate.queryForList(heatSql.toString());
        List<Map<String, Object>> heatmap = new ArrayList<>(heatRows.size());
        for (Map<String, Object> row : heatRows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("processCode", row.get("process_code"));
            item.put("count", toLong(row.get("cnt")));
            heatmap.add(item);
        }

        // 数据新鲜度:最新 occurred_at
        StringBuilder freshSql = new StringBuilder();
        freshSql.append("SELECT MAX(occurred_at) AS latest FROM ops.ncm_defect_record WHERE is_deleted = false ");
        freshSql.append(orgFilter());
        List<Map<String, Object>> freshRows = jdbcTemplate.queryForList(freshSql.toString());
        Object latest = (freshRows.isEmpty() || freshRows.get(0).isEmpty()) ? null : freshRows.get(0).get("latest");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayDefectCount", todayCount);
        result.put("currentShiftDefectRate", shiftDefectRate);
        result.put("top5DefectTypes", top5);
        result.put("processHeatmap", heatmap);
        result.put("dataFreshness", latest);
        return result;
    }

    // ==================== 私有辅助 ====================

    private String resolveDimColumn(String dim) {
        if (dim == null) {
            throw new BusinessException(400, "dim 不能为空, 可选: processCode/defectDictCode/deviceCode/batchNo");
        }
        return switch (dim) {
            case "processCode" -> "process_code";
            case "defectDictCode" -> "defect_dict_code";
            case "deviceCode" -> "device_code";
            case "batchNo" -> "batch_no";
            default -> throw new BusinessException(400,
                    "无效的维度: " + dim + ", 可选: processCode/defectDictCode/deviceCode/batchNo");
        };
    }

    private String resolveGranularity(String granularity) {
        String g = granularity == null ? "day" : granularity.toLowerCase();
        return switch (g) {
            case "day" -> "day";
            case "week" -> "week";
            case "month" -> "month";
            default -> throw new BusinessException(400,
                    "无效的粒度: " + granularity + ", 可选: day/week/month");
        };
    }

    private long countByRange(LocalDateTime start, LocalDateTime end) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT COUNT(*) AS cnt FROM ops.ncm_defect_record ");
        sql.append("WHERE is_deleted = false AND occurred_at >= ? AND occurred_at < ? ");
        args.add(Timestamp.valueOf(start));
        args.add(Timestamp.valueOf(end));
        sql.append(orgFilter());
        return toLong(jdbcTemplate.queryForMap(sql.toString(), args.toArray()).get("cnt"));
    }

    private BigDecimal shiftDefectRate(LocalDateTime start, LocalDateTime end) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT COALESCE(SUM(defect_count), 0) AS total_defect, ");
        sql.append("COALESCE(SUM(batch_total), 0) AS total_batch ");
        sql.append("FROM ops.ncm_defect_record ");
        sql.append("WHERE is_deleted = false AND occurred_at >= ? AND occurred_at < ? ");
        args.add(Timestamp.valueOf(start));
        args.add(Timestamp.valueOf(end));
        sql.append(orgFilter());
        Map<String, Object> row = jdbcTemplate.queryForMap(sql.toString(), args.toArray());
        long totalDefect = toLong(row.get("total_defect"));
        long totalBatch = toLong(row.get("total_batch"));
        return totalBatch > 0
                ? BigDecimal.valueOf(totalDefect).divide(BigDecimal.valueOf(totalBatch), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    private void appendTimeFilter(StringBuilder sql, List<Object> args, String startTime, String endTime) {
        LocalDateTime start = parseTime(startTime);
        LocalDateTime end = parseTime(endTime);
        if (start != null) {
            sql.append("AND occurred_at >= ? ");
            args.add(Timestamp.valueOf(start));
        }
        if (end != null) {
            sql.append("AND occurred_at <= ? ");
            args.add(Timestamp.valueOf(end));
        }
    }

    private LocalDateTime parseTime(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
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

    /** 环比/同比变化率 = (current - base) / base;base<=0 返回 null */
    private BigDecimal rate(long current, long base) {
        if (base <= 0) {
            return null;
        }
        return BigDecimal.valueOf(current - base)
                .divide(BigDecimal.valueOf(base), 4, RoundingMode.HALF_UP);
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
     * orgId 来自签名 JWT(可信),defensively escape。
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
        String safe = orgId.replace("'", "''");
        return " AND org_id = '" + safe + "' ";
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }
}
