package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核会签(质量/采购/研发并行 + 质量一票否决),对应 ops.sqm_audit_approval。
 * 与变更会签 SqmChangeApproval 同构,仅外键由 change_id 改为 audit_id(审核计划)。
 */
@Data
@TableName("ops.sqm_audit_approval")
public class SqmAuditApproval {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    /** 审核计划 id(外键 ops.sqm_audit_plan.id) */
    @TableField("audit_id")
    private String auditId;

    @TableField("approval_role")
    private String approvalRole; // quality/purchase/rd

    @TableField("role_label")
    private String roleLabel;

    private String status; // pending / done / rejected

    private String operator;

    @TableField("operate_date")
    private LocalDateTime operateDate;

    private String opinion;

    @TableField("has_veto")
    private Boolean hasVeto;

    @TableField("seq_order")
    private Integer seqOrder;
}
