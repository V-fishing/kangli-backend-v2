-- 审核执行相关表补齐 BaseEntity 标准审计列(created_at/updated_at/created_by/updated_by/is_deleted/version)
-- V06 建 sqm_audit_checklist_item / sqm_audit_photo 时未含这些列;V156 建的 workflow_log 同理。
-- 实体继承 BaseEntity,需这些列方可 INSERT/SELECT。幂等 ADD COLUMN IF NOT EXISTS。

DO $$
BEGIN
  -- sqm_audit_checklist_item
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_checklist_item' AND column_name='created_at') THEN
    ALTER TABLE ops.sqm_audit_checklist_item ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_checklist_item' AND column_name='updated_at') THEN
    ALTER TABLE ops.sqm_audit_checklist_item ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_checklist_item' AND column_name='created_by') THEN
    ALTER TABLE ops.sqm_audit_checklist_item ADD COLUMN created_by VARCHAR(64);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_checklist_item' AND column_name='updated_by') THEN
    ALTER TABLE ops.sqm_audit_checklist_item ADD COLUMN updated_by VARCHAR(64);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_checklist_item' AND column_name='is_deleted') THEN
    ALTER TABLE ops.sqm_audit_checklist_item ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT false;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_checklist_item' AND column_name='version') THEN
    ALTER TABLE ops.sqm_audit_checklist_item ADD COLUMN version INT NOT NULL DEFAULT 0;
  END IF;

  -- sqm_audit_photo
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_photo' AND column_name='created_at') THEN
    ALTER TABLE ops.sqm_audit_photo ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_photo' AND column_name='updated_at') THEN
    ALTER TABLE ops.sqm_audit_photo ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_photo' AND column_name='created_by') THEN
    ALTER TABLE ops.sqm_audit_photo ADD COLUMN created_by VARCHAR(64);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_photo' AND column_name='updated_by') THEN
    ALTER TABLE ops.sqm_audit_photo ADD COLUMN updated_by VARCHAR(64);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_photo' AND column_name='is_deleted') THEN
    ALTER TABLE ops.sqm_audit_photo ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT false;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_photo' AND column_name='version') THEN
    ALTER TABLE ops.sqm_audit_photo ADD COLUMN version INT NOT NULL DEFAULT 0;
  END IF;

  -- sqm_audit_workflow_log
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_workflow_log' AND column_name='created_at') THEN
    ALTER TABLE ops.sqm_audit_workflow_log ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_workflow_log' AND column_name='updated_at') THEN
    ALTER TABLE ops.sqm_audit_workflow_log ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_workflow_log' AND column_name='created_by') THEN
    ALTER TABLE ops.sqm_audit_workflow_log ADD COLUMN created_by VARCHAR(64);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_workflow_log' AND column_name='updated_by') THEN
    ALTER TABLE ops.sqm_audit_workflow_log ADD COLUMN updated_by VARCHAR(64);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_workflow_log' AND column_name='is_deleted') THEN
    ALTER TABLE ops.sqm_audit_workflow_log ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT false;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ops' AND table_name='sqm_audit_workflow_log' AND column_name='version') THEN
    ALTER TABLE ops.sqm_audit_workflow_log ADD COLUMN version INT NOT NULL DEFAULT 0;
  END IF;
END $$;
