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
}
