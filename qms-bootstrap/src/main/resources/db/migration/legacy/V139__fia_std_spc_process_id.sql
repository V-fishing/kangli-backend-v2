-- V138: 检验标准库工序语义纠正 + SPC 工序同源对齐。
-- 背景: 旧 V91 重灌把 fia_insp_std.proc_name 错填为首件类型(成品首件/半成品首件/来料首件),
--       而真实主流模型中标准库"工序"应是制造工序,与 SPC 工序字典(spc_process:焊接/装配/检测/系统)同源。
--       两套工序语义混用导致 FIA->SPC 匹配维度错位。本脚本:
--       1) fia_insp_std 加 spc_process_id 外键列(强绑定 SPC 字典,防文本漂移);
--       2) 存量首件类型 proc_name 按品类映射为制造工序(来料->检测, 半成品->装配, 成品->系统),并回填 spc_process_id;
--       3) spc_param 历史脏数据对齐: proc_name 对齐为字典 process_name, 回填 process_id(按 process_id/字典名关联)。
-- 幂等: 全程用 ADD COLUMN IF NOT EXISTS / UPDATE ... WHERE 条件守卫,可重复执行。

-- ============ 1) fia_insp_std 加 spc_process_id 列 ============
ALTER TABLE ops.fia_insp_std
  ADD COLUMN IF NOT EXISTS spc_process_id UUID REFERENCES ops.spc_process(id);

-- ============ 2) 存量首件类型 proc_name -> 制造工序, 回填 spc_process_id ============
-- 映射规则(按品类, 与 SPC 字典现有值对齐):
--   material(来料) -> 检测
--   semi(半成品)    -> 装配
--   product(成品)   -> 系统
--   其余/未知(NULL) -> 检测
-- 仅处理仍为"首件类型"文本的标准, 已为制造工序的不动。
-- 注意: PostgreSQL 不允许 UPDATE 目标表 s 出现在 FROM 的 JOIN 源中,
-- 故 spc_process 匹配改用相关子查询(引用 s.org_id),FROM 仅保留品类映射值表 m。
UPDATE ops.fia_insp_std s
   SET proc_name = m.mapped,
       spc_process_id = (SELECT p.id FROM ops.spc_process p
                          WHERE p.org_id = s.org_id AND p.process_name = m.mapped AND p.is_deleted = false LIMIT 1),
       std_name = s.material || ' - ' || m.mapped,
       updated_at = now()
  FROM (VALUES
        ('material', '检测'),
        ('semi', '装配'),
        ('product', '系统')
       ) AS m(cat, mapped)
 WHERE s.proc_name IN ('成品首件', '半成品首件', '来料首件')
   AND (s.category = m.cat OR (s.category IS NULL AND m.cat = '检测'))
   AND s.is_deleted = false;

-- 兜底: 仍为首件类型但品类未匹配上的(如 category 异常), 统一归"检测"
UPDATE ops.fia_insp_std s
   SET proc_name = '检测',
       spc_process_id = (SELECT p.id FROM ops.spc_process p
                          WHERE p.org_id = s.org_id AND p.process_name = '检测' AND p.is_deleted = false LIMIT 1),
       std_name = s.material || ' - 检测',
       updated_at = now()
 WHERE s.proc_name IN ('成品首件', '半成品首件', '来料首件')
   AND s.is_deleted = false;

-- ============ 3) spc_param 历史脏数据对齐 ============
-- 3a) 已绑定 process_id 的: 将 proc_name 对齐为字典 process_name(避免"来料首件"等错值残留)
UPDATE ops.spc_param sp
   SET proc_name = p.process_name,
       updated_at = now()
  FROM ops.spc_process p
 WHERE sp.process_id = p.id
   AND sp.proc_name IS DISTINCT FROM p.process_name
   AND sp.is_deleted = false;

-- 3b) 未绑定 process_id 但 proc_name 与某字典工序名一致的: 回填 process_id
UPDATE ops.spc_param sp
   SET process_id = p.id,
       updated_at = now()
  FROM ops.spc_process p
 WHERE sp.process_id IS NULL
   AND sp.proc_name = p.process_name
   AND p.is_deleted = false
   AND sp.is_deleted = false;

-- 3c) 未绑定 process_id 且 proc_name 仍是首件类型等错值的: 归"检测"(有 process_id 才能被 CollectView/同步正确识别)
UPDATE ops.spc_param sp
   SET process_id = (SELECT p.id FROM ops.spc_process p
                      WHERE p.org_id = sp.org_id AND p.process_name = '检测' AND p.is_deleted = false LIMIT 1),
       proc_name = '检测',
       updated_at = now()
 WHERE sp.process_id IS NULL
   AND sp.proc_name IN ('成品首件', '半成品首件', '来料首件', '首件')
   AND sp.is_deleted = false;
