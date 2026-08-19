package com.konli.qms.bootstrap;

import com.konli.qms.service.spc.SpcCapabilityService;
import com.konli.qms.service.spc.SpcControlLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
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
@Order(10)
@RequiredArgsConstructor
public class SpcDemoDataSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final SpcControlLimitService controlLimitService;
    private final SpcCapabilityService capabilityService;

    private static final int SUBGROUPS = 40;
    private static final int SUBGROUP_SIZE = 5;
    /** 每个参数前 FIRST_N 组作为「首件验证点」(stage=FIRST),其余为「量产监控点」(stage=ROUTINE);
     *  使首件入口(stage=FIRST)与量产入口(stage=ROUTINE)两种视图均有数据,stage 维度真正落地。 */
    private static final int FIRST_N = 5;
    private static final Random RAND = new Random(20240726L);

    /** 参数定义:name, process(工位), unit, target, processSigma, decimalScale, cat(所属工序父层), product(关联产品名), partNo(件号)。 */
    private record Pdef(String name, String process, String unit, double target, double sigma, int scale, String cat, String product, String partNo) {}

    /** 演示采集任务轮询模式,使管理页可见多种采集模式。 */
    private static final String[] COLLECT_MODES = { "AUTO", "MANUAL", "OPC", "FILE", "MES" };

    @Override
    public void run(ApplicationArguments args) {
        String orgId = getDemoOrgId();
        // 取该组织下一名启用用户作为演示子组的录入人(使「谁录的」字段有值可追溯)
        String operatorId;
        try {
            operatorId = jdbcTemplate.queryForObject(
                    "SELECT id FROM ops.sys_user WHERE org_id = ? AND status = '启用' LIMIT 1", String.class, orgId);
        } catch (Exception e) {
            operatorId = null;
        }
        List<String> supplierIds = ensureDemoSuppliers(orgId);
        // 先确保默认工序主数据(装配/焊接/检测/系统),供参数绑定父层
        Map<String, String> processMap = ensureDefaultProcesses(orgId);
        log.info("[SPC 种子] 开始重建演示数据, orgId={}, 演示供应商数={}", orgId, supplierIds.size());

        // 1. 清空该组织的演示数据(保留其他组织数据)。
        //    注意:仅清理本 Seeder 自己产生的演示参数(按演示参数名精确匹配),
        //    绝不动用户手动创建的 MANUAL 参数、也不动用户由 FIA 首件任务派生的参数,避免重启后用户数据丢失。
        String demoParamIds = "SELECT id FROM ops.spc_param WHERE org_id = ? AND param_name IN "
                + "('主轴长度','单体重','回流焊炉温','供电电压','锁紧扭矩')";
        // 只清理本 Seeder 自己产生的演示参数的下游数据(按演示参数 id 精确匹配)。
        // 绝不能按 org_id 全删:否则会误删用户由 FIA 首件任务派生的参数(如 关键尺寸/性能测试/外观)
        // 产生的告警、测量值、能力快照、控制限、采集任务、导入日志等真实数据(历史事故:告警列表被清空)。
        String demoSubgroupIds = "SELECT id FROM ops.spc_subgroup WHERE param_id IN (" + demoParamIds + ")";
        String demoAlarmIds = "SELECT id FROM ops.spc_alarm WHERE param_id IN (" + demoParamIds + ")";
        String[] deletes = {
                "DELETE FROM ops.spc_notify_record WHERE alarm_id IN (" + demoAlarmIds + ")",
                "DELETE FROM ops.spc_alarm WHERE param_id IN (" + demoParamIds + ")",
                "DELETE FROM ops.spc_collect_task WHERE param_id IN (" + demoParamIds + ")",
                "DELETE FROM ops.spc_import_log WHERE param_id IN (" + demoParamIds + ")",
                "DELETE FROM ops.spc_capability WHERE param_id IN (" + demoParamIds + ")",
                "DELETE FROM ops.spc_control_limit WHERE param_id IN (" + demoParamIds + ")",
                "DELETE FROM ops.spc_measurement WHERE subgroup_id IN (" + demoSubgroupIds + ")",
                "DELETE FROM ops.spc_sample_task WHERE param_id IN (" + demoParamIds + ")",
                "DELETE FROM ops.spc_subgroup WHERE param_id IN (" + demoParamIds + ")",
                "DELETE FROM ops.spc_param_product WHERE param_id IN (" + demoParamIds + ")",
                "DELETE FROM ops.spc_param WHERE id IN (" + demoParamIds + ")"
        };
        for (String d : deletes) jdbcTemplate.update(d, orgId);

        Pdef[] defs = {
                new Pdef("主轴长度", "CNC-01", "mm", 50.000, 0.008, 3, "检测", "主轴总成", "SHAFT-001"),
                new Pdef("单体重", "ASSY-02", "g", 120.00, 0.05, 2, "装配", "单体外壳", "BODY-002"),
                new Pdef("回流焊炉温", "SMT-03", "℃", 230.0, 1.2, 1, "焊接", "PCBA主板", "PCB-003"),
                new Pdef("供电电压", "TEST-04", "V", 12.00, 0.03, 2, "系统", "电源模块", "PWR-004"),
                new Pdef("锁紧扭矩", "ASSY-05", "N·m", 8.50, 0.04, 2, "装配", "锁紧组件", "LCK-005")
        };

        LocalDate base = LocalDate.now().minusDays(SUBGROUPS - 1);
        for (int pi = 0; pi < defs.length; pi++) {
            Pdef d = defs[pi];
            String paramId = UUID.randomUUID().toString();
            String supplierId = supplierIds.get(pi % supplierIds.size());
            double hw = 4.2 * d.sigma();               // 规格半宽 → CPK≈1.4
            double usl = round(d.target() + hw, d.scale());
            double lsl = round(d.target() - hw, d.scale());
            String specText = lsl + " ~ " + usl + " " + d.unit();

            jdbcTemplate.update(
                    "INSERT INTO ops.spc_param (id, org_id, param_name, proc_name, process_id, unit, spec_text, "
                            + "spec_lower, spec_upper, target_value, subgroup_size, collect_freq, is_active, "
                            + "chart_candidates, chart_type, sigma_method, sigma_k, supplier_id, created_at, created_by, is_deleted, version) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,'true','Xbar,R','Xbar','within',3,?,now(),null,false,0)",
                    paramId, orgId, d.name(), d.process(), processMap.get(d.cat()), d.unit(), specText,
                    BigDecimal.valueOf(lsl), BigDecimal.valueOf(usl), BigDecimal.valueOf(d.target()),
                    SUBGROUP_SIZE, "每日", supplierId);

            // 1.2 关联产品绑定(参数↔产品多对多):每个参数绑定 1 个产品,保证供应商+产品齐全,不与线上校验冲突
            jdbcTemplate.update(
                    "INSERT INTO ops.spc_param_product (id, org_id, param_id, product_name, part_no, kind, created_at, is_deleted, version) "
                            + "VALUES (ops.gen_uuid_v7(), ?, ?, ?, ?, 'product', now(), false, 0)",
                    orgId, paramId, d.product(), d.partNo());

            // 1.5 演示采集任务(轮询 5 种采集模式,使管理页有数据可验证)
            jdbcTemplate.update(
                    "INSERT INTO ops.spc_collect_task (id, org_id, param_id, collect_freq, status, collect_mode, next_due_at, created_at, is_deleted, version) "
                            + "VALUES (?,?,?,'每日','待采集',?, now() + interval '1 day', now(), false, 0)",
                    UUID.randomUUID().toString(), orgId, paramId, COLLECT_MODES[pi % COLLECT_MODES.length]);

            // 2. 生成子组 + 测量值
            for (int i = 0; i < SUBGROUPS; i++) {
                LocalDate day = base.plusDays(i);
                LocalDateTime t = day.atTime(8, 0).plusMinutes(i * 30L);
                boolean outlier = (pi == 0 && (i == 1 || i == 2)); // 仅首参数最早期 2 个子组判异(落在 FIRST 区间内)
                double shift = outlier ? 2.0 * d.sigma() : 0.0;
                // 阶段维度:前 FIRST_N 组为「首件验证点」,其余为「量产监控点」
                String stage = (i < FIRST_N) ? "FIRST" : "ROUTINE";

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
                // 每个子组批号不同(按日期+序号),体现「不同节点批号不一定相同,需批号溯源」
                String batchNo = "BN-" + day + "-" + String.format("%02d", i + 1);
                String woNo = "WO-" + d.process() + "-" + String.format("%03d", (i / 5) + 1);
                jdbcTemplate.update(
                        "INSERT INTO ops.spc_subgroup (id, org_id, param_id, subgroup_no, subgroup_time, n, xbar, range_r, "
                                + "stage, judge, is_outlier, outlier_rule, data_source, batch_no, wo_no, operator_id, created_at, created_by) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,'正常',?,?, 'manual', ?, ?, ?, now(),?)",
                        subgroupId, orgId, paramId, i + 1, t, SUBGROUP_SIZE, BigDecimal.valueOf(xbar), BigDecimal.valueOf(rangeR),
                        stage, outlier, outlier ? "①" : null, batchNo, woNo, operatorId, operatorId);

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
                // 生成最近 6 个月的能力快照,使"CPK 历史趋势"有多周期数据
                // (否则仅当前月 1 点,趋势折线画不出、表格仅 1 行,看起来像"没数据")
                YearMonth baseYm = YearMonth.now();
                for (int m = 5; m >= 0; m--) {
                    capabilityService.calc(paramId, "OVERALL", baseYm.minusMonths(m).toString());
                }
                log.info("[SPC 种子] 参数已就绪: {} ({})", d.name(), paramId);
            } catch (Exception ex) {
                log.warn("[SPC 种子] 计算失败 param={} : {}", d.name(), ex.getMessage());
            }
        }
        log.info("[SPC 种子] 完成,共 {} 个参数, 每参数 {} 子组", defs.length, SUBGROUPS);
    }

    /** 确保 MZ 演示组织下存在若干演示供应商(幂等),返回其 id 列表用于参数绑定。 */
    private List<String> ensureDemoSuppliers(String orgId) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ops.sqm_supplier WHERE org_id = ?", Integer.class, orgId);
        if (cnt == null || cnt == 0) {
            for (int i = 1; i <= 5; i++) {
                String no = "SPC-DEMO-" + i;
                jdbcTemplate.update(
                        "INSERT INTO ops.sqm_supplier (id, org_id, supplier_no, supplier_code, name, credit_code, category, status, level, created_at, is_deleted, version) "
                                + "VALUES (?,?,?,?,?,'CREDIT-" + no + "','电子','合格','B',now(),false,0) "
                                + "ON CONFLICT (supplier_no) DO NOTHING",
                        UUID.randomUUID().toString(), orgId, no, no, "演示供应商" + i);
            }
        }
        return jdbcTemplate.query("SELECT id FROM ops.sqm_supplier WHERE org_id = ? ORDER BY supplier_no LIMIT 5",
                (rs, rn) -> rs.getString(1), orgId);
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

    /** 确保演示工序主数据(装配/焊接/检测/系统)存在,返回 工序名→id 映射,供参数绑定父层。幂等。 */
    private Map<String, String> ensureDefaultProcesses(String orgId) {
        String[][] defaults = {
                {"装配", "1"}, {"焊接", "2"}, {"检测", "3"}, {"系统", "4"}
        };
        for (String[] p : defaults) {
            jdbcTemplate.update(
                    "INSERT INTO ops.spc_process (id, org_id, process_name, sort_no, is_active, created_at, is_deleted, version) "
                            + "VALUES (?, ?, ?, ?, true, now(), false, 0) "
                            + "ON CONFLICT (org_id, process_name) DO NOTHING",
                    UUID.randomUUID().toString(), orgId, p[0], Integer.parseInt(p[1]));
        }
        return jdbcTemplate.query("SELECT id, process_name FROM ops.spc_process WHERE org_id = ? AND is_deleted = false",
                (rs, rn) -> Map.of(rs.getString("process_name"), rs.getString("id")), orgId)
                .stream().flatMap(m -> m.entrySet().stream())
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private double round(double v, int scale) {
        BigDecimal b = BigDecimal.valueOf(v).setScale(scale, java.math.RoundingMode.HALF_UP);
        return b.doubleValue();
    }
}
