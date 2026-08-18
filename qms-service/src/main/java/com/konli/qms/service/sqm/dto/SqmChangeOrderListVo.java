package com.konli.qms.service.sqm.dto;

import lombok.Data;

/** 变更单列表展示行(含供应商名 + 评估资料字段)。 */
@Data
public class SqmChangeOrderListVo {
    private String id;
    private String changeNo;
    private String title;
    private String supplierId;
    private String supplierName;
    private String partNo;
    private String changeType;
    private String urgency;
    private String status;
    private String applicant;
    private String applyDate;
    private String reason;          // 变更说明(评估资料之一)
    private String verifyReport;    // 验证报告附件路径
    private String riskFile;        // 风险评估附件路径
    private String riskPreMark;     // 风险等级
    private String oldPartNo;        // 旧料号
    private String newPartNo;        // 新料号
    private String effDate;          // 计划生效日期
    private String switchDate;       // 新旧切换日期
    private String impactDesc;       // 影响范围说明
    private Boolean customerNotify;  // 是否需通知客户
    private Boolean customerApproved;// 客户是否已批准
}
