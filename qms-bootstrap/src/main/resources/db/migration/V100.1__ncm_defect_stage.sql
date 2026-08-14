-- V100.1: 不良记录新增 stage(不良阶段)维度
-- 与缺陷现象字典(defect_dict_code)正交,用于区分不良发生的业务阶段/来源场景。
-- 枚举: 来料不良 / 半成品不良 / 成品不良 / 首件不良
ALTER TABLE ops.ncm_defect_record
  ADD COLUMN IF NOT EXISTS stage VARCHAR(16) NOT NULL DEFAULT '来料不良';

COMMENT ON COLUMN ops.ncm_defect_record.stage IS '不良阶段: 来料不良/半成品不良/成品不良/首件不良';

-- 历史数据回填: 已有记录统一置为默认阶段(后续由录入/自动产生时写入具体阶段)
UPDATE ops.ncm_defect_record SET stage = '来料不良' WHERE stage IS NULL OR stage = '';
