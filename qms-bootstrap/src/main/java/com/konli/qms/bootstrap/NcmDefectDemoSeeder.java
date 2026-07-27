package com.konli.qms.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * NCM 不良演示数据种子(dev profile,幂等):灌入约 90 天、跨 4 个产品的不良记录,
 * 使趋势报表开箱即用。其中 MX-200 构造近 10 天连续上升通道,并在某天制造超历史均值+2σ 尖峰,
 * 用于直观演示趋势恶化告警。
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class NcmDefectDemoSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private static final Random RAND = new Random(20260726L);
    private static final String OPERATOR = "11111111-1111-1111-1111-111111111111";
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String[] PRODUCTS = {"MX-200", "MX-300", "MX-400", "MX-500"};
    private static final double[] BASE = {1.2, 1.8, 1.5, 2.2}; // 基线不良率(%)
    private static final String[] DICTS = {"A01", "A02", "A03"};
    private static final String[] PROCS = {"SMT-01", "ASSY-02", "CNC-03", "TEST-04"};
    private static final String[] SEVS = {"轻微", "一般", "严重"};

    @Override
    public void run(ApplicationArguments args) {
        String orgId = getDemoOrgId();

        // 每次 dev 启动重建演示产品(MX-200/300/400/500)不良记录,保证趋势报表开箱即用且数据完整确定。
        jdbcTemplate.update(
                "DELETE FROM ops.ncm_defect_record WHERE org_id = ? AND product_model IN (?,?,?,?)",
                orgId, PRODUCTS[0], PRODUCTS[1], PRODUCTS[2], PRODUCTS[3]);

        ensureDefectDicts(orgId);

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(89);
        int days = 90;
        List<Object[]> batch = new ArrayList<>();
        int counter = 0;

        for (int p = 0; p < PRODUCTS.length; p++) {
            for (int i = 0; i < days; i++) {
                LocalDate day = start.plusDays(i);
                double rate = dailyRate(p, i);
                int batchTotal = 800 + RAND.nextInt(400); // 800~1199
                int dayDefect = Math.max(1, (int) Math.round(rate / 100.0 * batchTotal));
                int recCount = 1 + RAND.nextInt(3); // 1~3 条
                int per = dayDefect / recCount;
                for (int r = 0; r < recCount; r++) {
                    int dc = (r == recCount - 1) ? (dayDefect - per * (recCount - 1)) : per;
                    dc = Math.max(1, dc);
                    double dr = round2(dc * 100.0 / batchTotal);
                    String defectNo = "DR-" + day.format(D) + "-" + p + "-" + r + "-" + (counter++);
                    String woNo = "WO-" + day.format(D) + "-" + PRODUCTS[p] + "-" + r;
                    String dict = DICTS[RAND.nextInt(DICTS.length)];
                    String proc = PROCS[RAND.nextInt(PROCS.length)];
                    String sev = dict.equals("A02") ? "严重" : SEVS[RAND.nextInt(SEVS.length)];
                    LocalDateTime occurred = day.atTime(8 + RAND.nextInt(10), RAND.nextInt(60));
                    batch.add(new Object[]{
                            UUID.randomUUID().toString(), orgId, defectNo, woNo, proc, dict, sev,
                            dc, batchTotal, dr, PRODUCTS[p], OPERATOR, occurred
                    });
                }
            }
        }

        String sql = "INSERT INTO ops.ncm_defect_record "
                + "(id, org_id, defect_no, wo_no, process_code, defect_dict_code, severity, "
                + "defect_count, batch_total, defect_rate, product_model, operator_id, source, occurred_at, created_at, is_deleted, version) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,'手动',?,now(),false,0)";
        jdbcTemplate.batchUpdate(sql, batch);
        log.info("[NCM 种子] 灌入不良记录 {} 条(近 90 天, 跨 {} 产品)", batch.size(), PRODUCTS.length);
    }

    /** 日不良率(%):MX-200 在最后 10 天连续上升,第 88 天超均值+2σ 尖峰。 */
    private double dailyRate(int p, int i) {
        double rate = BASE[p] * (1 + 0.1 * Math.sin(i / 7.0));
        if (p == 0 && i >= 80) {
            rate = BASE[0] * (1.0 + (i - 80) * 0.30);
            if (i == 88) rate = BASE[0] * 5.0; // 尖峰 ~6%
        }
        return round2(rate);
    }

    private void ensureDefectDicts(String orgId) {
        Object[][] dicts = {
                {"A01", "刮伤", "外观", "轻微"},
                {"A02", "虚焊", "焊接", "严重"},
                {"A03", "漏装", "装配", "一般"}
        };
        for (Object[] d : dicts) {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM ops.ncm_defect_dict WHERE code = ?", Integer.class, d[0]);
            if (cnt != null && cnt > 0) continue;
            jdbcTemplate.update(
                    "INSERT INTO ops.ncm_defect_dict (id, org_id, code, name, category, level, status, created_at, is_deleted, version) "
                            + "VALUES (?,?,?,?,?,?,'启用',now(),false,0)",
                    UUID.randomUUID().toString(), orgId, d[0], d[1], d[2], d[3]);
        }
    }

    private String getDemoOrgId() {
        String id = jdbcTemplate.queryForObject(
                "SELECT id FROM ops.sys_org WHERE org_code = 'MZ' LIMIT 1", String.class);
        if (id != null) return id;
        id = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO ops.sys_org (id, org_code, org_name, org_type, status, created_at, is_deleted, version) "
                        + "VALUES (?, 'MZ', '美滋(演示)', '工厂', 1, now(), false, 0)", id);
        return id;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
