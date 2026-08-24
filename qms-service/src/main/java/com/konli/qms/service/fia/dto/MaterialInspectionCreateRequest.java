package com.konli.qms.service.fia.dto;

import lombok.Data;

/**
 * 物料检验(来料检验)建单请求(直写 MES qms.material_inspection,无主键/唯一约束,后端 INSERT 新行)。
 * 工厂/组织按当前登录用户组织自动带入,前端可不传。
 */
@Data
public class MaterialInspectionCreateRequest {

    /** 物料编码(必填,按物料从 MES 历史带出名称/型号规格/单位/供应商) */
    private String materialCode;

    /** 物料名称(按物料带出,可覆盖) */
    private String materialName;

    /** 型号规格(按物料带出,可覆盖) */
    private String specModel;

    /** 物料批次号 */
    private String materialBatchNo;

    /** 来料条码 */
    private String materialBarcode;

    /** 供应商名称(按物料带出,可覆盖) */
    private String supplierName;

    /** 供应商编码(按物料带出,可覆盖) */
    private String supplierCode;

    /** 物料类别(material/semi/product 等) */
    private String materialCategory;

    /** 检验类别(来料/委外等) */
    private String inspectionCategory;

    /** 采购订单号(来料收货溯源) */
    private String purchaseOrder;

    /** 入库单号(来料收货溯源) */
    private String inboundNo;

    /** 到货日期(YYYY-MM-DD,来料收货溯源) */
    private String arrivalDate;

    /** 收货单号(来料收货溯源) */
    private String receivingNo;

    /** PO 行号(来料收货溯源) */
    private String poLineNo;

    /** 收货行号(来料收货溯源) */
    private String receivingLineNo;

    /** 保质期天数(来料收货溯源) */
    private String shelfLifeDays;

    /** 数量单位(按物料带出) */
    private String unit;

    /** 是否客户供料 */
    private Boolean isCustomerSupplied;

    /** 加急 */
    private Boolean isUrgent;

    /** 工厂编码(自动带入,可覆盖) */
    private String plantCode;

    /** 工厂名称(自动带入,可覆盖) */
    private String plantName;

    /** 备注 */
    private String remark;
}
