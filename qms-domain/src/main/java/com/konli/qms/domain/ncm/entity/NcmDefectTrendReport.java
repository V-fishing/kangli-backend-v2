package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 不良趋势报表快照(定时/手动生成后落库,供查看与历史追溯)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.ncm_defect_trend_report")
public class NcmDefectTrendReport extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("product_model")
    private String productModel;

    /** day / week / month。 */
    @TableField("granularity")
    private String granularity;

    /** 2026-07-26 / 2026-W30 / 2026-07。 */
    @TableField("period_value")
    private String periodValue;

    @TableField("summary_json")
    private String summaryJson;

    @TableField("rule_snapshot")
    private String ruleSnapshot;

    @TableField("generated_at")
    private java.time.LocalDateTime generatedAt;
}
