package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** SPC 标准线管理:按(组织+物料+工序)定义规格上下限/目标值/单位 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.spc_spec_standard")
public class SpcSpecStandard extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    /** 物料名称 */
    private String material;

    /** 工序名称 */
    @TableField("proc_name")
    private String procName;

    /** 规格下限(LSL) */
    @TableField("spec_lower")
    private BigDecimal specLower;

    /** 规格上限(USL) */
    @TableField("spec_upper")
    private BigDecimal specUpper;

    /** 目标值(设计中心值) */
    @TableField("target_value")
    private BigDecimal targetValue;

    /** 单位(mm/g/...) */
    private String unit;

    /** 默认控制图类型:Xbar-R/Xbar-S/I-MR/P */
    @TableField("chart_type")
    private String chartType;
}
