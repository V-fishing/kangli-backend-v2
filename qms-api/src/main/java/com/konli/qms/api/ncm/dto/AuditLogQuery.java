package com.konli.qms.api.ncm.dto;

import lombok.Data;

/** 操作轨迹查询请求:按记录 ID 查该 8D/缺陷报告的全流程操作留痕。 */
@Data
public class AuditLogQuery {

    /** 被操作记录 ID(8D 报告主键 / 缺陷记录主键),必填 */
    private String recordId;

    /** 模块过滤,可选,如 NCM */
    private String module;

    /** 动作过滤,可选,如 CREATE/ADVANCE/APPROVE/REOPEN/ARCHIVE */
    private String action;

    /** 页码,1-based,默认 1 */
    private int page = 1;

    /** 每页条数,默认 50 */
    private int size = 50;
}
