package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核会签配置:按审核类型(audit_type)配置默认会签人员及其否决权。
 * auditors 为 JSON 数组:[{"role":"zhiliang","label":"质量主管","veto":true}, ...]
 * 由管理员在「审核人员配置」页面维护,替代原先写死的 质量/采购/研发。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_audit_approval_cfg")
public class SqmAuditApprovalCfg extends BaseEntity {
    @TableField("org_id")
    private String orgId;
    @TableField("audit_type")
    private String auditType;
    @TableField("auditors")
    private String auditors;
}
