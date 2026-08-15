package com.konli.qms.service.qmsmgmt;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.qmsmgmt.entity.QmsQualityGoal;

import java.util.Map;

/** 质量目标管理。qms-mgmt.goal.* */
public interface QmsQualityGoalService {

    PageResult<QmsQualityGoal> page(String keyword, String goalType, String period, int page, int size);

    QmsQualityGoal get(String id);

    QmsQualityGoal create(QmsQualityGoal goal);

    QmsQualityGoal update(QmsQualityGoal goal);

    void delete(String id);

    /** 看板统计: 各类型目标达成率、整体达成率、未达标数。 */
    Map<String, Object> stats();
}
