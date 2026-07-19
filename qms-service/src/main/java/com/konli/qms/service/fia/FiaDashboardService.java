package com.konli.qms.service.fia;

import java.util.Map;

/** FIA 看板(今日任务/完成数、合格率、超时数、状态分布、近7天趋势)。fia.task.list */
public interface FiaDashboardService {

    Map<String, Object> dashboard();
}
