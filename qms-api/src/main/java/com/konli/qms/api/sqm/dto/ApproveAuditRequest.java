package com.konli.qms.api.sqm.dto;

import lombok.Data;

/** 审核会签审批请求 */
@Data
public class ApproveAuditRequest {

    private String approvalRole;

    private boolean approved;

    private String opinion;
}
