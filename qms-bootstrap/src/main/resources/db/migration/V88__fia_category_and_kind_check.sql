-- V88: FIA 任务增加「分类」字段(物料/半成品/成品); 并对 kind/category 加 CHECK 约束限定只取三值
-- 1) FIA 任务新增可空分类列(老任务为 null,回退按 source 派生)
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS category VARCHAR(16);
COMMENT ON COLUMN ops.fia_task.category IS '任务分类: material=物料 / semi=半成品 / product=成品(可空, 老任务按 source 派生)';
ALTER TABLE ops.fia_task DROP CONSTRAINT IF EXISTS chk_fia_task_category;
ALTER TABLE ops.fia_task ADD CONSTRAINT chk_fia_task_category
  CHECK (category IS NULL OR category IN ('material', 'semi', 'product'));

-- 2) SPC 参数产品 kind 加 CHECK 约束(已有数据为 material/product, 均合法)
ALTER TABLE ops.spc_param_product DROP CONSTRAINT IF EXISTS chk_spc_pp_kind;
ALTER TABLE ops.spc_param_product ADD CONSTRAINT chk_spc_pp_kind
  CHECK (kind IN ('material', 'semi', 'product'));
