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
        /** 指定审批人 user_id(ops.sys_user.id);空表示不绑定特定人。 */
        private String userId;
        /** 多人会签:同一节点多名审批人 user_id 列表(后端以逗号串存 approver_id)。 */
        private List<String> userIds;
    }
}
