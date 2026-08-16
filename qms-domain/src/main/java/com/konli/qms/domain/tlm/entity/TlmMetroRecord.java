package com.konli.qms.domain.tlm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 计量数据采集记录: 计量器具(GAUGE)实际测量值录入, 绑定工单/批次, 留存计量追溯。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.tlm_metro_record")
public class TlmMetroRecord extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("tool_id")
    private String toolId;

    @TableField("tool_no")
    private String toolNo;

    @TableField("tool_name")
    private String toolName;

    @TableField("wo_no")
    private String woNo;

    @TableField("batch_no")
    private String batchNo;

    @TableField("measure_point")
    private String measurePoint;

    @TableField("measure_value")
    private String measureValue;

    @TableField("measure_unit")
    private String measureUnit;

    @TableField("standard_value")
    private String standardValue;

    @TableField("upper_limit")
    private String upperLimit;

    @TableField("lower_limit")
    private String lowerLimit;

    @TableField("judged")
    private String judged;          // 合格/不合格

    @TableField("measure_time")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;

    @TableField("operator")
    private String operator;

    @TableField("remark")
    private String remark;
}
