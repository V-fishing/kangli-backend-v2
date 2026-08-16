-- ============================================================
-- V181 测量设备 CSV 文本字段补正(领用人/设备管理员/供应商/周期)
-- 背景：V169 把"一年/一个月"误当作天数写入 calib_cycle/maint_cycle(单位应为月)，
--       且领用人/设备管理员/供应商原为纯文本，却写入了 owner_id/admin_id(UUID 列)，
--       导致详情页新增的 owner_name/admin_name/supplier_name 文本列为空。
-- 本迁移按监视和测量设备总表 CSV 真实值，把文本落到 *_name 列，并修正周期为月数。
-- 全部幂等(按 tool_no 定位，仅当文本列为空时补)，可重复执行。
-- ============================================================

UPDATE ops.tlm_tooling
SET supplier_name = '宁波某医疗器械',
    owner_name    = '莫珍弟',
    admin_name    = '莫珍弟',
    calib_cycle   = 12,
    maint_cycle   = 1
WHERE tool_no = 'A241101-597'
  AND supplier_name IS NULL;

UPDATE ops.tlm_tooling
SET supplier_name = '深圳市汇兴源科技',
    owner_name    = '黎智勇',
    admin_name    = '黎智勇',
    calib_cycle   = 12,
    maint_cycle   = 1
WHERE tool_no = 'MKL-A-20210024'
  AND supplier_name IS NULL;

UPDATE ops.tlm_tooling
SET supplier_name = '上海博讯实业有限',
    admin_name    = '莫珍弟',
    calib_cycle   = 12,
    maint_cycle   = 1
WHERE tool_no = 'MKL-C-20210001'
  AND supplier_name IS NULL;

UPDATE ops.tlm_tooling
SET supplier_name = '南京长盛仪器有限',
    admin_name    = '温桂萍',
    calib_cycle   = 12
WHERE tool_no = 'MKL-C-20210008'
  AND supplier_name IS NULL;
