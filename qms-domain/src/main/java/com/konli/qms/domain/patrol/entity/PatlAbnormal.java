package com.konli.qms.domain.patrol.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 巡检异常(可转 NCM/8D)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.patl_abnormal")
public class PatlAbnormal extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("task_id")
    private String taskId;

    @TableField("record_id")
    private String recordId;

    @TableField("checkpoint_name")
    private String checkpointName;

    private String description;         // 异常描述

    private String severity;            // 严重/一般

    private String status;              // 待处理/已转NCM/已关闭

    @TableField("d8_id")
    private String d8Id;                // 转8D

    @TableField("ncm_record_id")
    private String ncmRecordId;         // 转NCM不良记录

    @TableField("handle_remark")
    private String handleRemark;

    @TableField("handled_by")
    private String handledBy;

    @TableField("handled_at")
    private LocalDateTime handledAt;
}
