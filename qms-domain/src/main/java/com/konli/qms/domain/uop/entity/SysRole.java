package com.konli.qms.domain.uop.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sys_role")
public class SysRole extends BaseEntity {

    @TableField("role_code")
    private String roleCode;

    @TableField("role_name")
    private String roleName;

    @TableField("role_type")
    private String roleType;   // 预置/自定义

    @TableField("perm_desc")
    private String permDesc;

    private String status;     // 启用/停用
}
