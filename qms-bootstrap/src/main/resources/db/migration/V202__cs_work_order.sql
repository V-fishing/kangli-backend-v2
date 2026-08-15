-- ============================================================
-- V202 售后管理 · 工单流程控制(cs) 主表。
-- 背景: 售后管理 A 模块(工单流程控制),纯手动录入(不对接外部 CRM)。
-- 状态机: PENDING(待派单) -> ASSIGNED(处理中) -> DONE(已完成) -> CLOSED(已评价闭环)。
-- 工单类型: INSTALL(安装) / REPAIR(维修)。
-- 满意度字段预留(C 模块客户满意度下一轮对接工单评价闭环)。
-- 全部幂等, 可重复执行。
-- ============================================================

CREATE TABLE IF NOT EXISTS ops.cs_work_order (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),

  order_no      VARCHAR(32) NOT NULL,          -- 工单号(手动录入或系统生成, 业务唯一)
  customer_name VARCHAR(120) NOT NULL,         -- 客户名称
  customer_contact VARCHAR(120),               -- 客户联系方式
  wo_type       VARCHAR(16) NOT NULL,          -- INSTALL / REPAIR
  priority      VARCHAR(16) NOT NULL DEFAULT 'NORMAL',  -- URGENT / NORMAL / LOW
  product_name  VARCHAR(160),                  -- 涉及产品/设备名称
  fault_desc    TEXT,                          -- 故障描述 / 服务需求

  status        VARCHAR(16) NOT NULL DEFAULT 'PENDING',  -- PENDING/ASSIGNED/DONE/CLOSED

  owner_id      UUID,                          -- 负责人(派单目标, ops.sys_user.id)
  owner_name    VARCHAR(80),                   -- 负责人姓名(文本快照)
  assign_at     TIMESTAMP,                     -- 派单时间
  handle_detail TEXT,                          -- 处理过程 / 措施
  handle_at     TIMESTAMP,                     -- 处理完成(标记完成)时间
  close_at      TIMESTAMP,                     -- 评价闭环时间

  -- 满意度预留(客户满意度模块下一轮回填)
  satisfaction  INTEGER,                       -- 1~5 满意度评分
  satisfaction_comment VARCHAR(400),           -- 满意度评价文本

  expect_time   TIMESTAMP,                     -- 期望上门/完成时间
  address       VARCHAR(240),                  -- 服务地址

  created_at    TIMESTAMP NOT NULL DEFAULT now(),
  updated_at    TIMESTAMP NOT NULL DEFAULT now(),
  created_by    VARCHAR(64),
  updated_by    VARCHAR(64),
  is_deleted    BOOLEAN NOT NULL DEFAULT false,
  version       INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_cs_work_order_no
  ON ops.cs_work_order(org_id, order_no) WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_cs_work_order_org_status
  ON ops.cs_work_order(org_id, status) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_cs_work_order_org_created
  ON ops.cs_work_order(org_id, created_at DESC) WHERE is_deleted = false;
