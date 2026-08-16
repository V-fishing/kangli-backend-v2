-- V89: spc_param_product.param_id 外键加 ON DELETE CASCADE
-- V87 引入了 spc_param_product -> spc_param 的外键。种子/演示数据初始化会对 spc_param 做物理 DELETE,
-- 若无级联会因子表关联行被外键拦截导致启动失败。加级联后删除参数自动清理其产品关联,避免孤儿行与启动异常。
ALTER TABLE ops.spc_param_product DROP CONSTRAINT IF EXISTS spc_param_product_param_id_fkey;
ALTER TABLE ops.spc_param_product ADD CONSTRAINT spc_param_product_param_id_fkey
  FOREIGN KEY (param_id) REFERENCES ops.spc_param(id) ON DELETE CASCADE;
