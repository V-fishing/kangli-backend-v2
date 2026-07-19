package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SPC 全局配置(按公司一行,org_id 可空表示全局默认)。
 * chart_auto_rules: DB 为 VARCHAR[],代码层按 String 逗号分隔存储(不映射数组)。
 * plain 实体(无审计)。
 */
@Data
@TableName("ops.spc_global_config")
public class SpcGlobalConfig {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("baseline_mode")
    private String baselineMode;

    @TableField("default_subgroup_size")
    private Integer defaultSubgroupSize;

    /** DB 为 VARCHAR[],代码层以逗号分隔字符串存储(代码规范:不映射为数组)。 */
    @TableField("chart_auto_rules")
    private String chartAutoRules;

    @TableField("cpk_period")
    private String cpkPeriod;

    @TableField("cpk_sufficient")
    private BigDecimal cpkSufficient;

    @TableField("cpk_acceptable")
    private BigDecimal cpkAcceptable;

    @TableField("spec_source")
    private String specSource;

    @TableField("alert_level")
    private String alertLevel;

    @TableField("suppress_minutes")
    private Integer suppressMinutes;
}
