-- V198 报废归档表补充 scrap_id 唯一约束, 使 onScrapApproved 的
-- "ON CONFLICT (scrap_id) DO NOTHING" 幂等归档生效(需求3 计量报废归档)。
-- 修复前因缺唯一约束导致 PG 报 "no unique or exclusion constraint matching
-- the ON CONFLICT" 而被吞成 WARN、归档写入静默失败的问题。
ALTER TABLE ops.tlm_scrap_archive
  ADD CONSTRAINT uq_tlm_scrap_archive_scrap_id UNIQUE (scrap_id);
