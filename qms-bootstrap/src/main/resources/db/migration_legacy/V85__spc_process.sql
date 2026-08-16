-- V85: SPC 工序主数据(参数的父级分组维度,一父多子)
-- 工序(装配/焊接/检测/系统...)作为 SPC 参数的父级;spc_param 增加 process_id 外键(可空=未分类)与 chartable 标记。
-- 注意:默认工序数据由 SpcDemoDataSeeder(dev) 按演示组织(MZ)幂等灌入,
--       此处不插入数据,避免迁移阶段 sys_org 尚未就绪导致外键失败。

CREATE TABLE IF NOT EXISTS ops.spc_process (
    id           UUID DEFAULT ops.gen_uuid_v7() NOT NULL PRIMARY KEY,
    org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
    process_name VARCHAR(64) NOT NULL,
    process_code VARCHAR(32),
    description  VARCHAR(255),
    sort_no      INT DEFAULT 0,
    is_active    BOOLEAN NOT NULL DEFAULT true,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
    is_deleted   BOOLEAN NOT NULL DEFAULT false,
    version      INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_spc_process_org_name UNIQUE (org_id, process_name)
);
COMMENT ON TABLE  ops.spc_process IS 'SPC 工序主数据(参数的父级分组:装配/焊接/检测/系统...)';
COMMENT ON COLUMN ops.spc_process.org_id       IS '组织ID(分公司/车间)';
COMMENT ON COLUMN ops.spc_process.process_name IS '工序名称(如 装配/焊接/检测/系统)';
COMMENT ON COLUMN ops.spc_process.process_code IS '工序编码(业务可选)';
COMMENT ON COLUMN ops.spc_process.description  IS '工序说明';
COMMENT ON COLUMN ops.spc_process.sort_no      IS '展示排序,升序';
COMMENT ON COLUMN ops.spc_process.is_active    IS '是否启用';

-- SpcParam 增加父层工序外键(可空=未分类),原 proc_name 保留为"工位/说明"
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS process_id UUID REFERENCES ops.spc_process(id);
COMMENT ON COLUMN ops.spc_param.process_id IS '所属工序ID(父级分组,可空=未分类);原 proc_name 保留为工位/说明';

-- 不可制图标记:规格不完整(无上下限且无目标值)的参数无法制作控制图,从控制图入口隐藏
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS chartable BOOLEAN NOT NULL DEFAULT true;
COMMENT ON COLUMN ops.spc_param.chartable IS '是否可制图:specLower/specUpper/targetValue 全空则 false';

CREATE INDEX IF NOT EXISTS idx_spc_param_process ON ops.spc_param(process_id);
