package com.konli.qms.api.ncm.dto;

import lombok.Data;

/** 8D 阶段推进请求 */
@Data
public class AdvanceStageRequest {

    private String stageCode;

    private String content;

    private String owner;

    /** 团队成员(D1 团队组建阶段,以"、"或","分隔的用户姓名) */
    private String teamMembers;
}
