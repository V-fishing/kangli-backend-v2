package com.konli.qms.service.fia.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 物料检验(来料检验)分阶段更新请求(复用同一结构,按端点区分业务语义):
 * - PUT /{id}/inspection : 检验员 + 判定结论 + 缺陷/处理方式
 * - PUT /{id}/signoff     : 数量信息 + 审核签核(表单直填,不流转)
 */
@Data
public class MaterialInspectionUpdateRequest {

    // ---- inspection 阶段 ----
    /** 检验员 */
    private String inspector;

    /** 判定结论:合格/不合格/让步接收等 */
    private String inspectionResult;

    /** 检验申请号(MES 单据号,本系统录入时补录) */
    private String inspectionRequestNo;

    /** MES 检验单号 */
    private String mesInspectionNo;

    /** 检验日期(YYYY-MM-DD) */
    private String inspectionDate;

    /** 判定日期(YYYY-MM-DD) */
    private String judgementDate;

    /** 检验完成日期(YYYY-MM-DD) */
    private String inspectionEndDate;

    /** 缺陷描述 */
    private String defectDesc;

    /** 处理方式(退货/让步接收/挑选等) */
    private String handlingMethod;

    /** 采购订单号(来料收货溯源,编辑可改) */
    private String purchaseOrder;

    /** 入库单号(来料收货溯源,编辑可改) */
    private String inboundNo;

    /** 到货日期(YYYY-MM-DD,来料收货溯源,编辑可改) */
    private String arrivalDate;

    /** 收货单号(来料收货溯源,编辑可改) */
    private String receivingNo;

    /** PO 行号(来料收货溯源,编辑可改) */
    private String poLineNo;

    /** 收货行号(来料收货溯源,编辑可改) */
    private String receivingLineNo;

    /** 保质期天数(来料收货溯源,编辑可改) */
    private String shelfLifeDays;

    // ---- signoff 阶段 ----
    /** 送检数 */
    private BigDecimal submittedQty;

    /** 合格数 */
    private BigDecimal qualifiedQty;

    /** 不合格数 */
    private BigDecimal unqualifiedQty;

    /** 损耗数 */
    private BigDecimal lossQty;

    /** 单位(按物料带出,可覆盖) */
    private String unit;

    /** 评审人 */
    private String reviewer;

    /** 评审日期(YYYY-MM-DD) */
    private String reviewDate;

    /** 评审结论(不评审/已评审等) */
    private String unqualifiedReview;

    /** 不合格最终处理状态 */
    private String unqualifiedFinalStatus;

    /** 不合格评审号(不合格处理阶段补录) */
    private String unqualifiedReviewNo;

    /** 判定人 */
    private String judge;

    /** 提交人 */
    private String submitter;

    /** 提交日期(YYYY-MM-DD) */
    private String submitDate;

    /** 复检备注 */
    private String reinspectRemark;

    /** 签核人 */
    private String signatureUser;

    /** 签核时间 */
    private String signatureTime;

    /** 签核结论/原因 */
    private String signatureReason;

    /** 备注 */
    private String remark;
}
