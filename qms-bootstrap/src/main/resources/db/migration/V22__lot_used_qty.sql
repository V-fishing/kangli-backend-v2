-- V20: 来料批次加已投数量,防超领
ALTER TABLE ops.sqm_incoming_lot ADD COLUMN IF NOT EXISTS used_qty NUMERIC(14,2) DEFAULT 0;
COMMENT ON COLUMN ops.sqm_incoming_lot.used_qty IS '已投料数量(追溯投料累加,防超领)';
