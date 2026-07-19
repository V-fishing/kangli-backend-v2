# 补齐计划:四大模块 44 缺口

## Batch 1:表存无代码(~20 项,每项=实体+Mapper+Service+Controller)

### FIA(3 项)
1. fia_approval(审批:豁免/紧急放行/让步接收)
2. fia_trigger_type(触发事件管理)
3. fia_intercept_config(拦截配置管理)

### SPC(4 项)
4. spc_control_limit(控制限:查/算)
5. spc_collect_task(采集任务:list/标记停产)
6. spc_global_config(全局配置:get/put)
7. spc_notify_channel(通知渠道:list/toggle)

### NCM(6 项)
8. ncm_corrective_action(纠正措施 CRUD)
9. qms_8d_fishbone(鱼骨图 CRUD)
10. ncm_filter_scheme(分析方案 CRUD)
11. ncm_alert_escalation(ESC 升级配置 CRUD)
12. ncm_daily_report_config(日报配置 CRUD)
13. ncm_bi_report(BI 报表 CRUD)

### SQM(9 项)
14. sqm_supplier_cert(资质 CRUD + 过期天数计算)
15. sqm_supplier_performance(绩效 CRUD + 自动算)
16. sqm_change_strict_inspect(加严检验 CRUD)
17. sqm_supplier_measure(改善措施 CRUD)
18. sqm_sqe_verification(SQE 验证 CRUD)
19. sqm_audit_freq_rule(审核频次规则 CRUD)
20. sqm_supplier_grade_rule(评级规则 CRUD)
21. sqm_supplier_escalation(升级管理 CRUD)
22. sqm_supplier_share(份额管理 CRUD)

## Batch 2:分析报表(~10 项)
- SPC: 控制图数据接口、能力趋势、看板
- NCM: 多维分析、趋势报表、环比同比、实时看板
- SQM: 多维分析、来料看板、绩效排名
- FIA: 看板

## Batch 3:联动集成(~5 项)
- FIA↔SPC: 首件数据写入 SPC 基准
- 变更↔FIA: 变更批准后更新检验标准
- SPC↔SQM: SPC 能力数据用于绩效
- FIA: 拦截生产(首件不通过)
- SQM: 绩效分级挂钩(份额/频次/准入)

## Batch 4:预警/PDF/推送(~9 项)
- SQM: 资质过期预警(30/60/90 天)
- FIA: 归档 PDF(openhtmltopdf)
- SQM: 审核报告 PDF
- SPC: 批量导入(FastExcel)
- SPC: 告警推送通知
- NCM: ESC 三级升级
- FIA/SPC: SLA 超期扫描
- SQM: 重复问题升级审核
- FIA: 逐项签名

## 本轮:Batch 1(20 项)
用子代理批量生成,每模块一组。每组=实体+Mapper+Service+Controller+种子(如需)。
预计 20 个功能点 × 4 文件/点 ≈ 80 个文件。
