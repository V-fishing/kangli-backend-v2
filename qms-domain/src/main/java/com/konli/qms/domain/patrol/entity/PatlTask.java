package com.konli.qms.domain.patrol.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 巡检任务(按路线生成,含班次+状态)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.patl_task")
public class PatlTask extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("task_no")
    private String taskNo;

    @TableField("route_id")
    private String routeId;

    private String shift;               // 早班/中班/晚班

    @TableField("plan_time")
    private LocalDateTime planTime;

    @TableField("actual_time")
    private LocalDateTime actualTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

    @TableField("inspector_id")
    private String inspectorId;

    private String status;              // 待巡检/进行中/已完成/超时

    @TableField("total_points")
    private Integer totalPoints;        // 应检点位数

    @TableField("done_points")
    private Integer donePoints;         // 已检点位数

    @TableField("abnormal_count")
    private Integer abnormalCount;      // 异常数

    private String remark;
}
