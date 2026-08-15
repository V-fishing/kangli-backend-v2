package com.konli.qms.domain.qmsmgmt.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 内审不符合项(含整改)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.qms_audit_nc")
public class QmsAuditNc extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("audit_id")
    private String auditId;         // 关联内审计划

    @TableField("nc_no")
    private String ncNo;

    @TableField("nc_desc")
    private String ncDesc;

    @TableField("clause")
    private String clause;          // 对应条款/标准

    @TableField("severity")
    private String severity;        // MAJOR / MINOR / OBSERVATION

    @TableField("status")
    private String status;          // OPEN / IN_PROGRESS / CLOSED

    @TableField("owner")
    private String owner;           // 责任部门/人

    @TableField("due_date")
    private LocalDateTime dueDate;

    @TableField("corrective")
    private String corrective;      // 纠正/纠正措施

    @TableField("verify_result")
    private String verifyResult;    // 验证结果

    @TableField("closed_at")
    private LocalDateTime closedAt;
}
