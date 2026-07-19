package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 变更会签(质量/采购/研发并行 + 质量一票否决 + 试产串行)。
 * 无审计字段子表;UNIQUE(change_id, approval_role)。
 */
@Data
@TableName("ops.sqm_change_approval")
public class SqmChangeApproval {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("change_id")
    private String changeId;

    @TableField("approval_role")
    private String approvalRole;          // quality/purchase/rd/trial

    @TableField("role_label")
    private String roleLabel;

    private String status;                // pending/doing/done/rejected

    private String operator;

    @TableField("operate_date")
    private LocalDateTime operateDate;

    private String opinion;

    @TableField("has_veto")
    private Boolean hasVeto;              // 仅 quality=true 一票否决

    @TableField("seq_order")
    private Integer seqOrder;             // 三方并行=0;trial=1

    @TableField("esign_id")
    private String esignId;
}
