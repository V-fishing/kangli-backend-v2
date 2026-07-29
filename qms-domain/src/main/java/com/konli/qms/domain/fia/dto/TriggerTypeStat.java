package com.konli.qms.domain.fia.dto;

import lombok.Data;

/**
 * 触发类型统计:每个触发类型对应的首件任务聚合数据。
 * 由 {@code FiaTriggerTypeService.stats()} 合并触发类型列表与 ops.fia_task 分组计数得到。
 */
@Data
public class TriggerTypeStat {
    /** 触发类型 id(来自 ops.fia_trigger_type) */
    private String id;
    /** 触发类型名称(来自 ops.fia_trigger_type.name) */
    private String name;
    /** 关联任务总数 */
    private Long taskCount = 0L;
    /** 综合判定=合格 */
    private Long qualifiedCount = 0L;
    /** 综合判定=不合格 */
    private Long unqualifiedCount = 0L;
    /** 进行中(状态非 已完成/已作废) */
    private Long pendingCount = 0L;
    /** 已逾期(is_overdue=true) */
    private Long overdueCount = 0L;
}
