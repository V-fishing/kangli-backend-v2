package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商绩效指标配置(权重/阈值可配置,calc 读取而非写死)。
 * metric_code: INCOMING_PASS / DELIVERY / QUALITY / RECTIFY / COMPLIANCE
 */
@Data
@TableName("ops.sqm_perf_metric_cfg")
public class SqmPerfMetricCfg {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("metric_code")
    private String metricCode;

    @TableField("metric_name")
    private String metricName;

    @TableField("weight")
    private BigDecimal weight;          // 权重,求和归一

    @TableField("target")
    private BigDecimal target;          // 达标阈值

    @TableField("challenge")
    private BigDecimal challenge;       // 挑战阈值

    @TableField("enabled")
    private Boolean enabled;            // 是否参与计算

    @TableField("auto_linkage")
    private Boolean autoLinkage;        // 分级是否联动份额/状态

    @TableField("is_deleted")
    private Boolean isDeleted;
}
