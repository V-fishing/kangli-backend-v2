package com.konli.qms.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用审批中心:待我审批条目(跨模块聚合)。
 */
@Data
public class PendingApprovalDTO {
    /** 审批记录主键 */
    private String id;
    /** 业务模块: FIA / NCM / SQM */
    private String module;
    /** 审批类型(中文) */
    private String bizType;
    /** 业务单号 */
    private String bizNo;
    /** 展示标题 */
    private String title;
    /** 申请人/责任人口径 */
    private String applicant;
    /** 发起时间 */
    private LocalDateTime appliedAt;
    /** 前端跳转路由 */
    private String url;
}
