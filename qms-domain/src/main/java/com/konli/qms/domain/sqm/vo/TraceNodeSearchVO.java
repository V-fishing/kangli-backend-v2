package com.konli.qms.domain.sqm.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 全局追溯节点检索结果:除节点本身字段外,附带根来料批次号与供应商名,
 * 便于在"不限定具体来料批次"的前提下直接检索 半成品/物料/客户 等对象。
 */
@Data
public class TraceNodeSearchVO {

    private String id;
    private String rootLotId;
    private String rootLotNo;          // sqm_incoming_lot.lot_no
    private String nodeType;           // incoming/raw/semi/ship/customer
    private String nodeName;
    private String batchNo;
    private String materialCode;
    private BigDecimal qty;
    private String unit;
    private String nodeDate;           // yyyy-MM-dd
    private String supplierId;
    private String supplierName;       // sqm_supplier.name
    private String remark;
    private Integer treeLevel;
    private String isValid;            // 是/否
}
