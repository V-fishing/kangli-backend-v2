package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * SPC 抽样批次任务(量产过程监控 ROUTINE 采集载体)。
 * 与首件(FIA 任务带入)平行:手工创建后,在其下列表跳转采集多批子组。
 * 工单×料号×工序三元组归集,标准自动带出 SpcParam。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.spc_sample_task")
public class SpcSampleTask extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("wo_no")
    private String woNo;

    @TableField("part_no")
    private String partNo;

    @TableField("proc_name")
    private String procName;

    @TableField("product_name")
    private String productName;

    @TableField("param_id")
    private String paramId;

    @TableField("target_count")
    private Integer targetCount;

    @TableField("current_count")
    private Integer currentCount;

    /** 采集中 / 已结案 */
    @TableField("status")
    private String status;

    @TableField("cpk")
    private BigDecimal cpk;

    /** 达标标记:CPK 跌破门槛则 false(软告警,不卡停产) */
    @TableField("released")
    private Boolean released;

    /** CPK 软告警标志 */
    @TableField("alarm_flag")
    private Boolean alarmFlag;

    /** 触发类型(如 量产/换线/客诉) */
    @TableField("trigger_type")
    private String triggerType;

    /** 品类 material/semi/product */
    @TableField("category")
    private String category;

    /** 供应商 UUID(物料类必填) */
    @TableField("supplier_id")
    private String supplierId;

    /** 供应商名称(冗余,展示用) */
    @TableField("supplier_name")
    private String supplierName;

    /** 加急 */
    @TableField("is_urgent")
    private Boolean urgent;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
