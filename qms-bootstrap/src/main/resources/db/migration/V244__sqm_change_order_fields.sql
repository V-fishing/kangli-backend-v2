-- 物料变更单补全主流 ECN 字段(生效日期/新旧料号对照/客户通知批准/影响范围/切换日)
-- 幂等:列已存在则跳过(IF NOT EXISTS)
ALTER TABLE ops.sqm_change_order ADD COLUMN IF NOT EXISTS eff_date date;
ALTER TABLE ops.sqm_change_order ADD COLUMN IF NOT EXISTS old_part_no character varying(64);
ALTER TABLE ops.sqm_change_order ADD COLUMN IF NOT EXISTS new_part_no character varying(64);
ALTER TABLE ops.sqm_change_order ADD COLUMN IF NOT EXISTS customer_notify boolean DEFAULT false NOT NULL;
ALTER TABLE ops.sqm_change_order ADD COLUMN IF NOT EXISTS customer_approved boolean DEFAULT false NOT NULL;
ALTER TABLE ops.sqm_change_order ADD COLUMN IF NOT EXISTS impact_desc text;
ALTER TABLE ops.sqm_change_order ADD COLUMN IF NOT EXISTS switch_date date;
