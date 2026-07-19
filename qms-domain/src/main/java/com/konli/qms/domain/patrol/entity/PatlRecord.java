package com.konli.qms.domain.patrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 巡检记录(点位级,含检查项明细 JSONB,无审计字段)。 */
@Data
@TableName("ops.patl_record")
public class PatlRecord {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("task_id")
    private String taskId;

    @TableField("checkpoint_id")
    private String checkpointId;

    @TableField("checkpoint_name")
    private String checkpointName;      // 快照

    private String result;              // 正常/异常

    @TableField("check_time")
    private LocalDateTime checkTime;

    @TableField("operator_id")
    private String operatorId;

    @TableField("photo_ref")
    private String photoRef;            // MinIO key(如有照片)

    private String remark;

    @TableField("item_results")
    private String itemResults;         // JSONB [{itemId, itemName, value, result}]
}
