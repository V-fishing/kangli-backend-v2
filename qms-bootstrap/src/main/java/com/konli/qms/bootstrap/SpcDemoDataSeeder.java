package com.konli.qms.bootstrap;

import com.konli.qms.service.spc.SpcCapabilityService;
import com.konli.qms.service.spc.SpcControlLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Random;
import java.util.UUID;

/**
 * SPC 演示数据种子(dev profile,幂等):清空并重灌一套干净、全覆盖的演示数据,
 * 使直方图 / CPK / R 控制图 / Xbar 控制图均可完整渲染。
 *
 * <p>每个参数生成 40 个子组(每组 5 测值),随后调用线上 calc 服务计算控制限(激活基线)
 * 与过程能力 CPK 快照,保证口径与线上一致。另在 1 个参数注入 2 个历史异常子组,
 * 用于演示控制图判异标注与告警联动。</p>
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SpcDemoDataSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final SpcControlLimitService controlLimitService;
    private final SpcCapabilityService capabilityService;

    private static final int SUBGROUPS = 40;
    private static final int SUBGROUP_SIZE = 5;
    private static final Random RAND = new Random(20240726L);

    /** 参数定义:name, process, unit, target, processSigma, decimalScale。 */
    private record Pdef(String name, String process, String unit, double target, double sigma, int scale) {}

    @Override
    public void run(ApplicationArguments args) {
        String orgId = getDemoOrgId();
        log.info("[SPC 种子] 开始重建演示数据, orgId={}", orgId);

        // 1. 清空该组织的演示数据(保留其他组织数据)
        String[] deletes = {
                "DELETE FROM ops.spc_notify_record WHERE org_id = ?",
                "DELETE FROM ops.spc_alarm WHERE org_id = ?",
                "DELETE FROM ops.spc_collect_task WHERE org_id = ?",
                "DELETE FROM ops.spc_import_log WHERE org_id = ?",
                "DELETE FROM ops.spc_capability WHERE org_id = ?",
                "DELETE FROM ops.spc_control_limit WHERE org_id = ?",
                "DELETE FROM ops.spc_measurement WHERE org_id = ?",
                "DELETE FROM ops.spc_subgroup WHERE org_id = ?",
                "DELETE FROM ops.spc_param WHERE org_id = ?"
        };
        for (String d : deletes) jdbcTemplate.update(d, orgId);

        Pdef[] defs = {
                new Pdef("主轴长度", "CNC-01", "mm", 50.000, 0.008, 3),
                new Pdef("单体重", "ASSY-02", "g", 120.00, 0.05, 2),
                new Pdef("回流焊炉温", "SMT-03", "℃", 230.0, 1.2, 1),
                new Pdef("供电电压", "TEST-04", "V", 12.00, 0.03, 2),
                new Pdef("锁紧扭矩", "ASSY-05", "N·m", 8.50, 0.04, 2)
        };

        LocalDate base = LocalDate.now().minusDays(SUBGROUPS - 1);
        for (int pi = 0; pi < defs.length; pi++) {
            Pdef d = defs[pi];
            String paramId = UUID.randomUUID().toString();
            double hw = 4.2 * d.sigma();               // 规格半宽 → CPK≈1.4
            double usl = round(d.target() + hw, d.scale());
            double lsl = round(d.target() - hw, d.scale());
            String specText = lsl + " ~ " + usl + " " + d.unit();

            jdbcTemplate.update(
                    "INSERT INTO ops.spc_param (id, org_id, param_name, proc_name, unit, spec_text, "
                            + "spec_lower, spec_upper, target_value, subgroup_size, collect_freq, is_active, "
                            + "chart_type, sigma_method, sigma_k, created_at, created_by, is_deleted, version) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,'true','Xbar-R','within',3,now(),null,false,0)",
                    paramId, orgId, d.name(), d.process(), d.unit(), specText,
                    BigDecimal.valueOf(lsl), BigDecimal.valueOf(usl), BigDecimal.valueOf(d.target()),
                    SUBGROUP_SIZE, "每日");

            // 2. 生成子组 + 测量值
            for (int i = 0; i < SUBGROUPS; i++) {
                LocalDate day = base.plusDays(i);
                LocalDateTime t = day.atTime(8, 0).plusMinutes(i * 30L);
                boolean outlier = (pi == 0 && (i == 1 || i == 2)); // 仅首参数最早期 2 个子组判异
                double shift = outlier ? 2.0 * d.sigma() : 0.0;

                String subgroupId = UUID.randomUUID().toString();
                double sum = 0;
                double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
                for (int j = 0; j < SUBGROUP_SIZE; j++) {
                    double v = d.target() + shift + RAND.nextGaussian() * d.sigma();
                    v = round(v, d.scale());
                    sum += v;
                    min = Math.min(min, v);
                    max = Math.max(max, v);
                    jdbcTemplate.update(
                            "INSERT INTO ops.spc_measurement (id, org_id, subgroup_id, subgroup_time, seq, value, created_at, created_by) "
                                    + "VALUES (?,?,?,?,?,?,now(),null)",
                            UUID.randomUUID().toString(), orgId, subgroupId, t, j + 1, BigDecimal.valueOf(v));
                }
                double xbar = round(sum / SUBGROUP_SIZE, d.scale());
                double rangeR = round(max - min, d.scale());
                jdbcTemplate.update(
                        "INSERT INTO ops.spc_subgroup (id, org_id, param_id, subgroup_no, subgroup_time, n, xbar, range_r, "
                                + "judge, is_outlier, outlier_rule, data_source, created_at, created_by) "
                                + "VALUES (?,?,?,?,?,?,?,?,'正常',?,?, 'manual', now(),null)",
                        subgroupId, orgId, paramId, i + 1, t, SUBGROUP_SIZE, BigDecimal.valueOf(xbar), BigDecimal.valueOf(rangeR),
                        outlier, outlier ? "①" : null);

                if (outlier) {
                    jdbcTemplate.update(
                            "INSERT INTO ops.spc_alarm (id, org_id, code, param_id, param_name, triggered_rule, level, "
                                    + "alarm_time, status, created_at, is_deleted, version) "
                                    + "VALUES (?,?,?,?,?,?,'中',?,'已关闭',now(),false,0)",
                            UUID.randomUUID().toString(), orgId,
                            "SPC-DEMO-" + pi + "-" + i, paramId, d.name(), "①", t);
                }
            }

            // 3. 计算控制限(激活基线)与过程能力 CPK
            try {
                controlLimitService.calc(paramId);
                capabilityService.calc(paramId, "OVERALL", YearMonth.now().toString());
                log.info("[SPC 种子] 参数已就绪: {} ({})", d.name(), paramId);
            } catch (Exception ex) {
                log.warn("[SPC 种子] 计算失败 param={} : {}", d.name(), ex.getMessage());
            }
        }
        log.info("[SPC 种子] 完成,共 {} 个参数, 每参数 {} 子组", defs.length, SUBGROUPS);
    }

    private String getDemoOrgId() {
        String id = jdbcTemplate.queryForObject(
                "SELECT id FROM ops.sys_org WHERE org_code = 'MZ' LIMIT 1", String.class);
        if (id != null) return id;
        // 兜底:演示组织不存在时自建(与 DataInitializer 幂等共存)
        id = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO ops.sys_org (id, org_code, org_name, org_type, status, created_at, is_deleted, version) "
                        + "VALUES (?, 'MZ', '美滋(演示)', '工厂', 1, now(), false, 0)", id);
        return id;
    }

    private double round(double v, int scale) {
        BigDecimal b = BigDecimal.valueOf(v).setScale(scale, java.math.RoundingMode.HALF_UP);
        return b.doubleValue();
    }
}
