package com.konli.qms.domain.patrol.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 巡检路线(配置:频次+关联工序)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.patl_route")
public class PatlRoute extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("route_code")
    private String routeCode;

    @TableField("route_name")
    private String routeName;

    @TableField("proc_name")
    private String procName;            // 关联工序(注塑/焊接/组装...)

    private String freq;                // 频次: 1次/班/1次/天/1次/周

    private String status;              // 启用/停用
}
