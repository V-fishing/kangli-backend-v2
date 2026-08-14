-- V173: 修复 TLM 子菜单 path 改为绝对路由路径(与 V65 约定及前端 router 一致)。
-- 原 V170 把子菜单 path 写成相对路径('tooling'/'maint'/'abnormals'),
-- 但前端 RouterLink 直接以节点 path 渲染、不做拼接,相对路径会被
-- 解析到当前路由下(如 /tlm/tooling/:id/tooling),导致二级菜单点击无效、
-- 详情页无 tab 高亮。改为绝对路径解决。

UPDATE ops.sys_menu SET path = '/tlm/tooling'   WHERE menu_code = 'tlm.tooling.list';
UPDATE ops.sys_menu SET path = '/tlm/maint'     WHERE menu_code = 'tlm.maint.list';
UPDATE ops.sys_menu SET path = '/tlm/abnormals' WHERE menu_code = 'tlm.abnormal.list';
