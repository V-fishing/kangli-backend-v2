package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 标准检测项模板(子表,无审计字段) */
@Data
@TableName("ops.fia_insp_std_item")
public class FiaInspStdItem {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("std_id")
    private String stdId;

    private Integer seq;

    @TableField("item_name")
    private String itemName;

    @TableField("is_ctq")
    private Boolean isCtq;

    @TableField("std_value")
    private String stdValue;

    private String tolerance;

    private String unit;

    @TableField("value_type")
    private String valueType;

    @TableField("enum_values")
    private String enumValues;
}
