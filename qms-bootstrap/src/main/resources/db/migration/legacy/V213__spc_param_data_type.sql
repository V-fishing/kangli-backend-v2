-- SPC 参数数据形态(data_type): VARIABLE(计量型) / ATTRIBUTE(计数型)
-- 由 chart_candidates 推断: 含 P/NP/C/U 任一则为 ATTRIBUTE, 否则 VARIABLE。
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS data_type VARCHAR(16);

UPDATE ops.spc_param
SET data_type = CASE
    WHEN chart_candidates IS NOT NULL
         AND (chart_candidates LIKE '%P%' OR chart_candidates LIKE '%NP%'
              OR chart_candidates LIKE '%C%' OR chart_candidates LIKE '%U%')
    THEN 'ATTRIBUTE'
    ELSE 'VARIABLE'
END
WHERE data_type IS NULL;
