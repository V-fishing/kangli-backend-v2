package com.konli.qms.domain.uop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 数据范围(三级:ALL/ORG_AND_SUB/SELF) */
@Data
@TableName("ops.sys_data_scope")
public class SysDataScope {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("role_id")
    private String roleId;

    @TableField("menu_id")
    private String menuId;

    @TableField("scope_type")
    private String scopeType;

    @TableField("scope_org_id")
    private String scopeOrgId;
}
