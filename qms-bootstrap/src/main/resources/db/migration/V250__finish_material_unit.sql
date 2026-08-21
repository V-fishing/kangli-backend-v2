-- ============================================================
-- V250 完工检验生产字段权威源:物料主数据补计量单位并按 part_no 带出
-- 1) sqm_material 增 unit 列(型号规格/单位 = 物料属性, 定死不可手填)
-- 2) 幂等补种子:演示物料(part_no -> 名称/型号规格/单位)
--    生产日期/提交数量为工单属性, 暂无真实工单则留空, 不做种子
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 计量单位列
ALTER TABLE ops.sqm_material ADD COLUMN IF NOT EXISTS unit varchar(16);
COMMENT ON COLUMN ops.sqm_material.unit IS '计量单位(完工检验按 part_no 带出,定死不可手填)';

-- 2) 演示物料种子(幂等, 按 part_no 唯一键 upsert)
DO $$
DECLARE v_org uuid;
BEGIN
  SELECT id INTO v_org FROM ops.sys_org WHERE org_code = 'MZ' LIMIT 1;
  IF v_org IS NULL THEN
    SELECT id INTO v_org FROM ops.sys_org LIMIT 1;
  END IF;
  IF v_org IS NOT NULL THEN
    INSERT INTO ops.sqm_material (id, org_id, part_no, part_name, spec_model, unit, category, created_at, updated_at)
    VALUES
      (ops.gen_uuid_v7(), v_org, '99.06.000347',  '耗材包装纸箱（580x435x360）（试剂12PCS）', '580×435×360mm',             '个', 'product',  now(), now()),
      (ops.gen_uuid_v7(), v_org, '10.01.010400',  '多通道GL电极活化工装',                        'BG800专用，KL04.0133.0000.04', '套', 'product',  now(), now()),
      (ops.gen_uuid_v7(), v_org, '99.01.430005',  'AFT-C Electrolyte analyzer user manual',      'AFT-C',                      '本', 'product',  now(), now()),
      (ops.gen_uuid_v7(), v_org, '99.05.000633',  '试剂盒',                                     '99.05.000633',               '盒', 'product',  now(), now()),
      (ops.gen_uuid_v7(), v_org, 'PACK-001',      '动力电池包',                                 'PACK-001',                   '个', 'product',  now(), now()),
      (ops.gen_uuid_v7(), v_org, 'BAT-MOD-100',   '电池模组',                                   'BAT-MOD-100',                '个', 'semi',     now(), now()),
      (ops.gen_uuid_v7(), v_org, 'BAT-CELL-3000', '锂电池电芯',                                 'BAT-CELL-3000',              '颗', 'material', now(), now())
    ON CONFLICT (part_no) DO UPDATE
      SET part_name  = EXCLUDED.part_name,
          spec_model = EXCLUDED.spec_model,
          unit       = EXCLUDED.unit,
          updated_at = now();
  END IF;
END $$;
