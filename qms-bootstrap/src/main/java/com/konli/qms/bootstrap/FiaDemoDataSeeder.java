package com.konli.qms.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * FIA 首件检验演示数据种子(dev profile, @Order(20) 晚于 SpcDemoDataSeeder)。
 *
 * <p>仅清空并灌入物料(material)/半成品(semi)/成品(product)三分类的"待检"首件任务,
 * 含检验标准与检验项。SPC 控制图数据由 SpcDemoDataSeeder 独立提供,
 * 只有完成检验且判定为"合格"时才通过真实 FIA→SPC 链路同步子组与控制限。</p>
 */
@Slf4j
@Component
@Profile("dev")
@Order(20)
@RequiredArgsConstructor
public class FiaDemoDataSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    /** 三分类演示任务定义:category, source, 产品名, 件号, 任务编码。 */
    private record TaskDef(String category, String source, String productName, String partNo, String code) {}

    /** 检验项定义:项名, 标准值, 公差, 单位, 下界, 上界, 可制图。 */
    private record SpecDef(String itemName, String stdValue, String tolerance, String unit,
                           BigDecimal lower, BigDecimal upper, boolean chartable) {}

    /** 三分类共用的一套检验项(2 个可制图 + 1 个外观不可制图,演示过滤)。 */
    private static final SpecDef[] SPECS = {
            new SpecDef("关键尺寸", "10.00", "±0.05", "mm", new BigDecimal("9.95"), new BigDecimal("10.05"), true),
            new SpecDef("性能测试", "95.0", "±2.0", "%", new BigDecimal("93.0"), new BigDecimal("97.0"), true),
            new SpecDef("外观检查", "无划伤/无变形", "-", "", null, null, false)
    };

    @Override
    public void run(ApplicationArguments args) {
        String orgId = getDemoOrgId();
        log.info("[FIA 种子] 开始重建三分类首件演示数据, orgId={}", orgId);

        cleanup(orgId);

        String supplierId = jdbcTemplate.queryForObject(
                "SELECT id FROM ops.sqm_supplier WHERE org_id = ? LIMIT 1", String.class, orgId);

        TaskDef[] tasks = {
                new TaskDef("material", "SUPPLIER", "锂电池电芯", "BAT-CELL-3000", "FIA-DEMO-M-001"),
                new TaskDef("semi", "FACTORY", "电池模组", "BAT-MOD-100", "FIA-DEMO-S-001"),
                new TaskDef("product", "FACTORY", "动力电池包", "PACK-001", "FIA-DEMO-P-001")
        };

        for (TaskDef t : tasks) {
            seedTask(orgId, t, supplierId);
        }
        log.info("[FIA 种子] 完成,共 {} 个待检首件演示任务(不含 SPC 数据)", tasks.length);
    }

    /** 清空该组织的 FIA 子树 + 自有演示标准 + FIA 派生 spc_param(级联清 spc_param_product)。
     *  仅清理本 Seeder 自己灌入的演示任务(code 以 'FIA-DEMO-' 开头),保留用户手动创建的首件任务。 */
    private void cleanup(String orgId) {
        // 删除前先解除 spc_param 对即将删除的 fia_insp_item 的 src_item_id 引用, 避免外键冲突。
        // 覆盖两类将被删除的 item: (a) demo 任务下的 item; (b) 引用 demo 标准项的 item。
        jdbcTemplate.update("UPDATE ops.spc_param SET src_item_id = NULL WHERE src_item_id IN ("
                + "SELECT id FROM ops.fia_insp_item WHERE task_id IN "
                + "(SELECT id FROM ops.fia_task WHERE org_id = ? AND code LIKE 'FIA-DEMO-%') "
                + "UNION SELECT id FROM ops.fia_insp_item WHERE std_item_id IN "
                + "(SELECT id FROM ops.fia_insp_std_item WHERE std_id IN "
                + "(SELECT id FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%')))", orgId);
        // 先删 fia_task 的子表(仅 demo 任务)
        jdbcTemplate.update("DELETE FROM ops.fia_insp_item WHERE task_id IN (SELECT id FROM ops.fia_task WHERE org_id = ? AND code LIKE 'FIA-DEMO-%')", orgId);
        jdbcTemplate.update("DELETE FROM ops.fia_approval WHERE task_id IN (SELECT id FROM ops.fia_task WHERE org_id = ? AND code LIKE 'FIA-DEMO-%')", orgId);
        jdbcTemplate.update("DELETE FROM ops.fia_archived_report WHERE task_id IN (SELECT id FROM ops.fia_task WHERE org_id = ? AND code LIKE 'FIA-DEMO-%')", orgId);
        jdbcTemplate.update("DELETE FROM ops.fia_task_log WHERE task_id IN (SELECT id FROM ops.fia_task WHERE org_id = ? AND code LIKE 'FIA-DEMO-%')", orgId);
        // 删除可能引用 fia_insp_std 的 fia_task 记录(外键约束,仅 demo 任务)
        jdbcTemplate.update("DELETE FROM ops.spc_subgroup WHERE task_id IN (SELECT id::varchar FROM ops.fia_task WHERE org_id = ? AND code LIKE 'FIA-DEMO-%')", orgId);
        // 仅删除本 Seeder 产生的演示任务,不动用户手动创建的任务
        jdbcTemplate.update("DELETE FROM ops.fia_task WHERE org_id = ? AND code LIKE 'FIA-DEMO-%'", orgId);
        // 再删标准库的子表 + fia_insp_std 本身
        // 先解除 spc_param 对 demo 标准项(fia_insp_std_item)的 fia_std_item_id 引用, 避免外键冲突
        jdbcTemplate.update("UPDATE ops.spc_param SET fia_std_item_id = NULL WHERE fia_std_item_id IN "
                + "(SELECT id FROM ops.fia_insp_std_item WHERE std_id IN "
                + "(SELECT id FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%'))");
        // 先删引用这些标准项的检验项(可能来自历史 seed 的 fia_insp_item, 不受 demo task 清理覆盖), 避免外键约束
        jdbcTemplate.update("DELETE FROM ops.fia_insp_item WHERE std_item_id IN (SELECT id FROM ops.fia_insp_std_item WHERE std_id IN (SELECT id FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%'))");
        jdbcTemplate.update("DELETE FROM ops.fia_insp_std_item WHERE std_id IN (SELECT id FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%')");
        // 删除仍引用这些演示标准的任务(历史任务可能绑定了 demo 标准, std_id 为 NOT NULL 不能置空),
        // 连同其子表一并清理, 避免删 fia_insp_std 触发 fia_task_std_id_fkey 冲突
        jdbcTemplate.update("DELETE FROM ops.fia_insp_item WHERE task_id IN (SELECT id FROM ops.fia_task WHERE std_id IN (SELECT id FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%'))");
        jdbcTemplate.update("DELETE FROM ops.fia_approval WHERE task_id IN (SELECT id FROM ops.fia_task WHERE std_id IN (SELECT id FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%'))");
        jdbcTemplate.update("DELETE FROM ops.fia_archived_report WHERE task_id IN (SELECT id FROM ops.fia_task WHERE std_id IN (SELECT id FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%'))");
        jdbcTemplate.update("DELETE FROM ops.fia_task_log WHERE task_id IN (SELECT id FROM ops.fia_task WHERE std_id IN (SELECT id FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%'))");
        jdbcTemplate.update("DELETE FROM ops.fia_task WHERE std_id IN (SELECT id FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%')");
        jdbcTemplate.update("DELETE FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%'");
        // 仅清理本 Seeder 自己的演示标准(FIA-STD-DEMO-%)派生的 spc_param,绝不碰用户由真实首件任务派生的参数。
        // 用 demo 标准项 id 集合作为唯一判定依据,避免 (fia_std_item_id IS NOT NULL) 误删用户数据。
        String demoParamIds = "SELECT id FROM ops.spc_param WHERE org_id = ? "
                + "AND fia_std_item_id IN (SELECT id FROM ops.fia_insp_std_item WHERE std_id IN (SELECT id FROM ops.fia_insp_std WHERE code LIKE 'FIA-STD-DEMO-%'))";
        // 先清引用这些派生的 spc_param 的首件抽样任务(spc_sample_task, fk_spctask_param),避免删 spc_param 时外键冲突
        // 抽样任务的子组存于 spc_subgroup.sample_task_id
        jdbcTemplate.update("DELETE FROM ops.spc_subgroup WHERE sample_task_id IN (SELECT id FROM ops.spc_sample_task WHERE param_id IN (" + demoParamIds + "))", orgId);
        jdbcTemplate.update("DELETE FROM ops.spc_sample_task WHERE param_id IN (" + demoParamIds + ")", orgId);
        jdbcTemplate.update("DELETE FROM ops.spc_notify_record WHERE alarm_id IN (SELECT id FROM ops.spc_alarm WHERE param_id IN (" + demoParamIds + "))", orgId);
        jdbcTemplate.update("DELETE FROM ops.spc_alarm WHERE param_id IN (" + demoParamIds + ")", orgId);
        jdbcTemplate.update("DELETE FROM ops.spc_collect_task WHERE param_id IN (" + demoParamIds + ")", orgId);
        jdbcTemplate.update("DELETE FROM ops.spc_import_log WHERE param_id IN (" + demoParamIds + ")", orgId);
        jdbcTemplate.update("DELETE FROM ops.spc_capability WHERE param_id IN (" + demoParamIds + ")", orgId);
        jdbcTemplate.update("DELETE FROM ops.spc_control_limit WHERE param_id IN (" + demoParamIds + ")", orgId);
        jdbcTemplate.update("DELETE FROM ops.spc_subgroup WHERE param_id IN (" + demoParamIds + ")", orgId);
        jdbcTemplate.update("DELETE FROM ops.spc_param_product WHERE param_id IN (" + demoParamIds + ")", orgId);
        jdbcTemplate.update("DELETE FROM ops.spc_param WHERE id IN (" + demoParamIds + ")", orgId);
    }

    /** 灌入单个分类任务:标准 + 检验项 + "待检"状态任务(不生成 SPC 数据)。 */
    private void seedTask(String orgId, TaskDef t, String supplierId) {
        // 1. 自有演示检验标准(幂等标记 FIA-STD-DEMO-<cat>)
        String stdId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO ops.fia_insp_std (id, org_id, code, material, proc_name, spc_process_id, std_version, status, "
                        + "is_default, created_at, updated_at, is_deleted, version) "
                        + "VALUES (?,?,?,?,?,(SELECT p.id FROM ops.spc_process p WHERE p.org_id=? AND p.process_name='检测' AND p.is_deleted=false LIMIT 1),'v1','生效',false,now(),now(),false,0)",
                stdId, orgId, "FIA-STD-DEMO-" + t.category(), t.productName(), "检测", orgId);

        String[] stdItemIds = new String[SPECS.length];
        for (int i = 0; i < SPECS.length; i++) {
            SpecDef s = SPECS[i];
            String itemId = UUID.randomUUID().toString();
            stdItemIds[i] = itemId;
            jdbcTemplate.update(
                    "INSERT INTO ops.fia_insp_std_item (id, org_id, std_id, seq, item_name, is_ctq, std_value, "
                            + "tolerance, unit, value_type, lower_limit, upper_limit, is_deleted) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,false)",
                    itemId, orgId, stdId, i + 1, s.itemName(), s.chartable(),
                    s.stdValue(), s.tolerance(), s.unit(),
                    s.chartable() ? "numeric" : "text", s.lower(), s.upper());
        }

        // 2. FIA 首件任务(待检状态)
        String taskId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO ops.fia_task (id, org_id, code, wo_no, line_name, product_name, part_no, proc_name, "
                        + "trigger_type, std_id, std_version, status, source, category, supplier_id, "
                        + "is_urgent, is_overdue, pass_rate, created_at, updated_at, is_deleted, version) "
                        + "VALUES (?,?,?,?,?,?,?,?,'换批次',?,?,'待检',?,?,?,false,false,0,now(),now(),false,0)",
                taskId, orgId, t.code(), "WO-DEMO-" + t.category(), "FIA-Demo-Line", t.productName(), t.partNo(),
                "检测", stdId, "v1", t.source(), t.category(),
                "SUPPLIER".equals(t.source()) ? supplierId : null);

        // 3. 检验项(关联标准项,待检状态不填充测量值/判定)
        for (int i = 0; i < SPECS.length; i++) {
            SpecDef s = SPECS[i];
            jdbcTemplate.update(
                    "INSERT INTO ops.fia_insp_item (id, org_id, task_id, seq, item_name, is_ctq, std_value, "
                            + "tolerance, unit, measured_value, judge, std_item_id) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,null,null,?)",
                    UUID.randomUUID().toString(), orgId, taskId, i + 1, s.itemName(), s.chartable(),
                    s.stdValue(), s.tolerance(), s.unit(), stdItemIds[i]);
        }
    }

    private String getDemoOrgId() {
        String id = jdbcTemplate.queryForObject(
                "SELECT id FROM ops.sys_org WHERE org_code = 'MZ' LIMIT 1", String.class);
        if (id != null) {
            return id;
        }
        id = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO ops.sys_org (id, org_code, org_name, org_type, status, created_at, is_deleted, version) "
                        + "VALUES (?, 'MZ', '美滋(演示)', '工厂', 1, now(), false, 0)", id);
        return id;
    }
}
