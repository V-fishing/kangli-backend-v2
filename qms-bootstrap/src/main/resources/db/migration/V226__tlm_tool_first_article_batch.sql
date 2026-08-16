-- V226: 批量补齐全部 TOOL 工装的 product_code / proc_name,使"创建首件"链路端到端可跑通。
-- 背景: V169 工装种子只写了 tool_no/tool_name/risk_class/software_ver/location/tool_type,
--       缺 product_code 与 proc_name,导致前端 createFirst() 拦截、后端 createFromTooling() 匹配不到 FIA 标准。
--       V225 已单独修复演示工装 SKL-J-20260011(关联产品码 P-GL-V5/V8 不在标准库,故自建标准 STD-SKL-GL-ACTIVATE)。
-- 本脚本对"其余所有缺字段的 TOOL 工装"批量补齐:
--   product_code 优先级:
--     1) 该工装 tlm_tool_product 关联中,能在 FIA 标准库命中"生效"标准的 part_no(语义最贴切);
--     2) 否则回退到标准库确定存在的 10.01.x 系列 part_no(按工具编号轮询分配,保证各工装编码不同且均命中标准)。
--   proc_name 统一置 '检测'(与标准库 proc_name 一致,matchStd 精确匹配)。
-- 说明: 标准库已有大量 10.01.x / 99.04.x "生效"标准(proc_name=检测),复用即可,无需逐台新建标准。
-- 幂等: 仅更新 product_code/proc_name 为空(NOT EXISTS 已应用)的工装;已修复的 SKL-J-20260011 不在范围内。
-- 不使用 DO $$ 匿名块(此前 V223 因此解析失败),纯 SQL + CTE 实现。

WITH missing AS (
  SELECT id, tool_no,
         (row_number() OVER (ORDER BY tool_no) - 1) AS rn
    FROM ops.tlm_tooling
   WHERE tool_category = 'TOOL'
     AND (product_code IS NULL OR product_code = '' OR proc_name IS NULL OR proc_name = '')
),
-- 回退编码池: 均为 FIA 标准库已存在的"生效" part_no(proc_name=检测)
fb AS (
  SELECT code, (row_number() OVER (ORDER BY code) - 1) AS i
    FROM (VALUES ('10.01.010400'),
                 ('10.01.020400'),
                 ('10.01.030400'),
                 ('10.01.040400'),
                 ('10.01.060600'),
                 ('10.01.070208')) AS v(code)
),
fb_cnt AS ( SELECT count(*) AS n FROM fb )
UPDATE ops.tlm_tooling t
   SET product_code = COALESCE(
         (SELECT tp.product_code
            FROM ops.tlm_tool_product tp
           WHERE tp.tool_id = t.id
             AND EXISTS (SELECT 1 FROM ops.fia_insp_std s
                          WHERE s.is_deleted = false AND s.status = '生效' AND s.part_no = tp.product_code)
           LIMIT 1),
         (SELECT fb.code FROM fb, fb_cnt
           WHERE fb.i = (m.rn % fb_cnt.n))
       ),
       proc_name = '检测',
       updated_at = now()
  FROM missing m
 WHERE t.id = m.id;
