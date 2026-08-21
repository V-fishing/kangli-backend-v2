package com.konli.qms.service.fia.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 完工检验视图对象(MES qms.finished_goods_inspection 40 列全字段对齐)。
 * 该表全 text 无主键,以应用生成的 report_no 作为稳定行定位键(id),用于 PUT/DELETE 精确定位行。
 */
@Data
public class FinishInspectionVO {

    /** 行定位键(= report_no,应用生成且唯一;MES 表无主键,用于精确更新/软删) */
    private String id;

    private String reportNo;
    private String inspectionRequestNo;
    private String productionOrderNo;
    private String materialCode;
    private String productName;
    private String modelSpec;
    private String prodBatchOrSn;
    private String productionDate;
    private String expiryDate;
    private BigDecimal submittedQty;
    private BigDecimal inspectedQty;
    private BigDecimal qualifiedQty;
    private BigDecimal unqualifiedQty;
    private String unit;
    private String inspectorName;
    private String inspectionResult;
    private String drugRegNo;
    private String perfTestMethod;
    private String perfSampleBatchNo;
    private String qcReviewer;
    private String qcReviewTime;
    private String mgrRepresentative;
    private String mgrApprovalTime;
    private String signatureUser;
    private String signatureTime;
    private String signatureReason;
    private String qcReview;
    private String mgrApproval;
    private String isUrgent;
    private String isEntrusted;
    private String isValid;
    private String category;
    private String plantCode;
    private String plantName;
    private String createdBy;
    private String updatedBy;
    private String createdAt;
    private String updatedAt;
}
