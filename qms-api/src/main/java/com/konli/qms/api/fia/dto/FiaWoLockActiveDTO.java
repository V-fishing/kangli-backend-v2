package com.konli.qms.api.fia.dto;

import lombok.Data;

/**
 * 仪表盘"工单锁定"告警条数据:当前组织下锁定中且等待处置的工单(取锁定最久的一条展示)。
 */
@Data
public class FiaWoLockActiveDTO {
    /** 工单号 */
    private String woNo;
    /** 锁定原因:首件未完成 / 首件不合格 */
    private String lockReason;
    /** 锁定时间(ISO 字符串,前端据此计算已锁定时长) */
    private String lockedAt;
    /** 关联首件任务的产品名称(来自 fia_task) */
    private String productName;
    /** 关联首件任务的产线(来自 fia_task) */
    private String lineName;
    /** 触发锁定的首件校验单号 */
    private String taskCode;
}
