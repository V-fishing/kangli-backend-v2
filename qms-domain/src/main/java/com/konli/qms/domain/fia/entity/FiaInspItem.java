package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 检验项目录入(子表,无审计字段;CTQ 超差整单不合格) */
@Data
@TableName("ops.fia_insp_item")
public class FiaInspItem {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("task_id")
    private String taskId;

    private Integer seq;

    @TableField("item_name")
    private String itemName;

    @TableField("is_ctq")
    private Boolean isCtq;

    @TableField("std_value")
    private String stdValue;

    private String tolerance;

    private String unit;

    @TableField("measured_value")
    private String measuredValue;

    private String judge;             // 合格/不合格/-

    @TableField("std_item_id")
    private String stdItemId;
}
