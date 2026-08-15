package com.konli.qms.service.qmsmgmt;

import java.util.Map;

/** 体系合规监控看板(跨模块聚合分析)。qms-mgmt.dashboard.* 复用 CS 反馈数据。 */
public interface QmsComplianceBoardService {

    /** 综合看板: 质量目标达成率 / 内审不符合项关闭率 / 不良事件处理率 / 顾客反馈满意度。 */
    Map<String, Object> board();
}
