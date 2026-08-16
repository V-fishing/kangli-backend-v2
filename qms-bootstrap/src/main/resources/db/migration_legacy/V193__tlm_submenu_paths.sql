-- 修正 TLM 子菜单 path 为绝对路径, 保证二级 tab 栏 .active 高亮与下划线滑动生效
-- (V187 中 scraps/repairs/metro 误写为相对路径, 导致 router-link 能跳转但 .active 匹配失败, tab-ink 不滑动)
UPDATE ops.sys_menu SET path = '/tlm/scraps'  WHERE menu_code = 'tlm.scrap.list'  AND path <> '/tlm/scraps';
UPDATE ops.sys_menu SET path = '/tlm/repairs' WHERE menu_code = 'tlm.repair.list' AND path <> '/tlm/repairs';
UPDATE ops.sys_menu SET path = '/tlm/metro'   WHERE menu_code = 'tlm.metro.list'  AND path <> '/tlm/metro';
