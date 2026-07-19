package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** SPC 参数主数据(控制对象定义:规格限/子组大小/采集频率/图表类型) */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.spc_param")
public class SpcParam extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("param_name")
    private String paramName;

    @TableField("proc_name")
    private String procName;

    private String unit;

    @TableField("spec_lower")
    private BigDecimal specLower;

    @TableField("spec_upper")
    private BigDecimal specUpper;

    @TableField("spec_text")
    private String specText;

    @TableField("target_value")
    private BigDecimal targetValue;

    @TableField("subgroup_size")
    private Integer subgroupSize;

    @TableField("collect_freq")
    private String collectFreq;

    @TableField("chart_type")
    private String chartType;

    @TableField("is_active")
    private Boolean isActive;
}
