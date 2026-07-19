package com.konli.qms.api.sqm.dto;

import lombok.Data;

/** 变更单会签审批请求 */
@Data
public class ApproveChangeRequest {

    private String approvalRole;

    private boolean approved;

    private String opinion;
}
