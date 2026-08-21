package com.konli.qms.service.fia.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 物料检验(来料检验)视图对象(MES qms.material_inspection 62 列全字段对齐)。
 * 该表全 text 无主键,以应用生成/唯一存在的 record_no 作为稳定行定位键(id),用于 PUT/DELETE 精确定位行。
 */
@Data
public class MaterialInspectionVO {

    /** 行定位键(= record_no,新建行由应用生成且唯一;MES 推送行以其业务记录号,用于精确更新/软删) */
    private String id;

    private String processNo;
    private String formVersion;
    private String isCustomerSupplied;
    private String memo;
    private String materialCategory;
    private String isValid;
    private String reviewStatus;
    private String signatureStatus;
    private String isUrgent;
    private String dataRecordFlag;
    private String isInvalid;
    private String reportGenerated;
    private String recordNo;
    private String purchaseOrder;
    private String inboundNo;
    private String inspectionRequestNo;
    private String mesInspectionNo;
    private String inspectionDate;
    private String judgementDate;
    private String inspector;
    private String inspectionResult;
    private String supplierName;
    private String supplierCode;
    private String materialCode;
    private String materialName;
    private String specModel;
    private String materialBatchNo;
    private BigDecimal qualifiedQty;
    private BigDecimal unqualifiedQty;
    private BigDecimal submittedQty;
    private BigDecimal lossQty;
    private String unit;
    private String defectDesc;
    private String handlingMethod;
    private String unqualifiedFinalStatus;
    private String unqualifiedReview;
    private String unqualifiedReviewNo;
    private String inspectionCategory;
    private String arrivalDate;
    private String receivingNo;
    private String poLineNo;
    private String receivingLineNo;
    private String shelfLifeDays;
    private String reinspectRemark;
    private String judge;
    private String inspectionEndDate;
    private String reviewer;
    private String reviewDate;
    private String submitter;
    private String submitDate;
    private String remark;
    private String extId;
    private String lastModifiedBy;
    private String signatureUser;
    private String signatureTime;
    private String signatureReason;
    private String plantCode;
    private String plantName;
    private String createdBy;
    private String updatedBy;
    private String isDeleted;
    private String version;
    private String createdAt;
    private String updatedAt;
    private String materialBarcode;
}
