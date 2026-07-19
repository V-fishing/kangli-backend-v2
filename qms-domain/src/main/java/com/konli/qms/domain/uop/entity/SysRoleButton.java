package com.konli.qms.domain.uop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 角色-按钮(无审计字段) */
@Data
@TableName("ops.sys_role_button")
public class SysRoleButton {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("role_id")
    private String roleId;

    @TableField("button_id")
    private String buttonId;
}
