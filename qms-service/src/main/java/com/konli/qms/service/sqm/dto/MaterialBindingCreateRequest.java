package com.konli.qms.service.sqm.dto;

import lombok.Data;

/**
 * MES 关键物料绑定建单请求: 直写 qms.critical_material_binding。
 * 父级用实例条码(product_barcode, 对应成品/半成品 prod_batch_or_sn), 子件用 material_barcode + category。
 */
@Data
public class MaterialBindingCreateRequest {
    /** 父实例条码(成品/半成品 prod_batch_or_sn) */
    private String productBarcode;
    /** 父料号(回退标识) */
    private String productMaterialNo;
    /** 父名 */
    private String productName;
    /** 工单号 */
    private String workOrderNo;
    /** 工单数量 */
    private String workOrderQty;
    /** 工厂代码 */
    private String plantCode;
    /** 工厂名 */
    private String plantName;
    /** 子件条码 */
    private String materialBarcode;
    /** 子件料号 */
    private String materialCode;
    /** 子件名 */
    private String materialName;
    /** 型号规格 */
    private String specModel;
    /** 工序编码 */
    private String processCode;
    /** 工序名 */
    private String processName;
    /** 子件类别: 半成品 / 来料 */
    private String category;
    /** 备注 */
    private String remark;
}
