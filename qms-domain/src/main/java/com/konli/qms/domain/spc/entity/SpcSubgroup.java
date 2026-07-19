package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SPC 子组(分区表,仅 insert + select,不做 update/delete)。
 * 一个子组聚合 n 个测量值,产出 xbar / rangeR。
 */
@Data
@TableName("ops.spc_subgroup")
public class SpcSubgroup {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("param_id")
    private String paramId;

    @TableField("subgroup_no")
    private Integer subgroupNo;

    @TableField("subgroup_time")
    private LocalDateTime subgroupTime;

    private String shift;

    private Integer n;

    private BigDecimal xbar;

    @TableField("range_r")
    private BigDecimal rangeR;

    private String judge;

    @TableField("is_outlier")
    private Boolean isOutlier;

    @TableField("outlier_rule")
    private String outlierRule;     // 命中判异规则编号(①-⑧)

    @TableField("data_source")
    private String dataSource;

    @TableField("operator_id")
    private String operatorId;

    @TableField("wo_no")
    private String woNo;

    @TableField("batch_no")
    private String batchNo;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("created_by")
    private String createdBy;
}
