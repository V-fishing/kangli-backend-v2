package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 供应商升级管理(重复问题递进动作,通知升级)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_supplier_escalation")
public class SqmSupplierEscalation extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("supplier_id")
    private String supplierId;

    @TableField("current_level")
    private String currentLevel;          // CHAR(1) A/B/C/D

    @TableField("quality_issue_count_6m")
    private Integer qualityIssueCount6m;

    @TableField("repeat_problem_count")
    private Integer repeatProblemCount;

    @TableField("suggested_action")
    private String suggestedAction;

    @TableField("escalation_status")
    private String escalationStatus;

    @TableField("escalation_action")
    private String escalationAction;

    @TableField("notice_sent_flag")
    private Boolean noticeSentFlag;
}
