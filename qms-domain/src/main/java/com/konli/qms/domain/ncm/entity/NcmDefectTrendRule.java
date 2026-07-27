package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 不良趋势恶化判定规则(可配置,支持全局/按组织)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.ncm_defect_trend_rule")
public class NcmDefectTrendRule extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    /** 连续上升天数阈值 N(默认 3)。 */
    @TableField("consecutive_days")
    private Integer consecutiveDays;

    /** 是否启用"超历史均值 + kσ"判定(默认 true)。 */
    @TableField("use_mean_plus_2sigma")
    private Boolean useMeanPlus2sigma;

    /** σ 倍数 k(默认 2.0)。 */
    @TableField("sigma_multiplier")
    private BigDecimal sigmaMultiplier;

    /** 基线窗口天数(用于计算历史均值/标准差,默认 30)。 */
    @TableField("baseline_days")
    private Integer baselineDays;

    @TableField("enabled")
    private Boolean enabled;
}
