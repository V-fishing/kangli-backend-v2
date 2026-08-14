package com.konli.qms.domain.tlm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 工装保养计划。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.tlm_maint_plan")
public class TlmMaintPlan extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("tool_id")
    private String toolId;

    @TableField("plan_no")
    private String planNo;

    @TableField("cycle_type")
    private String cycleType;   // WEEK/MONTH/YEAR

    @TableField("next_date")
    private LocalDate nextDate;

    @TableField("responsible_id")
    private String responsibleId;

    @TableField("remark")
    private String remark;
}
