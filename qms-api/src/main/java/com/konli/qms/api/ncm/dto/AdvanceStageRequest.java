package com.konli.qms.api.ncm.dto;

import lombok.Data;

/** 8D 阶段推进请求 */
@Data
public class AdvanceStageRequest {

    private String stageCode;

    private String content;

    private String owner;
}
