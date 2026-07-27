package com.konli.qms.service.spc.dto;

import lombok.Data;

/** SPC 跨参数(供应商) CPK 对比:以参数维度聚合的过程能力指数。 */
@Data
public class SpcSupplierCpkVo {
    private String sup;
    private String mat;
    private Double cpk;
    private String lvl;
}
