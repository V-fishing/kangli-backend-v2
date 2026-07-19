package com.konli.qms.api.patrol.dto;

import lombok.Data;

/** 提交巡检点位结果请求 */
@Data
public class SubmitRecordRequest {

    private String checkpointId;
    private String checkpointName;
    private String result;          // 正常/异常
    private String remark;
}
