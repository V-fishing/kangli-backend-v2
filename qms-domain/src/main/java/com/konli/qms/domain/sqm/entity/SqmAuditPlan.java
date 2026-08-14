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

    /** 审核组长用户 ID,支撑按当前登录用户聚合"我的任务"(V161 加) */
    @TableField("audit_lead_user_id")
    private String auditLeadUserId;

    @TableField("auditor_team")
    private String auditorTeam;

    /** 实际参与审核人(会签执行人),签字后由后端同步,用于「审核组栏」反映真实参与人 */
    @TableField("actual_auditors")
    private String actualAuditors;

    private String scope;

    @TableField("risk_level")
    private String riskLevel;             // 高/中/低,驱动频次

    @TableField("actual_date")
    private LocalDate actualDate;

    private String status;

    @TableField("record_id")
    private String recordId;

    /** JSONB -> String 映射(不加 typeHandler),存放各审核类型特有字段。 */
    @TableField("ext_json")
    private String extJson;

    /** 来源变更单 id(仅「物料变更审核」类型由变更单提交联动生成时填充,用于双向追溯)。 */
    @TableField("change_id")
    private String changeId;
}
