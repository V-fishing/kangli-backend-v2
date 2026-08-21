package com.konli.qms.service.fia.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 完工检验分阶段更新请求(复用同一结构,按端点区分业务语义):
 * - PUT /{id}/inspection : 检验员 + 判定结论(汇总数)
 * - PUT /{id}/signoff     : 数量信息 + 审核签核(表单直填,不流转)
 */
@Data
public class FinishInspectionUpdateRequest {

    /** 检验员(inspection 阶段) */
    private String inspectorName;

    /** 判定结论:合格/不合格/警告 */
    private String inspectionResult;

    /** 检验申请号(MES 单据号,本系统录入时补录) */
    private String inspectionRequestNo;

    /** 有效期至(YYYY-MM-DD) */
    private String expiryDate;

    /** 药品注册号 */
    private String drugRegNo;

    /** 性能测试方法 */
    private String perfTestMethod;

    /** 性能样本批号 */
    private String perfSampleBatchNo;

    /** 送检数(signoff 阶段) */
    private BigDecimal submittedQty;

    /** 检验数 */
    private BigDecimal inspectedQty;

    /** 合格数 */
    private BigDecimal qualifiedQty;

    /** 不合格数 */
    private BigDecimal unqualifiedQty;

    /** 单位(按物料带出,可覆盖) */
    private String unit;

    /** 质控审核结论 */
    private String qcReview;

    /** 质控审核人 */
    private String qcReviewer;

    /** 质控审核时间(YYYY-MM-DD HH:mm:ss 或 YYYY-MM-DD) */
    private String qcReviewTime;

    /** 经理审批结论 */
    private String mgrApproval;

    /** 经理代表 */
    private String mgrRepresentative;

    /** 经理审批时间 */
    private String mgrApprovalTime;

    /** 签核人 */
    private String signatureUser;

    /** 签核时间 */
    private String signatureTime;

    /** 签核结论/原因 */
    private String signatureReason;

    /** 是否有效(0/1 文本) */
    private String isValid;
}
