-- ============================================================
-- V65: 菜单树对齐真实前端路由(幂等,可重复执行)
-- ------------------------------------------------------------
-- 背景:
--   原 V19 菜单种子(fia.entry/fia.approve/fia.stdlib…)的 path/component 与真实
--   前端路由/组件对不上,且 DataInitializer 的 ensureMenu 按 code 幂等、不会 UPDATE
--   已有行,导致"角色-菜单"配置虽能存库,但前端导航从未真正按菜单权限渲染。
--
-- 目标:
--   1) 每个菜单节点 path 存【绝对路由路径】(与 qms-web-new/src/router/index.ts 一一对应);
--   2) parent_id 仅用于"模块 → 子页签"层级分组,前端直接用节点自身 path 渲染,不做路径拼接;
--   3) 保留所有被 DataInitializer/角色授权引用的 menu_code(根码 fia/spc/ncm/sqm/patrol/
--      system 及 system.* ,子码 ncm.trend),只修正 名称/路径/组件/父级/图标/排序/可见性;
--   4) 业务子菜单码(无外部引用)自由复用,废弃项置 visible=false;
--   5) 补齐此前缺失的 patrol/archive/system 子页签与 ncm 8D 审核配置。
--
-- 授权说明:角色授权仍按根模块码(fia/spc/ncm/sqm/patrol)授予,本迁移不动 sys_role_menu;
--   管理员(全树)所见由后端 /me 直接返回完整可见树;非管理员按各自角色菜单码过滤。
-- ============================================================

DO $$
DECLARE
  fia_id    uuid;
  spc_id    uuid;
  ncm_id    uuid;
  sqm_id    uuid;
  patrol_id uuid;
  archive_id uuid;
  system_id uuid;
BEGIN
  -- 取根节点 id(幂等:仅当 V19 已建这些根时存在)
  SELECT id INTO fia_id    FROM ops.sys_menu WHERE menu_code = 'fia';
  SELECT id INTO spc_id    FROM ops.sys_menu WHERE menu_code = 'spc';
  SELECT id INTO ncm_id    FROM ops.sys_menu WHERE menu_code = 'ncm';
  SELECT id INTO sqm_id    FROM ops.sys_menu WHERE menu_code = 'sqm';
  SELECT id INTO patrol_id FROM ops.sys_menu WHERE menu_code = 'patrol';
  SELECT id INTO archive_id FROM ops.sys_menu WHERE menu_code = 'archive';
  SELECT id INTO system_id FROM ops.sys_menu WHERE menu_code = 'system';

  -- ===== 1) 根模块:名称/类型/路径/图标/排序(保留 code & id)=====
  UPDATE ops.sys_menu SET menu_name='首件检验', menu_type='目录', path='/fia',     component=NULL, icon='📋', sort_order=10, visible=true WHERE menu_code='fia';
  UPDATE ops.sys_menu SET menu_name='SPC 监控', menu_type='目录', path='/spc',     component=NULL, icon='📈', sort_order=20, visible=true WHERE menu_code='spc';
  UPDATE ops.sys_menu SET menu_name='不良管理', menu_type='目录', path='/ncm',     component=NULL, icon='⚠️', sort_order=30, visible=true WHERE menu_code='ncm';
  UPDATE ops.sys_menu SET menu_name='供应商',   menu_type='目录', path='/sqm',     component=NULL, icon='🏢', sort_order=40, visible=true WHERE menu_code='sqm';
  UPDATE ops.sys_menu SET menu_name='巡检',     menu_type='目录', path='/patrol',  component=NULL, icon='🔦', sort_order=50, visible=true WHERE menu_code='patrol';
  UPDATE ops.sys_menu SET menu_name='归档',     menu_type='目录', path='/archive', component=NULL, icon='🗄️', sort_order=60, visible=true WHERE menu_code='archive';
  UPDATE ops.sys_menu SET menu_name='系统管理', menu_type='目录', path='/system',  component=NULL, icon='⚙️', sort_order=90, visible=true WHERE menu_code='system';

  -- overview 根 → 工作台(绝对路径 /dashboard)
  UPDATE ops.sys_menu
     SET menu_code='dashboard', menu_name='工作台', menu_type='菜单', path='/dashboard',
         component='dashboard/index', icon='📊', sort_order=0, visible=true, parent_id=NULL
   WHERE menu_code='overview';

  -- ===== 2) FIA 子页签(复用现有 child code,挂到 fia 根)=====
  UPDATE ops.sys_menu SET parent_id=fia_id, menu_name='任务列表', menu_type='菜单', path='/fia/tasks',          component='fia/TaskList',     icon='', sort_order=1, visible=true WHERE menu_code='fia.dash';
  UPDATE ops.sys_menu SET parent_id=fia_id, menu_name='新建任务', menu_type='菜单', path='/fia/tasks/create',   component='fia/TaskCreate',   icon='', sort_order=2, visible=true WHERE menu_code='fia.tasks';
  UPDATE ops.sys_menu SET parent_id=fia_id, menu_name='检验标准', menu_type='菜单', path='/fia/stds',           component='fia/StdList',      icon='', sort_order=3, visible=true WHERE menu_code='fia.entry';
  UPDATE ops.sys_menu SET parent_id=fia_id, menu_name='触发类型', menu_type='菜单', path='/fia/triggers',       component='fia/TriggerList',  icon='', sort_order=4, visible=true WHERE menu_code='fia.trigcfg';
  UPDATE ops.sys_menu SET parent_id=fia_id, menu_name='审批单',   menu_type='菜单', path='/fia/approvals',      component='fia/ApprovalList',  icon='', sort_order=5, visible=true WHERE menu_code='fia.approve';
  UPDATE ops.sys_menu SET visible=false WHERE menu_code IN ('fia.stdlib', 'fia.trace');

  -- ===== 3) SPC 子页签 =====
  UPDATE ops.sys_menu SET parent_id=spc_id, menu_name='控制图/参数', menu_type='菜单', path='/spc/params', component='spc/ParamList',  icon='', sort_order=1, visible=true WHERE menu_code='spc.dash';
  UPDATE ops.sys_menu SET parent_id=spc_id, menu_name='数据采集',   menu_type='菜单', path='/spc/collect', component='spc/CollectView', icon='', sort_order=2, visible=true WHERE menu_code='spc.collect';
  UPDATE ops.sys_menu SET parent_id=spc_id, menu_name='告警',       menu_type='菜单', path='/spc/alarms', component='spc/AlarmList',   icon='', sort_order=3, visible=true WHERE menu_code='spc.alarm';
  UPDATE ops.sys_menu SET visible=false WHERE menu_code IN ('spc.control', 'spc.capability', 'spc.paramcfg');

  -- ===== 4) NCM 子页签(含 ncm.trend 重新挂到 ncm 根)=====
  UPDATE ops.sys_menu SET parent_id=ncm_id, menu_name='不良字典', menu_type='菜单', path='/ncm/defect-dicts', component='ncm/DefectDictList',    icon='', sort_order=1, visible=true WHERE menu_code='ncm.dash';
  UPDATE ops.sys_menu SET parent_id=ncm_id, menu_name='不良记录', menu_type='菜单', path='/ncm/defect-records', component='ncm/DefectRecordList', icon='', sort_order=2, visible=true WHERE menu_code='ncm.entry';
  UPDATE ops.sys_menu SET parent_id=ncm_id, menu_name='8D报告',   menu_type='菜单', path='/ncm/8d-reports', component='ncm/8dList',     icon='', sort_order=3, visible=true WHERE menu_code='ncm.analysis';
  UPDATE ops.sys_menu SET parent_id=ncm_id, menu_name='CAPA',     menu_type='菜单', path='/ncm/capas',     component='ncm/CapaList',   icon='', sort_order=4, visible=true WHERE menu_code='ncm.8d';
  UPDATE ops.sys_menu SET parent_id=ncm_id, menu_name='趋势报表', menu_type='菜单', path='/ncm/trend-reports', component='ncm/TrendReport', icon='', sort_order=5, visible=true WHERE menu_code='ncm.trend';
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), ncm_id, 'ncm.8d-approval-config', '8D审核配置', '菜单', '/ncm/8d-approval-config', 'ncm/8dApprovalConfig', '', 6, true
  WHERE ncm_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='ncm.8d-approval-config');

  -- ===== 5) SQM 子页签 =====
  UPDATE ops.sys_menu SET parent_id=sqm_id, menu_name='供应商档案', menu_type='菜单', path='/sqm/suppliers', component='sqm/SupplierList',    icon='', sort_order=1, visible=true WHERE menu_code='sqm.dash';
  UPDATE ops.sys_menu SET parent_id=sqm_id, menu_name='来料异常',   menu_type='菜单', path='/sqm/abnormals', component='sqm/AbnormalList',   icon='', sort_order=2, visible=true WHERE menu_code='sqm.abnormal';
  UPDATE ops.sys_menu SET parent_id=sqm_id, menu_name='供应商审核', menu_type='菜单', path='/sqm/audits',   component='sqm/AuditList',       icon='', sort_order=3, visible=true WHERE menu_code='sqm.audit';
  UPDATE ops.sys_menu SET parent_id=sqm_id, menu_name='物料变更',   menu_type='菜单', path='/sqm/changes',  component='sqm/ChangeList',      icon='', sort_order=4, visible=true WHERE menu_code='sqm.change';
  UPDATE ops.sys_menu SET parent_id=sqm_id, menu_name='物料追溯',   menu_type='菜单', path='/sqm/trace',    component='sqm/TraceList',       icon='', sort_order=5, visible=true WHERE menu_code='sqm.trace';
  UPDATE ops.sys_menu SET parent_id=sqm_id, menu_name='供应商绩效', menu_type='菜单', path='/sqm/performance', component='sqm/PerformanceList', icon='', sort_order=6, visible=true WHERE menu_code='sqm.capa';
  UPDATE ops.sys_menu SET parent_id=sqm_id, menu_name='FMEA风险管控', menu_type='菜单', path='/sqm/fmea',   component='sqm/FmeaList',        icon='', sort_order=7, visible=true WHERE menu_code='sqm.fmea';
  UPDATE ops.sys_menu SET visible=false WHERE menu_code = 'sqm.lifecycle';

  -- ===== 6) 巡检子页签(此前无子项,新增)=====
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), patrol_id, 'patrol.routes',   '巡检路线', '菜单', '/patrol/routes',   'patrol/RouteList',    '', 1, true
  WHERE patrol_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='patrol.routes');
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), patrol_id, 'patrol.tasks',    '巡检任务', '菜单', '/patrol/tasks',    'patrol/TaskList',     '', 2, true
  WHERE patrol_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='patrol.tasks');
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), patrol_id, 'patrol.abnormals', '巡检异常', '菜单', '/patrol/abnormals', 'patrol/AbnormalList', '', 3, true
  WHERE patrol_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='patrol.abnormals');

  -- ===== 7) 归档子页签 =====
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), archive_id, 'archive.list', '归档查询', '菜单', '/archive/list', 'archive/ArchiveList', '', 1, true
  WHERE archive_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='archive.list');

  -- ===== 8) 系统管理子页签:补充 审核配置(其余 system.* 由 V62 建好)=====
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), system_id, 'system.audit-config', '审核配置', '菜单', '/system/audit-config', 'sqm/AuditApprovalConfig', '', 5, true
  WHERE system_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='system.audit-config');
END $$;
