package com.konli.qms.domain.qmsmgmt.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 质量目标(目标值 + 实际填报值, 达成率=实际/目标, 后端算)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.qms_quality_goal")
public class QmsQualityGoal extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("goal_name")
    private String goalName;

    @TableField("goal_type")
    private String goalType;        // QUALITY / DELIVERY / SATISFACTION / COST / OTHER

    @TableField("period")
    private String period;          // 统计周期(如 2026Q1 / 2026年度)

    @TableField("target_value")
    private BigDecimal targetValue;

    @TableField("actual_value")
    private BigDecimal actualValue;

    @TableField("unit")
    private String unit;            // % / 天 / 分 / 件

    @TableField("owner")
    private String owner;           // 责任人

    @TableField("deadline")
    private LocalDateTime deadline;

    @TableField("remark")
    private String remark;
}
