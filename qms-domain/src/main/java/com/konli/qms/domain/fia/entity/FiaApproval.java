package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 首件审批(豁免/紧急放行/让步接收)。status:待审批/已通过/已驳回。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.fia_approval")
public class FiaApproval extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    private String code;
    @TableField("approval_type")
    private String approvalType;       // 豁免/紧急放行/让步接收
    @TableField("wo_no")
    private String woNo;
    @TableField("task_id")
    private String taskId;
    private String reason;
    @TableField("applicant_id")
    private String applicantId;
    @TableField("apply_at")
    private LocalDateTime applyAt;
    private String status;             // 待审批/已通过/已驳回
    @TableField("approver_id")
    private String approverId;
    @TableField("approve_opinion")
    private String approveOpinion;
    @TableField("approve_at")
    private LocalDateTime approveAt;
    @TableField("esign_id")
    private String esignId;
}
