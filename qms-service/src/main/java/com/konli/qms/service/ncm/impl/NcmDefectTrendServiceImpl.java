package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.ncm.entity.NcmDefectTrendReport;
import com.konli.qms.domain.ncm.entity.NcmDefectTrendRule;
import com.konli.qms.domain.ncm.mapper.NcmDefectTrendReportMapper;
import com.konli.qms.domain.ncm.mapper.NcmDefectTrendRuleMapper;
import com.konli.qms.service.ncm.NcmDefectTrendService;
import com.konli.qms.service.ncm.dto.TrendPoint;
import com.konli.qms.service.ncm.dto.TrendRealtimeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 不良趋势报表服务:实时聚合 + 环比/同比 + 可配置趋势恶化判定 + 定时生成。
 *
 * <p>口径与 {@code NcmDefectRecordServiceImpl} 保持一致:
 * 不良率 = Σ不良数 / Σ批次数 × 100%;环比 = 当前周期 vs 上一等长周期;
 * 同比 = 当前周期 vs 去年同周期。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NcmDefectTrendServiceImpl implements NcmDefectTrendService {

    private final JdbcTemplate jdbcTemplate;
    private final NcmDefectTrendRuleMapper ruleMapper;
    private final NcmDefectTrendReportMapper reportMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter F_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter F_MON = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter F_WEEK = DateTimeFormatter.ofPattern("YYYY-'W'ww");

    // ───────────────────────── 规则 ─────────────────────────

    @Override
    public NcmDefectTrendRule getRule() {
        String orgId = resolveOrgId();
        NcmDefectTrendRule orgRule = null;
        if (orgId != null && !orgId.isBlank()) {
            orgRule = ruleMapper.selectOne(new LambdaQueryWrapper<NcmDefectTrendRule>()
                    .eq(NcmDefectTrendRule::getOrgId, orgId)
                    .eq(NcmDefectTrendRule::getIsDeleted, false));
        }
        if (orgRule != null) return orgRule;
        return ruleMapper.selectOne(new LambdaQueryWrapper<NcmDefectTrendRule>()
                .isNull(NcmDefectTrendRule::getOrgId)
                .eq(NcmDefectTrendRule::getIsDeleted, false));
    }

    @Override
    public NcmDefectTrendRule saveRule(NcmDefectTrendRule rule) {
        String orgId = resolveOrgId();
        if (orgId != null && !orgId.isBlank()) {
            rule.setOrgId(orgId);
            NcmDefectTrendRule existing = ruleMapper.selectOne(new LambdaQueryWrapper<NcmDefectTrendRule>()
                    .eq(NcmDefectTrendRule::getOrgId, orgId)
                    .eq(NcmDefectTrendRule::getIsDeleted, false));
            if (existing != null) {
                rule.setId(existing.getId());
                rule.setVersion(existing.getVersion());
                rule.setIsDeleted(false);
                ruleMapper.updateById(rule);
                return rule;
            }
        } else {
            rule.setOrgId(null);
            NcmDefectTrendRule existing = ruleMapper.selectOne(new LambdaQueryWrapper<NcmDefectTrendRule>()
                    .isNull(NcmDefectTrendRule::getOrgId)
                    .eq(NcmDefectTrendRule::getIsDeleted, false));
            if (existing != null) {
                rule.setId(existing.getId());
                ruleMapper.updateById(rule);
                return rule;
            }
        }
        ruleMapper.insert(rule);
        return rule;
    }

    // ───────────────────────── 实时聚合 ─────────────────────────

    @Override
    public TrendRealtimeResult realtime(String granularity, String productModel, String start, String end) {
        granularity = normalizeGranularity(granularity);
        LocalDate s = (start == null || start.isBlank()) ? LocalDate.now().minusDays(89) : LocalDate.parse(start, F_DAY);
        LocalDate e = (end == null || end.isBlank()) ? LocalDate.now() : LocalDate.parse(end, F_DAY);
        LocalDateTime startTs = s.atStartOfDay();
        LocalDateTime endTs = e.atTime(23, 59, 59);

        NcmDefectTrendRule rule = getRule();
        int consecutiveDays = rule.getConsecutiveDays() != null ? rule.getConsecutiveDays() : 3;
        boolean use2sigma = rule.getUseMeanPlus2sigma() != null ? rule.getUseMeanPlus2sigma() : true;
        double k = rule.getSigmaMultiplier() != null ? rule.getSigmaMultiplier().doubleValue() : 2.0;
        int baselineDays = rule.getBaselineDays() != null ? rule.getBaselineDays() : 30;

        List<PeriodAgg> cur = aggregate(granularity, productModel, startTs, endTs, 0);
        Map<String, Double> yoyMap = aggregate(granularity, productModel, startTs.minusYears(1), endTs.minusYears(1), 0)
                .stream().collect(Collectors.toMap(PeriodAgg::period, a -> rate(a), (a, b) -> a));

        List<TrendPoint> points = new ArrayList<>();
        for (int i = 0; i < cur.size(); i++) {
            PeriodAgg a = cur.get(i);
            double rate = rate(a);
            TrendPoint p = new TrendPoint();
            p.setPeriod(a.period);
            p.setDefectCount((int) a.dc);
            p.setBatchTotal((int) a.bt);
            p.setRecordCount((int) a.cnt);
            p.setDefectRate(round2(rate));

            // 环比:上一等长周期(序列中前一个点)
            if (i > 0) {
                double prev = rate(cur.get(i - 1));
                p.setMomPct(prev > 0 ? round2((rate - prev) / prev * 100.0) : null);
            }
            // 同比:去年同周期
            Double yoyRate = yoyMap.get(a.period);
            if (yoyRate != null && yoyRate > 0) {
                p.setYoyPct(round2((rate - yoyRate) / yoyRate * 100.0));
            }

            // 连续上升期数
            int streak = 0;
            for (int j = i; j > 0; j--) {
                if (rate(cur.get(j)) > rate(cur.get(j - 1))) streak++;
                else break;
            }
            p.setRisingStreak(streak);

            // 超历史均值 + kσ(基线取当前点之前的窗口)
            boolean exceed = false;
            List<Double> hist = new ArrayList<>();
            int from = Math.max(0, i - baselineDays);
            for (int j = from; j < i; j++) hist.add(rate(cur.get(j)));
            if (!hist.isEmpty()) {
                double mean = hist.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double variance = hist.stream().mapToDouble(x -> (x - mean) * (x - mean)).average().orElse(0);
                double std = Math.sqrt(variance);
                exceed = std > 0 && rate > mean + k * std;
            }
            p.setExceedMean2Sigma(exceed);

            boolean deterioration = streak >= consecutiveDays || (use2sigma && exceed);
            p.setDeterioration(deterioration);
            if (deterioration) {
                List<String> reasons = new ArrayList<>();
                if (streak >= consecutiveDays) reasons.add("连续" + streak + "期上升");
                if (exceed) reasons.add("超历史均值+" + k + "σ");
                p.setReason(String.join(";", reasons));
            }
            points.add(p);
        }

        TrendRealtimeResult result = new TrendRealtimeResult();
        result.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.setRule(rule);
        result.setPoints(points);
        result.setSummary(buildSummary(productModel, granularity, points));
        return result;
    }

    private Map<String, Object> buildSummary(String productModel, String granularity, List<TrendPoint> points) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("productModel", productModel == null ? "全部产品" : productModel);
        summary.put("granularity", granularity);
        summary.put("periodCount", points.size());
        summary.put("deteriorationCount", points.stream().filter(TrendPoint::isDeterioration).count());
        Optional<TrendPoint> worst = points.stream().max(Comparator.comparing(TrendPoint::getDefectRate));
        worst.ifPresent(p -> {
            summary.put("peakPeriod", p.getPeriod());
            summary.put("peakRate", p.getDefectRate());
        });
        return summary;
    }

    // ───────────────────────── 定时生成 ─────────────────────────

    @Override
    @Scheduled(cron = "0 15 2 * * ?")
    public void scheduledGenerate() {
        LocalDate today = LocalDate.now();
        // 日报表:昨日
        generateForPeriod("day", today.minusDays(1).atStartOfDay(), today.minusDays(1).atTime(23, 59, 59), today.minusDays(1).format(F_DAY));
        // 周报表:周一补生成上周
        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
            LocalDate wkStart = today.minusDays(7).with(DayOfWeek.MONDAY);
            generateForPeriod("week", wkStart.atStartOfDay(), wkStart.plusDays(6).atTime(23, 59, 59), wkStart.format(F_WEEK));
        }
        // 月报表:每月 1 号补生成上月
        if (today.getDayOfMonth() == 1) {
            LocalDate mStart = today.minusMonths(1).withDayOfMonth(1);
            LocalDate mEnd = today.withDayOfMonth(1).minusDays(1);
            generateForPeriod("month", mStart.atStartOfDay(), mEnd.atTime(23, 59, 59), mStart.format(F_MON));
        }
    }

    @Override
    public void generateForPeriod(String granularity, LocalDateTime start, LocalDateTime end, String periodValue) {
        List<String> products = jdbcTemplate.queryForList(
                "SELECT DISTINCT product_model FROM ops.ncm_defect_record r WHERE r.is_deleted = false "
                        + orgFilter() + " AND r.occurred_at >= ? AND r.occurred_at <= ? AND r.product_model IS NOT NULL",
                String.class, start, end);
        for (String product : products) {
            try {
                generateOne(granularity, product, start, end, periodValue);
            } catch (Exception ex) {
                log.warn("趋势报表生成失败 product={} period={} : {}", product, periodValue, ex.getMessage());
            }
        }
    }

    /** 手动触发:生成"当前"周期(今日/本周/本月)。 */
    public void generateCurrent(String granularity) {
        granularity = normalizeGranularity(granularity);
        LocalDate today = LocalDate.now();
        LocalDateTime start;
        LocalDateTime end;
        String periodValue;
        if ("week".equals(granularity)) {
            LocalDate ws = today.with(DayOfWeek.MONDAY);
            start = ws.atStartOfDay();
            end = ws.plusDays(6).atTime(23, 59, 59);
            periodValue = ws.format(F_WEEK);
        } else if ("month".equals(granularity)) {
            LocalDate ms = today.withDayOfMonth(1);
            start = ms.atStartOfDay();
            end = today.withDayOfMonth(1).plusMonths(1).minusDays(1).atTime(23, 59, 59);
            periodValue = ms.format(F_MON);
        } else {
            start = today.atStartOfDay();
            end = today.atTime(23, 59, 59);
            periodValue = today.format(F_DAY);
        }
        generateForPeriod(granularity, start, end, periodValue);
    }

    private void generateOne(String granularity, String product, LocalDateTime start, LocalDateTime end, String periodValue) {
        TrendRealtimeResult result = realtime(granularity, product,
                start.toLocalDate().format(F_DAY), end.toLocalDate().format(F_DAY));
        NcmDefectTrendRule rule = result.getRule();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("productModel", product);
        summary.put("granularity", granularity);
        summary.put("periodValue", periodValue);
        summary.put("points", result.getPoints());
        summary.put("summary", result.getSummary());

        // upsert:先软删旧快照,再插入
        NcmDefectTrendReport softDel = new NcmDefectTrendReport();
        softDel.setIsDeleted(true);
        reportMapper.update(softDel, new LambdaQueryWrapper<NcmDefectTrendReport>()
                .eq(NcmDefectTrendReport::getOrgId, resolveOrgId())
                .eq(NcmDefectTrendReport::getProductModel, product)
                .eq(NcmDefectTrendReport::getGranularity, granularity)
                .eq(NcmDefectTrendReport::getPeriodValue, periodValue));

        NcmDefectTrendReport report = new NcmDefectTrendReport();
        report.setOrgId(resolveOrgId());
        report.setProductModel(product);
        report.setGranularity(granularity);
        report.setPeriodValue(periodValue);
        try {
            report.setSummaryJson(objectMapper.writeValueAsString(summary));
            report.setRuleSnapshot(objectMapper.writeValueAsString(rule));
        } catch (Exception ex) {
            log.warn("趋势报表 JSON 序列化失败: {}", ex.getMessage());
        }
        report.setGeneratedAt(LocalDateTime.now());
        reportMapper.insert(report);
    }

    @Override
    public List<NcmDefectTrendReport> listReports(String productModel, String granularity, int page, int size) {
        LambdaQueryWrapper<NcmDefectTrendReport> q = new LambdaQueryWrapper<NcmDefectTrendReport>()
                .eq(NcmDefectTrendReport::getIsDeleted, false)
                .orderByDesc(NcmDefectTrendReport::getGeneratedAt);
        if (productModel != null && !productModel.isBlank()) q.eq(NcmDefectTrendReport::getProductModel, productModel);
        if (granularity != null && !granularity.isBlank()) q.eq(NcmDefectTrendReport::getGranularity, granularity);
        q.last(" LIMIT " + size + " OFFSET " + (Math.max(0, page - 1) * size));
        return reportMapper.selectList(q);
    }

    // ───────────────────────── 工具 ─────────────────────────

    private List<PeriodAgg> aggregate(String granularity, String productModel, LocalDateTime start, LocalDateTime end, int shiftYears) {
        String part = "day".equals(granularity) ? "day" : "week".equals(granularity) ? "week" : "month";
        String fmt = "day".equals(granularity) ? "YYYY-MM-DD" : "week".equals(granularity) ? "IYYY-IW" : "YYYY-MM";
        String periodExpr = "to_char(date_trunc('" + part + "', r.occurred_at), '" + fmt + "')";
        StringBuilder sql = new StringBuilder("SELECT ")
                .append(periodExpr).append(" AS period, COALESCE(SUM(r.defect_count),0) AS dc, ")
                .append("COALESCE(SUM(r.batch_total),0) AS bt, COUNT(*) AS cnt ")
                .append("FROM ops.ncm_defect_record r WHERE r.is_deleted = false ")
                .append(orgFilter())
                .append(" AND r.occurred_at >= ? AND r.occurred_at <= ? ");
        List<Object> args = new ArrayList<>(List.of(start, end));
        if (productModel != null && !productModel.isBlank()) {
            sql.append(" AND r.product_model = ?");
            args.add(productModel);
        }
        sql.append(" GROUP BY ").append(periodExpr).append(" ORDER BY period");
        return jdbcTemplate.query(sql.toString(), (rs, rn) -> new PeriodAgg(
                rs.getString("period"), rs.getLong("dc"), rs.getLong("bt"), rs.getLong("cnt")), args.toArray());
    }

    private double rate(PeriodAgg a) {
        return a.bt > 0 ? a.dc * 100.0 / a.bt : 0.0;
    }

    private String normalizeGranularity(String g) {
        if ("week".equalsIgnoreCase(g) || "w".equalsIgnoreCase(g)) return "week";
        if ("month".equalsIgnoreCase(g) || "m".equalsIgnoreCase(g)) return "month";
        return "day";
    }

    private String orgFilter() {
        String orgId = resolveOrgId();
        if (orgId == null || orgId.isBlank()) return "";
        return " AND r.org_id = '" + orgId + "'";
    }

    private String resolveOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null || u.dataScope() == null) return null;
        if ("all".equals(u.dataScope())) return null;
        return u.orgId();
    }

    private double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private record PeriodAgg(String period, long dc, long bt, long cnt) {
    }
}
