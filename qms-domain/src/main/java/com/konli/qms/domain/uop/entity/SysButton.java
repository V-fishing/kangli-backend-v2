package com.konli.qms.domain.uop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 按钮。btn_code 即按钮级权限码(如 system.user.create)。
 */
@Data
@TableName("ops.sys_button")
public class SysButton {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("menu_id")
    private String menuId;

    @TableField("btn_code")
    private String btnCode;

    @TableField("btn_name")
    private String btnName;

    /** 所属菜单名(查询时由菜单表回填,DB 无此列,仅展示用)。 */
    @TableField(exist = false)
    private String menuName;
}
