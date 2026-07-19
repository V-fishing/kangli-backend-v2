package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 供应商审核计划(年度复审/过程审核/专项审核/飞行检查)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_audit_plan")
public class SqmAuditPlan extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("plan_no")
    private String planNo;

    @TableField("supplier_id")
    private String supplierId;

    @TableField("audit_type")
    private String auditType;

    @TableField("plan_date")
    private LocalDate planDate;

    @TableField("audit_lead")
    private String auditLead;

    @TableField("auditor_team")
    private String auditorTeam;

    private String scope;

    @TableField("risk_level")
    private String riskLevel;             // 高/中/低,驱动频次

    @TableField("actual_date")
    private LocalDate actualDate;

    private String status;

    @TableField("record_id")
    private String recordId;
}
