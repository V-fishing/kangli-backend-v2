package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SPC 采集任务(按参数定义采集计划:频率/上次值/下次到期/计划停机)。
 * extends BaseEntity(有审计 + 软删 + 乐观锁)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.spc_collect_task")
public class SpcCollectTask extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("param_id")
    private String paramId;

    @TableField("collect_freq")
    private String collectFreq;

    @TableField("last_value")
    private BigDecimal lastValue;

    @TableField("last_at")
    private LocalDateTime lastAt;

    @TableField("next_due_at")
    private LocalDateTime nextDueAt;

    private String status;

    @TableField("is_planned_downtime")
    private Boolean isPlannedDowntime;
}
