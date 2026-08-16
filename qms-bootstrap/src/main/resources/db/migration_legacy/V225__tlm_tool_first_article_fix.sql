-- V225: 修复工装首件无法创建的根因。
-- 现象: 工装台账中"创建首件"按钮点击后前端直接 warning 拦截、或后端 400 报"缺少产品编码/工序/未找到检验标准"。
-- 根因: V169 工装种子数据只写了 tool_no/tool_name/risk_class/software_ver/location/tool_type,
--       未写入 product_code 与 proc_name。前端 createFirst() 要求二者非空、后端 createFromTooling()
--       也以 product_code 作 part_no、proc_name 作工序去 FIA 标准库精确匹配"生效"标准,
--       二者任一缺失或匹配不到标准即失败。
-- 修复:
--   1) 为演示工装 SKL-J-20260011 补 product_code + proc_name(电极活化工装的产线首件语义)。
--   2) 在 FIA 标准库新增一条"生效"标准(part_no=工装产品码, proc_name=检测),并补 3 条标准项,
--      使 matchStd(part_no, proc_name) 能精确命中,首件任务可正常创建。
-- 全部幂等(NOT EXISTS),可重复执行。

-- ===== 1) 新建 FIA 检验标准(电极活化工装产线首件) =====
INSERT INTO ops.fia_insp_std (
  id, org_id, code, material, proc_name, std_version, status,
  part_no, supplier_id, category, std_name, is_default, is_deleted, version
)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
       'STD-SKL-GL-ACTIVATE', '多通道GL电极活化工装', '检测', 'v1', '生效',
       'GL-ELECTRODE-ACT', NULL, 'material',
       '多通道GL电极活化工装 - 产线首件(检测)', false, false, 0
WHERE NOT EXISTS (
  SELECT 1 FROM ops.fia_insp_std WHERE code='STD-SKL-GL-ACTIVATE' AND is_deleted=false);

-- 该标准的检验项明细(复用 V140 模板: 外观/关键尺寸/性能测试)
INSERT INTO ops.fia_insp_std_item (
  id, org_id, std_id, seq, item_name, is_ctq, std_value, tolerance, unit, value_type, is_deleted
)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
       (SELECT id FROM ops.fia_insp_std WHERE code='STD-SKL-GL-ACTIVATE' AND is_deleted=false LIMIT 1),
       t.seq, t.item_name, t.is_ctq, t.std_value, t.tolerance, t.unit, t.value_type, false
FROM (VALUES
  (1, '外观',     true,  '无划伤/无变形/无污渍', NULL,    '—',  '文本'),
  (2, '活化温控精度', true,  '37.00',               '±0.5', '℃', '数值'),
  (3, '电极一致性', false, '≥95',                  NULL,    '%',  '数值')
) AS t(seq, item_name, is_ctq, std_value, tolerance, unit, value_type)
WHERE NOT EXISTS (
  SELECT 1 FROM ops.fia_insp_std_item i
   WHERE i.std_id = (SELECT id FROM ops.fia_insp_std WHERE code='STD-SKL-GL-ACTIVATE' AND is_deleted=false LIMIT 1)
     AND i.seq = t.seq AND i.is_deleted = false);

-- ===== 2) 回填演示工装 SKL-J-20260011 的 product_code / proc_name =====
UPDATE ops.tlm_tooling
   SET product_code = 'GL-ELECTRODE-ACT',
       proc_name = '检测',
       updated_at = now()
 WHERE tool_no = 'SKL-J-20260011'
   AND org_id = (SELECT id FROM ops.sys_org WHERE org_code='MZ')
   AND (product_code IS NULL OR product_code = '' OR proc_name IS NULL OR proc_name = '');
