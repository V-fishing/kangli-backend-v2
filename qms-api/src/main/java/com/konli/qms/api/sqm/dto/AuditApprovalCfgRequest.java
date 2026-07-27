package com.konli.qms.api.sqm.dto;

import lombok.Data;

import java.util.List;

/** 保存审核会签配置请求。 */
@Data
public class AuditApprovalCfgRequest {
    private String auditType;
    private List<AuditorItem> auditors;

    @Data
    public static class AuditorItem {
        private String role;
        private String label;
        private boolean veto;
    }
}
