-- ============================================================
-- V153 供应商绩效评审 R3 补齐
-- 1) 指标配置表 sqm_perf_metric_cfg（权重/阈值可配置）
-- 2) 绩效指标默认权重 seed
-- 3) 采购份额基线 seed（让分级联动有数据可写）
-- 4) 审核频次规则 seed（让绩效→频次建议有输出）
-- 全部幂等，可重复执行。
-- ============================================================

-- ---------- 1) 指标配置表 ----------
CREATE TABLE IF NOT EXISTS ops.sqm_perf_metric_cfg (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID REFERENCES ops.sys_org(id),
  metric_code  VARCHAR(32) NOT NULL UNIQUE,   -- INCOMING_PASS / DELIVERY / QUALITY / RECTIFY / COMPLIANCE
  metric_name  VARCHAR(64) NOT NULL,
  weight       NUMERIC(5,2) NOT NULL DEFAULT 0,
  target       NUMERIC(5,2),                  -- 达标阈值
  challenge     NUMERIC(5,2),                 -- 挑战阈值
  enabled      BOOLEAN NOT NULL DEFAULT true,
  auto_linkage BOOLEAN NOT NULL DEFAULT false,-- 分级是否联动份额/状态(默认关)
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.sqm_perf_metric_cfg IS '供应商绩效指标配置(权重/阈值,calc 读取而非写死)';

-- ---------- 2) 默认权重 seed（幂等 upsert） ----------
INSERT INTO ops.sqm_perf_metric_cfg (metric_code, metric_name, weight, target, challenge, enabled, auto_linkage)
SELECT 'INCOMING_PASS', '来料合格率', 30.00, 98.00, 99.50, true, false
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_perf_metric_cfg WHERE metric_code = 'INCOMING_PASS');

INSERT INTO ops.sqm_perf_metric_cfg (metric_code, metric_name, weight, target, challenge, enabled, auto_linkage)
SELECT 'DELIVERY', '交付及时率', 30.00, 95.00, 99.00, true, false
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_perf_metric_cfg WHERE metric_code = 'DELIVERY');

INSERT INTO ops.sqm_perf_metric_cfg (metric_code, metric_name, weight, target, challenge, enabled, auto_linkage)
SELECT 'QUALITY', '质量分(SPC过程能力)', 40.00, 90.00, 95.00, true, false
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_perf_metric_cfg WHERE metric_code = 'QUALITY');

INSERT INTO ops.sqm_perf_metric_cfg (metric_code, metric_name, weight, target, challenge, enabled, auto_linkage)
SELECT 'RECTIFY', '整改及时率', 0.00, 90.00, 98.00, false, false
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_perf_metric_cfg WHERE metric_code = 'RECTIFY');

INSERT INTO ops.sqm_perf_metric_cfg (metric_code, metric_name, weight, target, challenge, enabled, auto_linkage)
SELECT 'COMPLIANCE', '合规率', 0.00, 95.00, 99.00, false, false
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_perf_metric_cfg WHERE metric_code = 'COMPLIANCE');

-- ---------- 3) 采购份额基线 seed ----------
-- 对每个有来料批次的供应商,取一个代表料号与最早批次日期,插入一条份额基准(100%)。
-- 仅当 sqm_supplier_share 尚无该供应商记录时插入,幂等。
INSERT INTO ops.sqm_supplier_share (org_id, supplier_id, part_no, share_ratio, effective_date, change_reason)
SELECT s.org_id,
       v.supplier_id,
       v.part_no,
       100.00,
       v.first_incoming,
       '绩效联动基线(初始100%)'
FROM (
  SELECT l.supplier_id,
         (ARRAY_AGG(l.part_no ORDER BY l.incoming_date NULLS LAST))[1] AS part_no,
         MIN(l.incoming_date) AS first_incoming
  FROM ops.sqm_incoming_lot l
  WHERE l.supplier_id IS NOT NULL
    AND l.is_deleted = false
  GROUP BY l.supplier_id
) v
JOIN ops.sqm_supplier s ON s.id = v.supplier_id
WHERE s.is_deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM ops.sqm_supplier_share sh WHERE sh.supplier_id = v.supplier_id
  );

-- ---------- 4) 审核频次规则 seed ----------
-- 按绩效等级给出现场审核频次建议(年频次)。幂等。
INSERT INTO ops.sqm_audit_freq_rule (org_id, risk_level, level, freq_per_year, audit_type)
SELECT NULL, '高', 'D', 4, '现场审核'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_freq_rule WHERE level = 'D');

INSERT INTO ops.sqm_audit_freq_rule (org_id, risk_level, level, freq_per_year, audit_type)
SELECT NULL, '中', 'C', 2, '现场审核'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_freq_rule WHERE level = 'C');

INSERT INTO ops.sqm_audit_freq_rule (org_id, risk_level, level, freq_per_year, audit_type)
SELECT NULL, '低', 'B', 1, '文件审核'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_freq_rule WHERE level = 'B');

INSERT INTO ops.sqm_audit_freq_rule (org_id, risk_level, level, freq_per_year, audit_type)
SELECT NULL, '极低', 'A', 1, '文件审核'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_freq_rule WHERE level = 'A');

-- ---------- 5) 绩效指标配置菜单 seed ----------
DO $$
DECLARE
  v_sqm_id UUID;
  v_menu_id UUID;
BEGIN
  SELECT id INTO v_sqm_id FROM ops.sys_menu WHERE menu_code = 'sqm';
  IF v_sqm_id IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
    SELECT ops.gen_uuid_v7(), v_sqm_id, 'sqm.perf.config', '绩效指标配置', '菜单', '/sqm/perf-config', 'sqm/PerfConfig', '⚙️', 99, true
    WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code = 'sqm.perf.config')
    RETURNING id INTO v_menu_id;
    IF v_menu_id IS NULL THEN
      SELECT id INTO v_menu_id FROM ops.sys_menu WHERE menu_code = 'sqm.perf.config';
    END IF;
    -- 授权给有 sqm.capa 权限的角色(绩效同源)
    INSERT INTO ops.sys_role_menu (role_id, menu_id)
    SELECT rm.role_id, v_menu_id
    FROM ops.sys_role_menu rm
    JOIN ops.sys_menu m ON m.id = rm.menu_id
    WHERE m.menu_code = 'sqm.capa' AND NOT EXISTS (
      SELECT 1 FROM ops.sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = v_menu_id
    );
  END IF;
END $$;
