-- V91: 清理 FIA 首件检验标准库脏数据,以来料追溯节点为权威源重新灌入检验标准与检验项明细。
-- 背景: 旧标准库存在软删记录与有效记录序号冲突、检验项重复累积、状态值不统一(生效/ACTIVE/???)、
--       工序名混乱(来料首件/ASSY-02/TEST-PROC)、无料号孤儿记录、测试种子数据(SEED-LOT-*/MAT-Q3-*)、
--       同料号多条重复记录,且 material 列存的是料号而非名称,导致前端产品名称与料号相同。
-- 规则: 物料必须绑定供应商,未绑定视为脏数据;半成品与成品不绑外部供应商,统一绑定"工厂自产"。
--       品类映射 incoming->material / semi->semi / ship->product,raw 与 customer 不参与。
-- 幂等: 清理使用无条件 DELETE,重灌以 sqm_trace_node 为源 INSERT ... SELECT 并带 NOT EXISTS 守卫,可重复执行。

-- ============ 1) 供应商主数据规范:工厂自产 ============
-- 半成品/成品只绑定自己工厂,历史名称 InternalPlant 改为业务口径的"工厂自产"。
-- 复用同一记录 ID,sqm_trace_node / spc_param / fia_* 等既有外键引用自动生效,不动唯一约束字段。
UPDATE ops.sqm_supplier
   SET name = '工厂自产', updated_at = now()
 WHERE id = 'a6d5d36e-9efb-bac7-496a-b14cd4a7b055'
   AND name <> '工厂自产';

-- ============ 2) 清理来料追溯脏数据:未绑定供应商的孤立来料节点 ============
-- 仅删除同时满足以下条件的节点:来料类、无供应商、孤立根节点(无父节点)、
-- 无子节点、无三张明细表引用、无 sqm_trace_link 软引用。任一条件不满足则保留,避免误删。
DELETE FROM ops.sqm_trace_node n
 WHERE n.node_type = 'incoming'
   AND n.supplier_id IS NULL
   AND n.parent_node_id IS NULL
   AND NOT EXISTS (SELECT 1 FROM ops.sqm_trace_node c WHERE c.parent_node_id = n.id)
   AND NOT EXISTS (SELECT 1 FROM ops.sqm_trace_node c WHERE c.root_node_id = n.id)
   AND NOT EXISTS (SELECT 1 FROM ops.sqm_trace_raw_detail d WHERE d.node_id = n.id)
   AND NOT EXISTS (SELECT 1 FROM ops.sqm_trace_product_detail d WHERE d.node_id = n.id)
   AND NOT EXISTS (SELECT 1 FROM ops.sqm_trace_customer_detail d WHERE d.node_id = n.id)
   AND NOT EXISTS (SELECT 1 FROM ops.sqm_trace_link l
                    WHERE l.parent_node_id = n.id OR l.child_node_id = n.id);

-- 清理上一步遗留的孤儿来料批次:已无任何追溯节点、关键件、IQC 记录、首件任务引用。
DELETE FROM ops.sqm_incoming_lot l
 WHERE l.supplier_id IS NULL
   AND NOT EXISTS (SELECT 1 FROM ops.sqm_trace_node n WHERE n.root_lot_id = l.id)
   AND NOT EXISTS (SELECT 1 FROM ops.sqm_key_part_sn k WHERE k.lot_id = l.id)
   AND NOT EXISTS (SELECT 1 FROM ops.sqm_iqc_inspect_record r WHERE r.lot_id = l.id)
   AND NOT EXISTS (SELECT 1 FROM ops.fia_task t WHERE t.lot_id = l.id);

-- ============ 3) 清空 FIA 业务数据(按外键依赖自底向上) ============
-- 守卫: 仅在检测到脏数据时才清空重建,避免误伤生产环境已由业务正常录入的标准。
-- 脏数据判据(满足任一即触发): 存在软删记录、无料号记录、产品名与料号相同的记录,
-- 或有效标准数多于来料追溯合法料号数(说明混入了重复/测试数据)。
DO $$
DECLARE
  dirty BOOLEAN;
  valid_cnt INTEGER;
  std_cnt INTEGER;
BEGIN
  SELECT COUNT(*) INTO std_cnt FROM ops.fia_insp_std;

  SELECT COUNT(*) INTO valid_cnt
    FROM (
      SELECT DISTINCT n.material_code
        FROM ops.sqm_trace_node n
       WHERE n.is_deleted = false
         AND n.node_type IN ('ship', 'semi', 'incoming')
         AND n.material_code IS NOT NULL
         AND n.material_code <> ''
         AND (n.node_type <> 'incoming' OR n.supplier_id IS NOT NULL)
    ) v;

  dirty := EXISTS (SELECT 1 FROM ops.fia_insp_std WHERE is_deleted = true)
        OR EXISTS (SELECT 1 FROM ops.fia_insp_std_item WHERE is_deleted = true)
        OR EXISTS (SELECT 1 FROM ops.fia_insp_std WHERE part_no IS NULL OR part_no = '')
        OR EXISTS (SELECT 1 FROM ops.fia_insp_std WHERE material = part_no)
        -- 工序语义错填(首件类型而非制造工序)亦视为脏数据,触发重灌纠正
        OR EXISTS (SELECT 1 FROM ops.fia_insp_std WHERE proc_name IN ('成品首件', '半成品首件', '来料首件') AND is_deleted = false)
        OR std_cnt > valid_cnt;

  IF NOT dirty THEN
    RAISE NOTICE 'FIA 标准库未检测到脏数据(std=%,valid=%),跳过清空与重灌。', std_cnt, valid_cnt;
    RETURN;
  END IF;

  -- spc_param 属 SPC 模块资产,不在清理范围,仅解除其对 FIA 的两处引用,保留记录本身。
  UPDATE ops.spc_param SET fia_std_item_id = NULL WHERE fia_std_item_id IS NOT NULL;
  UPDATE ops.spc_param SET src_item_id = NULL WHERE src_item_id IS NOT NULL;

  DELETE FROM ops.fia_approval;
  DELETE FROM ops.fia_archived_report;
  DELETE FROM ops.fia_task_log;
  DELETE FROM ops.fia_insp_item;
  DELETE FROM ops.fia_task;
  DELETE FROM ops.fia_insp_plan;
  -- 先断开自引用,否则删除主表时受 prev_version_id 外键阻塞。
  UPDATE ops.fia_insp_std SET prev_version_id = NULL WHERE prev_version_id IS NOT NULL;
  DELETE FROM ops.fia_insp_std_item;
  DELETE FROM ops.fia_insp_std;
END $$;

-- ============ 4) 重灌检验标准主表 ============
-- 数据源为来料追溯节点,不硬编码料号清单,后续追溯新增节点重跑本脚本即可自动扩展。
-- part_no 存料号,material 存节点名称(职责分离,修复产品名与料号相同的根因)。
-- 工序(proc_name)修正为制造工序(与 SPC 工序字典 spc_process 同源):按品类映射
--   material(来料) -> 检测, semi(半成品) -> 装配, product(成品) -> 系统,
-- 并写入 spc_process_id 外键强绑定。同一料号可能存在多条节点记录,按 ship > semi > incoming 优先级 DISTINCT ON 去重。
INSERT INTO ops.fia_insp_std (
  org_id, code, material, proc_name, spc_process_id, std_version, status,
  part_no, supplier_id, category, std_name, is_default, is_deleted, version
)
SELECT src.org_id,
       'STD-' || src.material_code || '-' || src.cat,
       src.node_name,
       src.mapped_proc,
       (SELECT p.id FROM ops.spc_process p
         WHERE p.org_id = src.org_id AND p.process_name = src.mapped_proc AND p.is_deleted = false LIMIT 1),
       'v1',
       '生效',
       src.material_code,
       src.supplier_id,
       src.cat,
       src.node_name || ' - ' || src.mapped_proc,
       false, false, 0
  FROM (
    SELECT DISTINCT ON (n.material_code)
           n.org_id,
           n.material_code,
           n.node_name,
           CASE n.node_type WHEN 'ship' THEN 'product' WHEN 'semi' THEN 'semi' ELSE 'material' END AS cat,
           CASE n.node_type WHEN 'ship' THEN '系统' WHEN 'semi' THEN '装配' ELSE '检测' END AS mapped_proc,
           -- 来料取节点自身绑定的真实供应商,半成品与成品统一取"工厂自产"
           CASE n.node_type WHEN 'incoming' THEN n.supplier_id
                            ELSE 'a6d5d36e-9efb-bac7-496a-b14cd4a7b055'::uuid END AS supplier_id
      FROM ops.sqm_trace_node n
     WHERE n.is_deleted = false
       AND n.node_type IN ('ship', 'semi', 'incoming')
       AND n.material_code IS NOT NULL
       AND n.material_code <> ''
       -- 物料必须绑定供应商,未绑定的自动剔除,使脏数据识别成为规则的自然结果
       AND (n.node_type <> 'incoming' OR n.supplier_id IS NOT NULL)
     ORDER BY n.material_code,
              CASE n.node_type WHEN 'ship' THEN 0 WHEN 'semi' THEN 1 ELSE 2 END
  ) src
 WHERE NOT EXISTS (
       SELECT 1 FROM ops.fia_insp_std e
        WHERE e.code = 'STD-' || src.material_code || '-' || src.cat);

-- ============ 5) 重灌检验项明细 ============
-- CROSS JOIN 常量模板批量生成,保证每条标准的明细数量与序号完全一致,从机制上杜绝重复累积。
INSERT INTO ops.fia_insp_std_item (
  org_id, std_id, seq, item_name, is_ctq, std_value, tolerance, unit, value_type, is_deleted
)
SELECT s.org_id, s.id, t.seq, t.item_name, t.is_ctq, t.std_value, t.tolerance, t.unit, t.value_type, false
  FROM ops.fia_insp_std s
 CROSS JOIN (VALUES
   (1, '外观',     true,  '无划伤/无变形/无污渍', NULL,    '—',  '文本'),
   (2, '关键尺寸', true,  '10.00',                '±0.05', 'mm', '数值'),
   (3, '性能测试', false, '≥95',                  NULL,    '%',  '数值')
 ) AS t(seq, item_name, is_ctq, std_value, tolerance, unit, value_type)
 WHERE s.is_deleted = false
   AND NOT EXISTS (
       SELECT 1 FROM ops.fia_insp_std_item i
        WHERE i.std_id = s.id AND i.seq = t.seq AND i.is_deleted = false);
