package com.konli.qms.domain.uop.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 通用字典(代码规范§2.1,对应 ops.sys_dict) */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sys_dict")
public class SysDict extends BaseEntity {

    @TableField("dict_type")
    private String dictType;

    @TableField("dict_key")
    private String dictKey;     // 存 DB 值(前端 value)

    @TableField("dict_value")
    private String dictValue;   // 显示文本(前端 label)

    @TableField("sort_order")
    private Integer sortOrder;

    private Boolean enabled;
}
