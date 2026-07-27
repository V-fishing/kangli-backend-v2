package com.konli.qms.service.spc.dto;

import lombok.Data;

import java.util.List;

/** SPC 过程能力直方图:分箱中心(bins)与对应频次(freq),并附带规格上下限、均值、标准差与正态拟合曲线。 */
@Data
public class SpcHistogramVo {
    private List<Double> bins;
    private List<Long> freq;
    private Double usl;
    private Double lsl;
    /** 全部测量值(xbar)均值。 */
    private Double mean;
    /** 整体标准差 σ。 */
    private Double sigma;
    /** 与 bins 等长的正态拟合频次(量级与 freq 一致:pdf(bin_i)*total*binWidth)。 */
    private List<Double> normalFreq;
}
