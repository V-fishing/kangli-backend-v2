package com.konli.qms.domain.uop.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织树(代码规范§3.3,对应 ops.sys_org)。path(ltree)暂不映射,树由 parent_id 递归构建。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sys_org")
public class SysOrg extends BaseEntity {

    @TableField("org_code")
    private String orgCode;

    @TableField("org_name")
    private String orgName;

    @TableField("parent_id")
    private String parentId;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("org_type")
    private String orgType;   // 公司/工厂/车间/产线/工位

    private String status;    // 启用/停用
}
