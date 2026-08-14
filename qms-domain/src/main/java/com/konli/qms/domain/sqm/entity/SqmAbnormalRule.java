package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 来料异常严重度判定规则:定义「严重」阈值与「一般不良累计触发 8D」窗口。
 * severeMinQty: 来料不良数 >= 该值判「严重」;
 * generalAccumDays: 一般不良累计窗口天数(同供应商+同物料);
 * generalAccumQty: 窗口内一般不良累计件数阈值,达到触发 8D。
 * 复用来料异常模块已建表 ops.sqm_abnormal_rule。
 */
@Data
@TableName("ops.sqm_abnormal_rule")
public class SqmAbnormalRule {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("severe_min_qty")
    private Integer severeMinQty;

    @TableField("general_accum_days")
    private Integer generalAccumDays;

    @TableField("general_accum_qty")
    private Integer generalAccumQty;

    @TableField("remark")
    private String remark;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
