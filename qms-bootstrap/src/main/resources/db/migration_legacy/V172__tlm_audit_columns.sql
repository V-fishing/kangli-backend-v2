-- V172: 补齐 tlm 各表的标准审计列(对齐 BaseEntity: created_at/updated_at/created_by/updated_by/is_deleted/version)。
-- 部分表在 V162-V168 创建时遗漏了 updated_at/updated_by/version(及 tool_wo_bind 的 created_at/created_by),
-- 导致 MyBatis-Plus 查询报 "column updated_at does not exist"。此处幂等补齐。
DO $$
DECLARE
  t text;
  cols text[];
  c text;
  has boolean;
BEGIN
  -- 需要补齐审计列的表清单
  FOR t IN
    SELECT unnest(ARRAY[
      'tlm_tooling','tlm_tool_product','tlm_maint_plan','tlm_maint_record',
      'tlm_repair','tlm_scrap','tlm_tool_wo_bind'
    ])
  LOOP
    -- updated_at
    SELECT COUNT(*) > 0 INTO has FROM information_schema.columns
      WHERE table_schema='ops' AND table_name=t AND column_name='updated_at';
    IF NOT has THEN
      EXECUTE format('ALTER TABLE ops.%I ADD COLUMN updated_at TIMESTAMP DEFAULT now()', t);
    END IF;
    -- updated_by
    SELECT COUNT(*) > 0 INTO has FROM information_schema.columns
      WHERE table_schema='ops' AND table_name=t AND column_name='updated_by';
    IF NOT has THEN
      EXECUTE format('ALTER TABLE ops.%I ADD COLUMN updated_by VARCHAR(64)', t);
    END IF;
    -- version
    SELECT COUNT(*) > 0 INTO has FROM information_schema.columns
      WHERE table_schema='ops' AND table_name=t AND column_name='version';
    IF NOT has THEN
      EXECUTE format('ALTER TABLE ops.%I ADD COLUMN version INT DEFAULT 0', t);
    END IF;
    -- created_at(仅 tool_wo_bind 缺失)
    SELECT COUNT(*) > 0 INTO has FROM information_schema.columns
      WHERE table_schema='ops' AND table_name=t AND column_name='created_at';
    IF NOT has THEN
      EXECUTE format('ALTER TABLE ops.%I ADD COLUMN created_at TIMESTAMP DEFAULT now()', t);
    END IF;
    -- created_by(仅 tool_wo_bind 缺失)
    SELECT COUNT(*) > 0 INTO has FROM information_schema.columns
      WHERE table_schema='ops' AND table_name=t AND column_name='created_by';
    IF NOT has THEN
      EXECUTE format('ALTER TABLE ops.%I ADD COLUMN created_by VARCHAR(64)', t);
    END IF;
  END LOOP;
  RAISE NOTICE 'TLM audit columns ensured on all tlm tables';
END $$;
