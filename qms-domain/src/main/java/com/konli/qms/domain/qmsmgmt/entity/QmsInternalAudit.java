package com.konli.qms.domain.qmsmgmt.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 内审计划。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.qms_internal_audit")
public class QmsInternalAudit extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("audit_no")
    private String auditNo;

    @TableField("audit_name")
    private String auditName;

    @TableField("audit_scope")
    private String auditScope;

    @TableField("plan_date")
    private LocalDateTime planDate;

    @TableField("auditor")
    private String auditor;

    @TableField("status")
    private String status;          // PLANNED / ONGOING / DONE / CLOSED

    @TableField("remark")
    private String remark;
}
