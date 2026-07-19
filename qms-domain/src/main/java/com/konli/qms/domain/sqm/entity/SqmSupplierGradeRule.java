package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商评级规则(按分数区间映射等级)。
 * 无审计字段;org_id 可空为全局规则。
 */
@Data
@TableName("ops.sqm_supplier_grade_rule")
public class SqmSupplierGradeRule {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;                 // nullable,全局规则

    @TableField("score_min")
    private BigDecimal scoreMin;

    @TableField("score_max")
    private BigDecimal scoreMax;

    private String level;                 // CHAR(1) A/B/C/D

    @TableField("observe_first_year")
    private Boolean observeFirstYear;
}
