package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 供应商档案(主数据,基础一期需,绩效评级二期)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_supplier")
public class SqmSupplier extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("supplier_no")
    private String supplierNo;

    @TableField("supplier_code")
    private String supplierCode;

    private String name;

    @TableField("credit_code")
    private String creditCode;

    private String category;

    private String level;                 // CHAR(1) A/B/C/D(绩效自动分级,二期)

    private String status;

    private BigDecimal score;             // NUMERIC(5,2)

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    private String address;

    private String certs;                 // JSONB -> String

    @TableField("last_audit_date")
    private LocalDate lastAuditDate;

    @TableField("next_audit_date")
    private LocalDate nextAuditDate;

    @TableField("observe_flag")
    private Boolean observeFlag;          // 首年观察期不分级

    @TableField("sole_source_flag")
    private Boolean soleSourceFlag;       // 独家供应
}
