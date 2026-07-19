package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** SPC 报警(判异规则触发后生成,需处置/关闭) */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.spc_alarm")
public class SpcAlarm extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    private String code;

    @TableField("param_id")
    private String paramId;

    @TableField("param_name")
    private String paramName;

    @TableField("current_value")
    private BigDecimal currentValue;

    @TableField("triggered_rule")
    private String triggeredRule;

    private String level;

    @TableField("subgroup_start_no")
    private Integer subgroupStartNo;

    @TableField("subgroup_end_no")
    private Integer subgroupEndNo;

    @TableField("alarm_time")
    private LocalDateTime alarmTime;

    private String status;

    @TableField("close_reason")
    private String closeReason;

    private String disposition;

    @TableField("closed_by")
    private String closedBy;

    @TableField("closed_at")
    private LocalDateTime closedAt;

    @TableField("suppress_until")
    private LocalDateTime suppressUntil;

    @TableField("wo_no")
    private String woNo;

    @TableField("batch_no")
    private String batchNo;
}
