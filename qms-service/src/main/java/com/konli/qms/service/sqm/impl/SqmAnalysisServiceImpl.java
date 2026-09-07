package com.konli.qms.service.sqm.impl;

import com.konli.qms.common.api.PageResult;
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

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
            period = YearMonth.now().toString();
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

    // ==================== 供应商看板:五聚合 ====================

    /** 五档分桶:>=100 / [90,100) / [80,90) / [70,80) / <70,返回 bucket 与中文 label */
    private static final String[][] PASS_BUCKETS = {
            {"100", "100%"},
            {"90_100", "90%~100%"},
            {"80_90", "80%~90%"},
            {"70_80", "70%~80%"},
            {"lt70", "<70%"},
    };

    @Override
    public List<Map<String, Object>> passRateDist(String level, String keyword, String startYm, String endYm) {
        // 1) 符合筛选的供应商
        List<Map<String, Object>> suppliers = listFilteredSuppliers(level, keyword);
        if (suppliers.isEmpty()) {
            return emptyDist();
        }
        // 2) 每个供应商最新一期绩效合格率(优先) + 异常数
        Map<String, BigDecimal> perfRate = new LinkedHashMap<>();
        Map<String, Long> abnormalCount = new LinkedHashMap<>();
        StringBuilder perfSql = new StringBuilder();
        perfSql.append("SELECT DISTINCT ON (supplier_id) supplier_id, incoming_pass_rate ");
        perfSql.append("FROM ops.sqm_supplier_performance WHERE 1=1 ");
        if (startYm != null && !startYm.isBlank()) {
            perfSql.append("AND period >= '").append(safeYm(startYm)).append("' ");
        }
        if (endYm != null && !endYm.isBlank()) {
            perfSql.append("AND period <= '").append(safeYm(endYm)).append("' ");
        }
        perfSql.append(orgFilter("sqm_supplier_performance")).append(" ORDER BY supplier_id, period DESC");
        Map<String, BigDecimal> latestPerf = new LinkedHashMap<>();
        for (Map<String, Object> r : jdbcTemplate.queryForList(perfSql.toString())) {
            latestPerf.put(String.valueOf(r.get("supplier_id")), toBigDecimal(r.get("incoming_pass_rate")));
        }
        // 3) 异常数(按筛选月份范围)
        Map<String, Long> abnormalMap = loadAbnormalCount(startYm, endYm);

        // 4) 分桶
        Map<String, List<Map<String, Object>>> bucketSuppliers = new LinkedHashMap<>();
        for (String[] b : PASS_BUCKETS) bucketSuppliers.put(b[0], new ArrayList<>());
        for (Map<String, Object> s : suppliers) {
            String sid = String.valueOf(s.get("id"));
            BigDecimal rate = latestPerf.get(sid);
            if (rate == null) {
                rate = calcRealtimePassRate(sid); // 兜底:全量 iqc_pass
            }
            String bucket = bucketOf(rate);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("supplierId", sid);
            item.put("supplierName", s.get("name"));
            item.put("level", s.get("level"));
            item.put("passRate", rate);
            item.put("abnormalCount", abnormalMap.getOrDefault(sid, 0L));
            bucketSuppliers.get(bucket).add(item);
        }
        // 5) 组装返回
        List<Map<String, Object>> result = new ArrayList<>(PASS_BUCKETS.length);
        for (String[] b : PASS_BUCKETS) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("bucket", b[0]);
            item.put("label", b[1]);
            List<Map<String, Object>> list = bucketSuppliers.get(b[0]);
            item.put("count", list.size());
            item.put("suppliers", list);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> passRateTrend(boolean observeOnly, List<String> supplierIds, String startYm, String endYm) {
        String from = (startYm != null && !startYm.isBlank()) ? safeYm(startYm) : YearMonth.now().minusMonths(11).toString();
        String to = (endYm != null && !endYm.isBlank()) ? safeYm(endYm) : YearMonth.now().toString();
        List<Map<String, Object>> suppliers = listFilteredSuppliers(null, null);
        // 目标供应商集合
        Set<String> targets = new LinkedHashSet<>();
        if (observeOnly) {
            for (Map<String, Object> s : suppliers) {
                if (Boolean.TRUE.equals(s.get("observe_flag"))) targets.add(String.valueOf(s.get("id")));
            }
        }
        if (supplierIds != null) {
            for (String id : supplierIds) {
                if (id != null && !id.isBlank()) targets.add(id);
            }
        }
        if (targets.isEmpty()) {
            // 无重点观察供应商时,回退到合格率最低的 8 家(避免线条过多,聚焦问题供应商)
            int fallbackTopN = 8;
            StringBuilder ps = new StringBuilder();
            ps.append("SELECT DISTINCT ON (supplier_id) supplier_id, incoming_pass_rate ");
            ps.append("FROM ops.sqm_supplier_performance WHERE 1=1 ");
            ps.append(orgFilter("sqm_supplier_performance")).append(" ORDER BY supplier_id, period DESC");
            List<Map<String, Object>> latest = jdbcTemplate.queryForList(ps.toString());
            latest.stream()
                    .sorted((a, b) -> {
                        BigDecimal av = toBigDecimal(a.get("incoming_pass_rate"));
                        BigDecimal bv = toBigDecimal(b.get("incoming_pass_rate"));
                        if (av == null) return 1;
                        if (bv == null) return -1;
                        return av.compareTo(bv);
                    })
                    .limit(fallbackTopN)
                    .forEach(r -> targets.add(String.valueOf(r.get("supplier_id"))));
        }
        if (targets.isEmpty()) return new ArrayList<>();

        // 一次 SQL 批量查目标供应商在月份范围内的实时合格率(替代逐供应商 calcRealtimePassRate)
        // 以实时聚合(来料表)为准:绩效表可能在无来料月份生成 rate(历史遗留/生成逻辑问题),不可作为"有来料"依据
        Map<String, Map<String, Object>> realtimeBySupplier = new LinkedHashMap<>();
        StringBuilder rs = new StringBuilder();
        rs.append("SELECT supplier_id, to_char(incoming_date,'YYYY-MM') AS ym, ");
        rs.append("COUNT(*) AS total, COUNT(CASE WHEN iqc_pass = true THEN 1 END) AS pass ");
        rs.append("FROM ops.sqm_incoming_lot WHERE is_deleted = false AND supplier_id IN (");
        int idx = 0;
        for (String sid : targets) {
            if (idx++ > 0) rs.append(",");
            rs.append("'").append(sid.replace("'", "''")).append("'");
        }
        rs.append(") AND to_char(incoming_date,'YYYY-MM') >= '").append(from).append("' ");
        rs.append("AND to_char(incoming_date,'YYYY-MM') <= '").append(to).append("' ");
        rs.append(orgFilter()).append(" GROUP BY supplier_id, ym");
        for (Map<String, Object> r : jdbcTemplate.queryForList(rs.toString())) {
            String sid = String.valueOf(r.get("supplier_id"));
            String ym = String.valueOf(r.get("ym"));
            long total = toLong(r.get("total"));
            long pass = toLong(r.get("pass"));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rate", passRate(pass, total));
            m.put("total", total);
            realtimeBySupplier.computeIfAbsent(sid, k -> new LinkedHashMap<>()).put(ym, m);
        }

        // 组装:每个目标供应商每月绩效合格率,缺月用实时聚合兜底
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String sid : targets) {
            Map<String, Object> sup = suppliers.stream()
                    .filter(s -> String.valueOf(s.get("id")).equals(sid)).findFirst().orElse(null);
            if (sup == null) continue;
            String sname = String.valueOf(sup.get("name"));
            Map<String, Object> realtimeMap = realtimeBySupplier.getOrDefault(sid, new LinkedHashMap<>());
            // 遍历月份区间:只返回有实际来料的月份(实时聚合来料表有记录),缺月跳过
            // 避免前端把 null 当 0% 渲染,也避免绩效表在无来料月份生成的 rate 造成假 0%
            YearMonth cursor = YearMonth.parse(from);
            YearMonth end = YearMonth.parse(to);
            while (!cursor.isAfter(end)) {
                String ym = cursor.toString();
                Object realtime = realtimeMap.get(ym);
                if (realtime == null) { cursor = cursor.plusMonths(1); continue; } // 该月无实际来料
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("period", ym);
                point.put("supplierId", sid);
                point.put("supplierName", sname);
                point.put("passRate", ((Map<?, ?>) realtime).get("rate"));
                point.put("total", ((Map<?, ?>) realtime).get("total"));
                rows.add(point);
                cursor = cursor.plusMonths(1);
            }
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> abnormalHeat(String year, int topN) {
        int y = (year != null && !year.isBlank()) ? Integer.parseInt(year) : YearMonth.now().getYear();
        // 异常数 Top N 供应商
        StringBuilder topSql = new StringBuilder();
        topSql.append("SELECT supplier_id, COUNT(*) AS cnt FROM ops.sqm_incoming_abnormal ");
        topSql.append("WHERE is_deleted = false AND to_char(occur_date,'YYYY') = '").append(y).append("' ");
        topSql.append(orgFilter()).append(" GROUP BY supplier_id ORDER BY cnt DESC LIMIT ").append(topN > 0 ? topN : 20);
        List<Map<String, Object>> topRows = jdbcTemplate.queryForList(topSql.toString());
        if (topRows.isEmpty()) return new ArrayList<>();

        List<String> topIds = new ArrayList<>();
        for (Map<String, Object> r : topRows) topIds.add(String.valueOf(r.get("supplier_id")));
        // 名称
        Map<String, String> names = supplierNames(topIds);
        // 月份交叉
        StringBuilder heatSql = new StringBuilder();
        heatSql.append("SELECT supplier_id, to_char(occur_date,'YYYY-MM') AS ym, COUNT(*) AS cnt ");
        heatSql.append("FROM ops.sqm_incoming_abnormal WHERE is_deleted = false ");
        heatSql.append("AND to_char(occur_date,'YYYY') = '").append(y).append("' ");
        heatSql.append("AND supplier_id IN (");
        for (int i = 0; i < topIds.size(); i++) {
            if (i > 0) heatSql.append(",");
            heatSql.append("'").append(topIds.get(i)).append("'");
        }
        heatSql.append(") ").append(orgFilter()).append(" GROUP BY supplier_id, ym");
        Map<String, Map<String, Integer>> monthsMap = new LinkedHashMap<>();
        for (Map<String, Object> r : jdbcTemplate.queryForList(heatSql.toString())) {
            String sid = String.valueOf(r.get("supplier_id"));
            String ym = String.valueOf(r.get("ym"));
            int cnt = toInt(r.get("cnt"));
            monthsMap.computeIfAbsent(sid, k -> new LinkedHashMap<>()).put(ym, cnt);
        }
        List<Map<String, Object>> result = new ArrayList<>(topIds.size());
        for (String sid : topIds) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("supplierId", sid);
            item.put("supplierName", names.getOrDefault(sid, sid));
            item.put("months", monthsMap.getOrDefault(sid, new LinkedHashMap<String, Integer>()));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> levelRatio(String level, String keyword) {
        List<Map<String, Object>> suppliers = listFilteredSuppliers(level, keyword);
        Map<String, Long> cnt = new LinkedHashMap<>();
        for (Map<String, Object> s : suppliers) {
            String lv = s.get("level") == null ? "未知" : String.valueOf(s.get("level"));
            cnt.put(lv, cnt.getOrDefault(lv, 0L) + 1);
        }
        List<Map<String, Object>> result = new ArrayList<>(cnt.size());
        for (Map.Entry<String, Long> e : cnt.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("level", e.getKey());
            item.put("count", e.getValue());
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> inspectResult(String level, String keyword) {
        List<Map<String, Object>> suppliers = listFilteredSuppliers(level, keyword);
        if (suppliers.isEmpty()) return new ArrayList<>();
        String ids = suppliers.stream().map(s -> String.valueOf(s.get("id"))).collect(Collectors.joining("','", "'", "'"));
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CASE ");
        sql.append(" WHEN inspect_result LIKE '%不合格%' THEN '不合格' ");
        sql.append(" WHEN inspect_result LIKE '%合格%' THEN '合格' ");
        sql.append(" WHEN inspect_result LIKE '%待检%' THEN '待检' ");
        sql.append(" ELSE COALESCE(NULLIF(inspect_result,''),'未知') END AS r, COUNT(*) AS c ");
        sql.append("FROM ops.sqm_incoming_lot WHERE is_deleted = false AND supplier_id IN (").append(ids).append(") ");
        sql.append(orgFilter("sqm_incoming_lot")).append(" GROUP BY r ORDER BY c DESC");
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> r : jdbcTemplate.queryForList(sql.toString())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("result", String.valueOf(r.get("r")));
            item.put("count", toLong(r.get("c")));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> deliveryVsPass(String level, String keyword) {
        List<Map<String, Object>> suppliers = listFilteredSuppliers(level, keyword);
        if (suppliers.isEmpty()) return new ArrayList<>();
        // 每个供应商最新一期绩效
        StringBuilder ps = new StringBuilder();
        ps.append("SELECT DISTINCT ON (supplier_id) supplier_id, delivery_timely_rate, incoming_pass_rate, score ");
        ps.append("FROM ops.sqm_supplier_performance WHERE 1=1 ");
        ps.append(orgFilter("sqm_supplier_performance")).append(" ORDER BY supplier_id, period DESC");
        Map<String, Map<String, Object>> perfMap = new LinkedHashMap<>();
        for (Map<String, Object> r : jdbcTemplate.queryForList(ps.toString())) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("delivery", toBigDecimal(r.get("delivery_timely_rate")));
            m.put("pass", toBigDecimal(r.get("incoming_pass_rate")));
            m.put("score", toBigDecimal(r.get("score")));
            perfMap.put(String.valueOf(r.get("supplier_id")), m);
        }
        List<Map<String, Object>> result = new ArrayList<>(suppliers.size());
        for (Map<String, Object> s : suppliers) {
            String sid = String.valueOf(s.get("id"));
            Map<String, Object> perf = perfMap.get(sid);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("supplierId", sid);
            item.put("supplierName", s.get("name"));
            item.put("level", s.get("level"));
            item.put("deliveryRate", perf == null ? BigDecimal.ZERO : (BigDecimal) perf.get("delivery"));
            item.put("incomingPassRate", perf == null ? BigDecimal.ZERO : (BigDecimal) perf.get("pass"));
            item.put("lotCount", perf == null ? BigDecimal.ZERO : (BigDecimal) perf.get("score"));
            result.add(item);
        }
        return result;
    }

    // ==================== 物料看板:三聚合 ====================

    @Override
    public List<Map<String, Object>> materialPassRateDist(boolean keyOnly, String startYm, String endYm) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT part_no AS part_no, part_name AS part_name, ");
        sql.append("COUNT(*) AS total, COUNT(CASE WHEN iqc_pass = true THEN 1 END) AS pass ");
        sql.append("FROM ops.sqm_incoming_lot WHERE is_deleted = false ");
        if (keyOnly) {
            sql.append("AND is_key_part = true ");
        }
        appendYmFilter(sql, args, startYm, endYm);
        sql.append(orgFilter());
        sql.append("GROUP BY part_no, part_name");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());

        Map<String, List<Map<String, Object>>> bucketMaterials = new LinkedHashMap<>();
        for (String[] b : PASS_BUCKETS) bucketMaterials.put(b[0], new ArrayList<>());
        for (Map<String, Object> r : rows) {
            long total = toLong(r.get("total"));
            long pass = toLong(r.get("pass"));
            BigDecimal rate = passRate(pass, total);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("partNo", r.get("part_no"));
            item.put("partName", r.get("part_name"));
            item.put("passRate", rate);
            item.put("totalCount", total);
            item.put("failCount", total - pass);
            bucketMaterials.get(bucketOf(rate)).add(item);
        }
        List<Map<String, Object>> result = new ArrayList<>(PASS_BUCKETS.length);
        for (String[] b : PASS_BUCKETS) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("bucket", b[0]);
            item.put("label", b[1]);
            List<Map<String, Object>> list = bucketMaterials.get(b[0]);
            item.put("count", list.size());
            item.put("materials", list);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> materialPassRateTrend(boolean keyOnly, List<String> partNos, String startYm, String endYm) {
        String from = (startYm != null && !startYm.isBlank()) ? safeYm(startYm) : YearMonth.now().minusMonths(11).toString();
        String to = (endYm != null && !endYm.isBlank()) ? safeYm(endYm) : YearMonth.now().toString();
        boolean hasPartNos = partNos != null && !partNos.isEmpty();
        // 明确指定对比物料时拉全量(含非关键物料),否则按 keyOnly 过滤
        List<Map<String, Object>> rows = materialMonthlyRates(hasPartNos ? false : keyOnly, from, to);

        // 目标物料集合 = 默认集合(关键物料或合格率最低 8 个) + 追加对比物料
        Set<String> targets = new LinkedHashSet<>();
        if (keyOnly) {
            // 默认:全部关键物料(rows 可能为全量,按 is_key_part 过滤)
            for (Map<String, Object> r : rows) {
                if (toLong(r.get("is_key_part")) == 1) targets.add(String.valueOf(r.get("part_no")));
            }
        } else {
            // 默认:合格率最低的 8 个物料(避免线条过多,聚焦问题物料)
            Map<String, long[]> agg = new LinkedHashMap<>();
            for (Map<String, Object> r : rows) {
                String pn = String.valueOf(r.get("part_no"));
                long[] v = agg.computeIfAbsent(pn, k -> new long[2]);
                v[0] += toLong(r.get("total"));
                v[1] += toLong(r.get("pass"));
            }
            agg.entrySet().stream()
                    .sorted((a, b) -> passRate(a.getValue()[1], a.getValue()[0])
                            .compareTo(passRate(b.getValue()[1], b.getValue()[0])))
                    .limit(8)
                    .forEach(e -> targets.add(e.getKey()));
        }
        // 追加对比物料(含非关键物料)
        if (hasPartNos) {
            for (String p : partNos) {
                if (p != null && !p.isBlank()) targets.add(p);
            }
        }
        if (targets.isEmpty()) return new ArrayList<>();

        // 物料名称
        Map<String, String> names = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            names.putIfAbsent(String.valueOf(r.get("part_no")), String.valueOf(r.get("part_name")));
        }
        // 月度聚合: partNo -> ym -> [total, pass]
        Map<String, Map<String, long[]>> monthly = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            String pn = String.valueOf(r.get("part_no"));
            String ym = String.valueOf(r.get("ym"));
            long[] v = monthly.computeIfAbsent(pn, k -> new LinkedHashMap<>()).computeIfAbsent(ym, k -> new long[2]);
            v[0] += toLong(r.get("total"));
            v[1] += toLong(r.get("pass"));
        }
        // 遍历月份区间,缺月补 null
        List<Map<String, Object>> result = new ArrayList<>();
        YearMonth cursor = YearMonth.parse(from);
        YearMonth end = YearMonth.parse(to);
        while (!cursor.isAfter(end)) {
            String ym = cursor.toString();
            for (String pn : targets) {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("period", ym);
                point.put("partNo", pn);
                point.put("partName", names.getOrDefault(pn, pn));
                long[] v = monthly.getOrDefault(pn, new LinkedHashMap<>()).get(ym);
                point.put("passRate", v == null ? null : passRate(v[1], v[0]));
                result.add(point);
            }
            cursor = cursor.plusMonths(1);
        }
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> materialDeterioration(boolean keyOnly, String startYm, String endYm, int page, int size) {
        // "本月"严格取筛选截止月(默认当前月);物料在该月无来货则无"本月合格率/环比"可言,不参与劣化预警
        String curYm = (endYm != null && !endYm.isBlank()) ? safeYm(endYm) : YearMonth.now().toString();
        List<Map<String, Object>> rows = materialMonthlyRates(keyOnly, startYm, endYm);
        // partNo -> ym -> [total, pass]
        Map<String, Map<String, long[]>> monthly = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            String pn = String.valueOf(r.get("part_no"));
            String ym = String.valueOf(r.get("ym"));
            long[] v = monthly.computeIfAbsent(pn, k -> new LinkedHashMap<>()).computeIfAbsent(ym, k -> new long[2]);
            v[0] += toLong(r.get("total"));
            v[1] += toLong(r.get("pass"));
        }
        Map<String, String> names = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            names.putIfAbsent(String.valueOf(r.get("part_no")), String.valueOf(r.get("part_name")));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, long[]>> e : monthly.entrySet()) {
            String pn = e.getKey();
            Map<String, long[]> byYm = e.getValue();
            // 本月必须有来货,否则无"本月合格率"与"环比降幅"可言
            long[] cur = byYm.get(curYm);
            if (cur == null) continue;
            BigDecimal curRate = passRate(cur[1], cur[0]);
            // 上月 = 截止月前最近一个有来货的月份
            BigDecimal prevRate = null;
            BigDecimal dropPp = null;
            List<String> yms = new ArrayList<>(byYm.keySet());
            yms.sort(String::compareTo);
            for (int i = yms.size() - 1; i >= 0; i--) {
                if (yms.get(i).compareTo(curYm) < 0) {
                    long[] prev = byYm.get(yms.get(i));
                    prevRate = passRate(prev[1], prev[0]);
                    dropPp = prevRate.subtract(curRate);
                    break;
                }
            }
            // 判定:跌破 95% 警戒线或环比降幅 >=5pp 严重;环比降幅 >=2pp 预警
            String level = null;
            String reason = null;
            boolean severe = curRate.doubleValue() < 95;
            if (severe) {
                reason = "最新月合格率 " + curRate + "% 跌破 95% 警戒线";
            }
            if (dropPp != null && dropPp.doubleValue() >= 5) {
                severe = true;
                reason = "环比下降 " + dropPp + "pp";
            }
            if (severe) {
                level = "严重";
            } else if (dropPp != null && dropPp.doubleValue() >= 2) {
                level = "预警";
                reason = "环比下降 " + dropPp + "pp";
            }
            if (level == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("partNo", pn);
            item.put("partName", names.getOrDefault(pn, pn));
            item.put("prevRate", prevRate);
            item.put("curRate", curRate);
            item.put("dropPp", dropPp);
            item.put("level", level);
            item.put("reason", reason);
            result.add(item);
        }
        // 排序:严重优先,dropPp 降序
        result.sort((a, b) -> {
            int la = "严重".equals(a.get("level")) ? 0 : 1;
            int lb = "严重".equals(b.get("level")) ? 0 : 1;
            if (la != lb) return la - lb;
            BigDecimal da = (BigDecimal) a.get("dropPp");
            BigDecimal db = (BigDecimal) b.get("dropPp");
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return db.compareTo(da);
        });
        // 内存分页:page/size 1-based,越界返回空页
        int from = (page - 1) * size;
        if (from >= result.size()) {
            return new PageResult<>(new ArrayList<>(), result.size(), page, size);
        }
        int to = Math.min(from + size, result.size());
        return new PageResult<>(new ArrayList<>(result.subList(from, to)), result.size(), page, size);
    }

    @Override
    public List<Map<String, Object>> materialSearch(String keyword, int limit) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT part_no AS part_no, part_name AS part_name ");
        sql.append("FROM ops.sqm_incoming_lot WHERE is_deleted = false ");
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (part_no ILIKE ? OR part_name ILIKE ?) ");
            String like = "%" + keyword.replace("'", "''") + "%";
            args.add(like);
            args.add(like);
        }
        sql.append(orgFilter());
        sql.append("GROUP BY part_no, part_name ORDER BY part_no LIMIT ?");
        args.add(limit > 0 ? Math.min(limit, 50) : 20);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        // 转 camelCase,对齐前端 MaterialSearchItem(partNo/partName)
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("partNo", r.get("part_no"));
            item.put("partName", r.get("part_name"));
            result.add(item);
        }
        return result;
    }

    // ==================== 看板私有辅助 ====================

    /** 物料月度合格率聚合:一次 SQL 拉取,返回 [{part_no, part_name, ym, total, pass}] */
    private List<Map<String, Object>> materialMonthlyRates(boolean keyOnly, String startYm, String endYm) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT part_no AS part_no, part_name AS part_name, to_char(incoming_date,'YYYY-MM') AS ym, ");
        sql.append("COUNT(*) AS total, COUNT(CASE WHEN iqc_pass = true THEN 1 END) AS pass, ");
        sql.append("MAX(CASE WHEN is_key_part THEN 1 ELSE 0 END) AS is_key_part ");
        sql.append("FROM ops.sqm_incoming_lot WHERE is_deleted = false ");
        if (keyOnly) {
            sql.append("AND is_key_part = true ");
        }
        appendYmFilter(sql, args, startYm, endYm);
        sql.append(orgFilter());
        sql.append("GROUP BY part_no, part_name, ym");
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    /** YYYY-MM 月份范围过滤(参数绑定,ym 经 safeYm 清洗) */
    private void appendYmFilter(StringBuilder sql, List<Object> args, String startYm, String endYm) {
        if (startYm != null && !startYm.isBlank()) {
            sql.append("AND to_char(incoming_date,'YYYY-MM') >= ? ");
            args.add(safeYm(startYm));
        }
        if (endYm != null && !endYm.isBlank()) {
            sql.append("AND to_char(incoming_date,'YYYY-MM') <= ? ");
            args.add(safeYm(endYm));
        }
    }

    private List<Map<String, Object>> emptyDist() {
        List<Map<String, Object>> result = new ArrayList<>(PASS_BUCKETS.length);
        for (String[] b : PASS_BUCKETS) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("bucket", b[0]);
            item.put("label", b[1]);
            item.put("count", 0);
            item.put("suppliers", new ArrayList<>());
            result.add(item);
        }
        return result;
    }

    /** 符合 level/keyword 筛选的供应商(id,name,level,observe_flag) */
    private List<Map<String, Object>> listFilteredSuppliers(String level, String keyword) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT id, name, level, observe_flag FROM ops.sqm_supplier WHERE is_deleted = false ");
        if (level != null && !level.isBlank()) {
            sql.append("AND level = ? ");
            args.add(level);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND name ILIKE ? ");
            args.add("%" + keyword.replace("'", "''") + "%");
        }
        sql.append(orgFilter()).append(" ORDER BY name");
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    /** 异常数:supplier_id -> count(按月份范围) */
    private Map<String, Long> loadAbnormalCount(String startYm, String endYm) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT supplier_id, COUNT(*) AS cnt FROM ops.sqm_incoming_abnormal WHERE is_deleted = false ");
        if (startYm != null && !startYm.isBlank()) {
            sql.append("AND to_char(occur_date,'YYYY-MM') >= ? ");
            args.add(safeYm(startYm));
        }
        if (endYm != null && !endYm.isBlank()) {
            sql.append("AND to_char(occur_date,'YYYY-MM') <= ? ");
            args.add(safeYm(endYm));
        }
        sql.append(orgFilter()).append(" GROUP BY supplier_id");
        Map<String, Long> map = new LinkedHashMap<>();
        for (Map<String, Object> r : jdbcTemplate.queryForList(sql.toString(), args.toArray())) {
            map.put(String.valueOf(r.get("supplier_id")), toLong(r.get("cnt")));
        }
        return map;
    }

    /** 实时算某供应商全量 iqc_pass 率(无绩效时兜底) */
    private BigDecimal calcRealtimePassRate(String supplierId) {
        return calcRealtimePassRate(supplierId, null);
    }

    /** 实时算某供应商指定月份 iqc_pass 率;ym 为 null 时算全量 */
    private BigDecimal calcRealtimePassRate(String supplierId, String ym) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) AS total, COUNT(CASE WHEN iqc_pass = true THEN 1 END) AS pass ");
        sql.append("FROM ops.sqm_incoming_lot WHERE is_deleted = false AND supplier_id = '").append(supplierId).append("' ");
        if (ym != null && !ym.isBlank()) {
            sql.append("AND to_char(incoming_date,'YYYY-MM') = '").append(safeYm(ym)).append("' ");
        }
        sql.append(orgFilter());
        Map<String, Object> row = jdbcTemplate.queryForMap(sql.toString());
        long total = toLong(row.get("total"));
        long pass = toLong(row.get("pass"));
        return passRate(pass, total);
    }

    private Map<String, String> supplierNames(List<String> ids) {
        if (ids == null || ids.isEmpty()) return new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, name FROM ops.sqm_supplier WHERE is_deleted = false AND id IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("'").append(ids.get(i)).append("'");
        }
        sql.append(") ").append(orgFilter());
        Map<String, String> map = new LinkedHashMap<>();
        for (Map<String, Object> r : jdbcTemplate.queryForList(sql.toString())) {
            map.put(String.valueOf(r.get("id")), String.valueOf(r.get("name")));
        }
        return map;
    }

    private String bucketOf(BigDecimal rate) {
        double v = rate == null ? -1 : rate.doubleValue();
        if (v >= 100) return "100";
        if (v >= 90) return "90_100";
        if (v >= 80) return "80_90";
        if (v >= 70) return "70_80";
        return "lt70";
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private int toInt(Object o) {
        return (int) toLong(o);
    }

    /** YYYY-MM 安全化(防注入,仅保留数字与 -) */
    private String safeYm(String ym) {
        if (ym == null) return "";
        return ym.replaceAll("[^0-9\\-]", "");
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
