package com.konli.qms.service.fia.dto;

import lombok.Data;

/**
 * 完工检验 + 物料检验 合并行(列表「全部」档使用)。
 * 两套 MES 表字段取并集:srcType 区分 finish/material,row_id 即各自行定位键。
 */
@Data
public class MergedInspectionVO {

    /** finish=完工检验 / material=物料检验 */
    private String srcType;

    /** 行定位键(report_no 或 record_no) */
    private String id;

    private String reportNo;
    private String recordNo;
    private String productionOrderNo;
    private String materialCode;
    private String productName;
    private String materialName;
    private String supplierName;
    private String materialBatchNo;
    private String category;
    private String inspectionResult;
    private String signatureUser;
    private String qcReviewer;
    private String reviewer;
    private String inspectedQty;
    private String createdAt;
}
