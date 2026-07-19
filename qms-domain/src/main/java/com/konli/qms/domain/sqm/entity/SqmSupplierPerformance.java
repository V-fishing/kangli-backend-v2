package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商绩效(按周期统计,自动分级)。
 * 无审计字段;UNIQUE(supplier_id, period)。
 */
@Data
@TableName("ops.sqm_supplier_performance")
public class SqmSupplierPerformance {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("supplier_id")
    private String supplierId;

    private String period;                // CHAR(7) 如 2026-07

    private BigDecimal score;             // NUMERIC(5,2)

    @TableField("delivery_score")
    private BigDecimal deliveryScore;

    @TableField("quality_score")
    private BigDecimal qualityScore;

    @TableField("service_score")
    private BigDecimal serviceScore;

    @TableField("incoming_pass_rate")
    private BigDecimal incomingPassRate;

    @TableField("defect_rate")
    private BigDecimal defectRate;

    @TableField("rectify_timely_rate")
    private BigDecimal rectifyTimelyRate;

    @TableField("delivery_timely_rate")
    private BigDecimal deliveryTimelyRate;

    @TableField("compliance_rate")
    private BigDecimal complianceRate;

    private String level;                 // CHAR(1) A/B/C/D

    @TableField("observe_flag")
    private Boolean observeFlag;          // 首年观察期不分级

    @TableField("data_missing_flag")
    private Boolean dataMissingFlag;      // 数据缺失标志
}
