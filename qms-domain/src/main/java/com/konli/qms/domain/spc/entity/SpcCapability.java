package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** SPC 过程能力指数(按周期聚合 Cp/Cpk/Pp/Ppk 等) */
@Data
@TableName("ops.spc_capability")
public class SpcCapability {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("param_id")
    private String paramId;

    @TableField("period_type")
    private String periodType;

    @TableField("period_value")
    private String periodValue;

    private BigDecimal cpk;

    private BigDecimal ppk;

    private BigDecimal cp;

    private BigDecimal pp;

    private String calcNote;

    private String level;

    @TableField("sample_count")
    private Integer sampleCount;

    private BigDecimal usl;

    private BigDecimal lsl;

    @TableField("calc_window_days")
    private Integer calcWindowDays;

    @TableField("calc_at")
    private LocalDateTime calcAt;
}
