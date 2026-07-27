package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 来料批次(追溯根)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_incoming_lot")
public class SqmIncomingLot extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("lot_no")
    private String lotNo;

    @TableField("supplier_id")
    private String supplierId;

    @TableField("part_no")
    private String partNo;

    @TableField("part_name")
    private String partName;

    private BigDecimal qty;               // NUMERIC(14,2)

    private String unit;

    @TableField("incoming_date")
    private LocalDate incomingDate;

    @TableField("inspect_result")
    private String inspectResult;         // 合格/特采/不合格/待检

    @TableField("inspect_type")
    private String inspectType;           // 正常/加严/放宽

    @TableField("iqc_pass")
    private Boolean iqcPass;

    @TableField("po_no")
    private String poNo;

    @TableField("change_id")
    private String changeId;

    @TableField("is_key_part")
    private Boolean isKeyPart;            // 决定 SN vs 树状追溯
    @TableField("used_qty")
    private BigDecimal usedQty;           // 已投料数量(累加,防超领)

    /** 供应商名称(仅展示用，不存表) */
    @TableField(exist = false)
    private String supplierName;
}
