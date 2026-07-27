-- 不良趋势报表:恶化规则 + 报表快照
-- 与既有 Flyway 命名(Vxx__)及 ops schema 保持一致;幂等建表。

CREATE TABLE IF NOT EXISTS ops.ncm_defect_trend_rule (
    id                  uuid PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
    org_id              uuid,
    consecutive_days    int          NOT NULL DEFAULT 3,
    use_mean_plus_2sigma boolean      NOT NULL DEFAULT true,
    sigma_multiplier    numeric(4,2) NOT NULL DEFAULT 2.0,
    baseline_days       int          NOT NULL DEFAULT 30,
    enabled             boolean      NOT NULL DEFAULT true,
    created_at          timestamptz  NOT NULL DEFAULT now(),
    updated_at          timestamptz  NOT NULL DEFAULT now(),
    is_deleted          boolean      NOT NULL DEFAULT false,
    version             int          NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ops.ncm_defect_trend_report (
    id              uuid PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
    org_id          uuid,
    product_model   varchar(80),
    granularity     varchar(16)  NOT NULL,           -- day / week / month
    period_value    varchar(32)  NOT NULL,           -- 2026-07-26 / 2026-W30 / 2026-07
    summary_json    jsonb,
    rule_snapshot   jsonb,
    generated_at    timestamptz  NOT NULL DEFAULT now(),
    created_at      timestamptz  NOT NULL DEFAULT now(),
    is_deleted      boolean      NOT NULL DEFAULT false,
    version         int          NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_trend_report_lookup
    ON ops.ncm_defect_trend_report (org_id, product_model, granularity, period_value)
    WHERE is_deleted = false;

-- 默认全局恶化规则(org_id 为空表示全局生效)
INSERT INTO ops.ncm_defect_trend_rule (id, org_id, consecutive_days, use_mean_plus_2sigma, sigma_multiplier, baseline_days, enabled)
SELECT ops.gen_uuid_v7(), NULL, 3, true, 2.0, 30, true
WHERE NOT EXISTS (SELECT 1 FROM ops.ncm_defect_trend_rule WHERE is_deleted = false);
