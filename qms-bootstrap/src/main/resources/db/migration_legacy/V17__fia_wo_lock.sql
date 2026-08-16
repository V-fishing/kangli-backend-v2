-- ============================================================
-- V17: 首件工单锁定记录(SR-FIA-022~026 生产锁定与解锁流程)
-- 轻量模型:工单(wo_no)为业务键,锁定/解锁/紧急放行全程留痕。
-- 在制品待处理(wip_hold)/禁止流转 用锁定语义表达(一期不接 MES 真实 WIP)。
-- ============================================================
CREATE TABLE IF NOT EXISTS ops.fia_wo_lock (
    id              uuid PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
    org_id          uuid NOT NULL REFERENCES ops.sys_org(id),
    wo_no           varchar(64) NOT NULL,
    lock_status     varchar(16) NOT NULL,        -- 锁定 / 正常
    lock_reason     varchar(64),                 -- 首件未完成 / 首件不合格
    locked_at       timestamptz NOT NULL DEFAULT now(),
    wip_hold        boolean NOT NULL DEFAULT false,  -- 在制品待处理标记
    unlock_type     varchar(16),                 -- 自动解锁 / 紧急放行
    unlocked_at     timestamptz,
    approver_id     uuid,                        -- 紧急放行审批人
    release_reason  varchar(255),                -- 放行原因/审批意见
    trace_tag       varchar(64),                 -- 放行产品追溯标签
    task_code       varchar(64),                 -- 触发锁定的首件校验单号
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    created_by      uuid,
    updated_by      uuid,
    is_deleted      boolean NOT NULL DEFAULT false,
    version         integer NOT NULL DEFAULT 0
);

CREATE INDEX idx_fia_wo_lock_wo ON ops.fia_wo_lock (org_id, wo_no);
COMMENT ON TABLE ops.fia_wo_lock IS '首件工单锁定记录:锁定(首件未完成/不合格)->解锁(自动/紧急放行),全程留痕';
