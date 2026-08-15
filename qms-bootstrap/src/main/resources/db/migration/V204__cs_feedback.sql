-- ============================================================
-- V204 售后管理 · 客户满意度(B 模块) 客户反馈登记表。
-- 背景: 客户满意度管理(监控客户服务质量)。除工单闭环评分(cs_work_order.satisfaction)
-- 外, 还需独立登记非工单渠道的客户反馈(投诉/建议/表扬/咨询)并跟踪处理。
-- 全部幂等, 可重复执行。org_id 外键 sys_org。
-- ============================================================

CREATE TABLE IF NOT EXISTS ops.cs_feedback (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),

  customer_name VARCHAR(120) NOT NULL,         -- 客户名称
  customer_contact VARCHAR(120),               -- 联系方式
  fb_type       VARCHAR(16) NOT NULL,          -- COMPLAINT / SUGGESTION / PRAISE / INQUIRY
  content       TEXT NOT NULL,                 -- 反馈内容
  related_wo_no VARCHAR(32),                   -- 关联工单号(可选)
  status        VARCHAR(16) NOT NULL DEFAULT 'OPEN',  -- OPEN / HANDLING / DONE

  handle_detail TEXT,                          -- 处理过程/结果
  handle_at     TIMESTAMP,                     -- 处理完成时间
  owner_name    VARCHAR(80),                   -- 处理人(文本快照)

  satisfaction  INTEGER,                       -- 反馈关联满意度评分 1~5(可选)

  created_at    TIMESTAMP NOT NULL DEFAULT now(),
  updated_at    TIMESTAMP NOT NULL DEFAULT now(),
  created_by    VARCHAR(64),
  updated_by    VARCHAR(64),
  is_deleted    BOOLEAN NOT NULL DEFAULT false,
  version       INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_cs_feedback_org_status
  ON ops.cs_feedback(org_id, status) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_cs_feedback_org_created
  ON ops.cs_feedback(org_id, created_at DESC) WHERE is_deleted = false;
