package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 触发事件类型(org_id 可空=全局) */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.fia_trigger_type")
public class FiaTriggerType extends BaseEntity {

    @TableField("org_id")
    private String orgId;          // 可空(全局)

    private String name;

    @TableField("is_enabled")
    private Boolean isEnabled;

    private String description;
}
