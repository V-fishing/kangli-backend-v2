package com.konli.qms.domain.uop.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单(树形)。menu_code 即菜单级权限码(如 system.user)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sys_menu")
public class SysMenu extends BaseEntity {

    @TableField("parent_id")
    private String parentId;

    @TableField("menu_code")
    private String menuCode;

    @TableField("menu_name")
    private String menuName;

    @TableField("menu_type")
    private String menuType;   // 目录/菜单/按钮

    private String path;

    private String component;

    private String icon;

    @TableField("sort_order")
    private Integer sortOrder;

    private Boolean visible;
}
