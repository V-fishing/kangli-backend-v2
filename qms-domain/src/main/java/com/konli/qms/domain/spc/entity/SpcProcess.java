package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** SPC 工序主数据(参数的父级分组维度:装配/焊接/检测/系统...)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.spc_process")
public class SpcProcess extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("process_name")
    private String processName;

    @TableField("process_code")
    private String processCode;

    @TableField("description")
    private String description;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("is_active")
    private Boolean isActive;
}
