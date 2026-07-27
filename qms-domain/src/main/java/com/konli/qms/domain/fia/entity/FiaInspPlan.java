package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 来料检验计划:物料分类→工序→标准→AQL抽样方案 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.fia_insp_plan")
public class FiaInspPlan extends BaseEntity {

    @TableField("org_id")
    private String orgId;
    @TableField("plan_code")
    private String planCode;
    @TableField("plan_name")
    private String planName;
    @TableField("material_category")
    private String materialCategory;
    @TableField("supplier_id")
    private String supplierId;
    @TableField("proc_name")
    private String procName;
    @TableField("std_id")
    private String stdId;
    private BigDecimal aql;
    @TableField("sample_level")
    private String sampleLevel;
    @TableField("sample_plan")
    private String samplePlan;
    private String severity;
    @TableField("is_active")
    private Boolean isActive;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("is_default")
    private Boolean isDefault;      // 通用默认检验计划(兜底)
}
