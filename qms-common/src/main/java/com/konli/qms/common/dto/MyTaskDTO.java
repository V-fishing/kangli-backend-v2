package com.konli.qms.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跨模块"我的任务"聚合条目(被指派/作为负责人且未闭环的任务)。
 * 对应 service.my.MyTaskService.myTasks 的返回。
 */
@Data
public class MyTaskDTO {
    /** 业务模块: FIA / NCM / PATROL */
    private String module;
    /** 任务类型(中文,如 首件检验/8D/CAPA/巡检) */
    private String taskType;
    /** 业务单号(如 FA-xxx / 8D-xxx) */
    private String bizNo;
    /** 展示标题 */
    private String title;
    /** 状态 */
    private String status;
    /** 指派/负责人 */
    private String assignee;
    /** 截止时间(若有) */
    private LocalDateTime dueAt;
    /** 前端跳转路由 */
    private String url;
}
