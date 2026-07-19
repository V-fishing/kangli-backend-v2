package com.konli.qms.api.patrol.dto;

import lombok.Data;

/** 创建巡检任务请求(按路线生成) */
@Data
public class CreateTaskRequest {

    private String orgId;
    private String routeId;
    private String shift;
    private String planTime;       // ISO LocalDateTime 字符串
}
