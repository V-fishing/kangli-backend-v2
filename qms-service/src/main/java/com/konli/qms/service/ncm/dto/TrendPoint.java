package com.konli.qms.service.ncm.dto;

import lombok.Data;

/** 单个周期的趋势数据点(含环比/同比与恶化判定)。 */
@Data
public class TrendPoint {
    private String period;
    private int defectCount;
    private int batchTotal;
    private int recordCount;
    private double defectRate;          // 百分率,如 1.23 表示 1.23%
    private Double momPct;              // 环比变化率(%)
    private Double yoyPct;              // 同比变化率(%)
    private int risingStreak;           // 截至本期的连续上升期数
    private boolean exceedMean2Sigma;   // 是否超历史均值 + kσ
    private boolean deterioration;      // 是否满足恶化判定
    private String reason;              // 恶化原因说明
}
