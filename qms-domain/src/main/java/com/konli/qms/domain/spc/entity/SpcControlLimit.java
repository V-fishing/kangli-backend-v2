package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SPC 控制限(前25子组建基线,动态更新)。
 * 一行 = 一个参数的当前激活基线;calc 时旧记录 isActive=false。
 * plain 实体(无审计)。
 */
@Data
@TableName("ops.spc_control_limit")
public class SpcControlLimit {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("param_id")
    private String paramId;

    @TableField("chart_type")
    private String chartType;

    @TableField("baseline_source")
    private String baselineSource;

    @TableField("n_subgroups")
    private Integer nSubgroups;

    @TableField("xbar_ucl")
    private BigDecimal xbarUcl;

    @TableField("xbar_cl")
    private BigDecimal xbarCl;

    @TableField("xbar_lcl")
    private BigDecimal xbarLcl;

    @TableField("r_ucl")
    private BigDecimal rucl;

    @TableField("r_cl")
    private BigDecimal rcl;

    @TableField("r_lcl")
    private BigDecimal rlcl;

    @TableField("calc_at")
    private LocalDateTime calcAt;

    /** 是否人工覆盖:true 时该基线优先级高于自动计算,控制图直接采用其 UCL/CL/LCL。 */
    @TableField("manual")
    private Boolean manual;

    @TableField("is_active")
    private Boolean isActive;
}
