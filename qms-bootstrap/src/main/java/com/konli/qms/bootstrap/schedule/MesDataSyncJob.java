package com.konli.qms.bootstrap.schedule;

import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * MES → QMS 主数据同步任务(每日 02:00 定时 + 应用启动触发)。
 *
 * <p>把 MES 落地宽表 {@code qms.material_inspection} 中的真实供应商与来料不良记录,
 * 同步进 QMS 自有主数据/业务表, 供「供应商档案」「来料异常」「来料批次」页面展示:</p>
 * <ol>
 *   <li>{@link #syncSuppliers()} 按 (plant_code→org, supplier_code) 去重, 生成 ops.sqm_supplier
 *       (ven_code = MES supplier_code, name = supplier_name)。</li>
 *   <li>{@link #syncIncomingAbnormal()} 把 unqualified_qty>0 的来料检验行, 转成
 *       ops.sqm_incoming_abnormal (abnormal_no='MESMI-'+record_no, 经 supplier 关联回填 supplier_id)。</li>
 *   <li>{@link #syncIncomingLot()} 把每行 material_inspection 作为一个来料批次, 转成
 *       ops.sqm_incoming_lot (lot_no=record_no, 经 (org, ven_code) 关联回填 supplier_id)。</li>
 * </ol>
 *
 * <p>幂等: 供应商按 ven_code、异常按 abnormal_no、批次按 lot_no NOT EXISTS 去重。可每日重跑,
 * 也兼容 MES 重新整表导入后补齐新增供应商/不良。逻辑与 scripts/mes_sync_supplier_abnormal.sql 一致。</p>
 *
 * <p>注意: 本 Job 仅做数据搬运, 不触发审核/通知/8D 流程; 生成的异常单状态固定为「待处理」。</p>
 */
@Slf4j
@Component
public class MesDataSyncJob {

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    /** MES 落地宽表所在 schema(与 qms.mes.schema 配置保持一致, 禁止硬编码)。 */
    @Value("${qms.mes.schema:qms}")
    private String mesSchema;

    public MesDataSyncJob(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    /** 每日 02:00 同步(早于 RepeatEscalationJob 的 03:00, 保证升级扫描前数据已就绪)。 */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncAll() {
        log.info("=== 定时任务启动: MES 供应商/来料不良同步 ===");
        try {
            int suppliers = syncSuppliers();
            int abnormals = syncIncomingAbnormal();
            int lots = syncIncomingLot();
            log.info("=== 定时任务完成: 新增供应商 {} 条, 新增来料异常 {} 条, 新增来料批次 {} 条 ===", suppliers, abnormals, lots);
        } catch (Exception e) {
            log.error("MES 同步任务异常(不阻断): {}", e.getMessage(), e);
        }
    }

    /** 应用启动即同步一次(除每日 02:00 定时外), 保证 pull 后启动即可见供应商/来料异常/来料批次数据。异步执行, 不阻塞启动。 */
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        if (environment.acceptsProfiles(Profiles.of("test"))) {
            return;
        }
        CompletableFuture.runAsync(this::syncAll);
    }

    /** 步骤 1: MES 供应商去重 → ops.sqm_supplier。返回新增条数。 */
    private int syncSuppliers() {
        String sql = """
            INSERT INTO ops.sqm_supplier
                (id, org_id, supplier_no, supplier_code, name, category, status, ven_code, created_at, updated_at, is_deleted)
            SELECT ops.gen_uuid_v7(), o.id,
                   md5(o.org_code || '|' || mi.supplier_code),
                   left(o.org_code || '_' || mi.supplier_code, 16),
                   CASE WHEN mi.supplier_name IS NULL OR mi.supplier_name = '' THEN '供应商-' || mi.supplier_code ELSE mi.supplier_name END,
                   '其它', '合格',
                   mi.supplier_code,
                   now(), now(), false
            FROM (
                SELECT DISTINCT ON (plant_code, supplier_code)
                    plant_code, supplier_code,
                    COALESCE(NULLIF(supplier_name, ''), supplier_code) AS supplier_name
                FROM %s.material_inspection
                WHERE is_deleted = '0'
                  AND supplier_code IS NOT NULL AND supplier_code <> ''
                  AND supplier_code NOT IN ('未知', 'UNKNOWN', '无', '-', 'N/A')
                ORDER BY plant_code, supplier_code, supplier_name
            ) mi
            JOIN ops.sys_org o ON o.org_code = mi.plant_code
            WHERE NOT EXISTS (
                SELECT 1 FROM ops.sqm_supplier s
                WHERE s.org_id = o.id AND s.ven_code = mi.supplier_code
            )
            """.formatted(mesSchema);
        return jdbcTemplate.update(sql);
    }

    /** 步骤 2: MES 来料不良 → ops.sqm_incoming_abnormal。返回新增条数。 */
    private int syncIncomingAbnormal() {
        String sql = """
            INSERT INTO ops.sqm_incoming_abnormal
                (id, org_id, abnormal_no, lot_id, batch_no, supplier_id, part_no, part_name, description,
                 qty, incoming_qty, level, occur_date, status, created_at, updated_at, is_deleted)
            SELECT ops.gen_uuid_v7(), o.id,
                   'MESMI-' || mi.record_no,
                   left(mi.record_no, 32),
                   NULLIF(mi.material_batch_no, ''),
                   s.id,
                   NULLIF(mi.material_code, ''),
                   NULLIF(mi.material_name, ''),
                   COALESCE(NULLIF(mi.defect_desc, ''), NULLIF(mi.handling_method, ''), 'MES来料不良导入'),
                   GREATEST(1, COALESCE(mi.unqualified_qty, 0)::int),
                   COALESCE(mi.submitted_qty, 0)::int,
                   CASE WHEN COALESCE(mi.unqualified_qty, 0) >= 3 THEN '严重' ELSE '一般' END,
                   mi.inspection_date,
                   '待处理',
                   now(), now(), false
            FROM %s.material_inspection mi
            JOIN ops.sys_org o ON o.org_code = mi.plant_code
            JOIN ops.sqm_supplier s ON s.org_id = o.id AND s.ven_code = mi.supplier_code
            WHERE mi.is_deleted = '0'
              AND COALESCE(mi.unqualified_qty, 0) > 0
              AND mi.supplier_code IS NOT NULL AND mi.supplier_code <> ''
              AND NOT EXISTS (
                  SELECT 1 FROM ops.sqm_incoming_abnormal a WHERE a.abnormal_no = 'MESMI-' || mi.record_no
              )
            """.formatted(mesSchema);
        return jdbcTemplate.update(sql);
    }

    /** 步骤 3: MES 来料检验批 → ops.sqm_incoming_lot。每行 material_inspection 即一个来料批次。返回新增条数。 */
    private int syncIncomingLot() {
        String sql = """
            INSERT INTO ops.sqm_incoming_lot
                (id, org_id, lot_no, supplier_id, part_no, part_name, qty, unit, incoming_date,
                 inspect_result, inspect_type, iqc_pass, po_no, is_key_part, created_at, updated_at,
                 is_deleted, ven_code, material_batch_no, supplier_name)
            SELECT ops.gen_uuid_v7(), o.id,
                   mi.record_no,
                   s.id,
                   NULLIF(mi.material_code, ''),
                   NULLIF(mi.material_name, ''),
                   COALESCE(mi.submitted_qty, 0),
                   NULLIF(mi.unit, ''),
                   COALESCE(mi.arrival_date, mi.inspection_date),
                   COALESCE(NULLIF(mi.inspection_result, ''), '待检'),
                   'IQC',
                   (mi.inspection_result = '合格'),
                   left(NULLIF(mi.purchase_order, ''), 32),
                   false,
                   now(), now(), false,
                   mi.supplier_code,
                   NULLIF(mi.material_batch_no, ''),
                   NULLIF(mi.supplier_name, '')
            FROM %s.material_inspection mi
            JOIN ops.sys_org o ON o.org_code = mi.plant_code
            JOIN ops.sqm_supplier s ON s.org_id = o.id AND s.ven_code = mi.supplier_code
            WHERE mi.is_deleted = '0'
              AND mi.supplier_code IS NOT NULL AND mi.supplier_code <> ''
              AND NOT EXISTS (SELECT 1 FROM ops.sqm_incoming_lot l WHERE l.lot_no = mi.record_no)
            """.formatted(mesSchema);
        return jdbcTemplate.update(sql);
    }
}
