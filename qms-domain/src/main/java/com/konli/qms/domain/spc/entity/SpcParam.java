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

    /** CPK 标准差算法:within(组内,默认) / overall(整体)。 */
    @TableField("sigma_method")
    private String sigmaMethod;

    /** 控制限/能力指数 σ 倍数(默认 3,可配 2 / 2.5 等)。 */
    @TableField("sigma_k")
    private BigDecimal sigmaK;

    /** CPK 自动滚动更新周期:批次 / 日 / 周,null 表示不自动更新。 */
    @TableField("cpk_period")
    private String cpkPeriod;

    @TableField("is_active")
    private Boolean isActive;

    /** 关联供应商:该工序参数归属的供应商,用于 CPK→供应商质量分联动(可空)。 */
    @TableField("supplier_id")
    private String supplierId;
}
