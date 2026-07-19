package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 任务时间线(日志,无审计字段) */
@Data
@TableName("ops.fia_task_log")
public class FiaTaskLog {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("task_id")
    private String taskId;

    @TableField("node_seq")
    private Integer nodeSeq;

    @TableField("node_name")
    private String nodeName;

    @TableField("op_time")
    private LocalDateTime opTime;

    private String operator;

    @TableField("op_type")
    private String opType;            // 系统/人工

    @TableField("is_done")
    private Boolean isDone;
}
