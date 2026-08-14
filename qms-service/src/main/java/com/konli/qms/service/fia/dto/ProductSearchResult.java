package com.konli.qms.service.fia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 产品料号模糊搜索结果 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResult {
    /** 料号 */
    private String partNo;
    /** 产品名称 */
    private String productName;
    /** 库中是否已存在该产品 */
    private boolean exists;
    /** 库中已存在的匹配供应商ID(仅exists=true时有值) */
    private String matchedSupplierId;
    /** 库中已存在的匹配供应商名称 */
    private String matchedSupplierName;
    /** 产品品类: material(物料) / semi(半成品) / product(成品) */
    private String category;
}
