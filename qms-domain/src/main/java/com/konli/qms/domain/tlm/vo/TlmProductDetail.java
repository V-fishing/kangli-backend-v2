package com.konli.qms.domain.tlm.vo;

import lombok.Data;

/** 产品在某 MES 检验表的记录明细。 */
@Data
public class TlmProductDetail {
    private String kind;          // MATERIAL / SEMI / FINISHED
    private String sourceTable;   // qms.material_inspection / qms.finished_goods_inspection
    private String materialCode;
    private String productName;
    private String specModel;
    private String batchNo;       // 物料批次 / 成品批号
    private String supplierName;
    private String inspectionResult;
    private String inspectionDate;
    private String productionOrderNo;
    private String qty;           // 提交/检验数量
    private String unit;
    private String inspector;
    private String plantName;
}
