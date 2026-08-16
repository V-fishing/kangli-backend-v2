-- V94: 清理旧 MES 导入遗留的加工/中间表数据(仅清数据, 保留表结构供录入模型使用)
-- 适用场景: 重跑 V90(方案B) 前, 清除旧版 V90 造的 KIT-/SHIP-/SEMI- 占位批次与 node/link/detail 数据。
--
-- 说明:
--   * sqm_incoming_lot 中 lot_no 形如 'KIT-...' / 'SHIP-...' / 'SEMI-...' 的占位批次删除(真实来料批次不动)。
--   * sqm_trace_node / sqm_trace_link / sqm_trace_raw_detail / sqm_trace_product_detail / sqm_key_part_sn
--     旧版写入的全部删除(清空), 表结构保留(录入模型 attachComponent 仍可能引用 sqm_trace_node)。
--   * 本脚本可重复执行, 幂等。

-- 清空旧版写入的追溯节点/边/明细/关键件SN(先清被 FK 引用的子表, 避免违反外键约束)
TRUNCATE TABLE ops.sqm_key_part_sn RESTART IDENTITY;   -- 引用 sqm_incoming_lot.id, 须先于 lot 删除
TRUNCATE TABLE ops.sqm_trace_link RESTART IDENTITY;
TRUNCATE TABLE ops.sqm_trace_node RESTART IDENTITY CASCADE;
TRUNCATE TABLE ops.sqm_trace_raw_detail RESTART IDENTITY;
TRUNCATE TABLE ops.sqm_trace_product_detail RESTART IDENTITY;

-- 删除旧版占位批次(KIT-/SHIP-/SEMI- 前缀), 真实来料批次(lot_no = record_no)保留
DELETE FROM ops.sqm_incoming_lot
WHERE lot_no LIKE 'KIT-%' OR lot_no LIKE 'SHIP-%' OR lot_no LIKE 'SEMI-%';

-- 注: 不在自动迁移里清空 sqm_trace_relation(避免冲掉 V90 已填充的边)。
--     若需重跑 V90 导入, 手动执行: TRUNCATE TABLE ops.sqm_trace_relation RESTART IDENTITY; 后再跑 V90。
