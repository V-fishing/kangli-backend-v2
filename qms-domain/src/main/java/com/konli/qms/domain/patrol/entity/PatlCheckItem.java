package com.konli.qms.domain.patrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 点位检查项(可配:枚举/数值/文本,无审计字段)。 */
@Data
@TableName("ops.patl_check_item")
public class PatlCheckItem {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("checkpoint_id")
    private String checkpointId;

    private Short seq;

    @TableField("item_name")
    private String itemName;            // 检查内容(如:温度是否正常/有无异物)

    @TableField("check_type")
    private String checkType;           // enum/numeric/text

    @TableField("std_value")
    private String stdValue;            // 标准值/合格值

    @TableField("enum_values")
    private String enumValues;          // 枚举选项(逗号分隔: 正常,异常)

    @TableField("is_required")
    private Boolean isRequired;
}
