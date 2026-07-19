package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 审核频次规则(按风险等级配置审核频次,org_id 可空为全局规则)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_audit_freq_rule")
public class SqmAuditFreqRule extends BaseEntity {

    @TableField("org_id")
    private String orgId;                 // nullable,全局规则

    @TableField("risk_level")
    private String riskLevel;

    private String level;                 // CHAR(1) A/B/C/D

    @TableField("freq_per_year")
    private Short freqPerYear;            // SMALLINT

    @TableField("audit_type")
    private String auditType;
}
