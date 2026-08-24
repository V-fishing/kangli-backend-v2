package com.konli.qms.service.sqm.dto;

import lombok.Data;

/**
 * MES 关键物料绑定编辑请求: 仅传需修改的业务字段, 未传字段保留原值。
 * 行定位键(product_barcode + material_barcode + category)不可改。
 */
@Data
public class MaterialBindingUpdateRequest {
    /** 父料号 */
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
