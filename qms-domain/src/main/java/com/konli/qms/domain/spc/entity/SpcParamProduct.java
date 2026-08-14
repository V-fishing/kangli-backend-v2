package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SPC 参数-产品关联(多对多)。
 * 一个 SPC 参数可绑定多个产品(物料/件号),每条关联带 kind 区分物料/成品。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.spc_param_product")
public class SpcParamProduct extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("param_id")
    private String paramId;

    @TableField("product_name")
    private String productName;

    @TableField("part_no")
    private String partNo;

    /** 类型: material(物料/来料首件) / product(成品/产线首件);由 FIA 任务 source 派生或手动指定 */
    @TableField("kind")
    private String kind;
}
