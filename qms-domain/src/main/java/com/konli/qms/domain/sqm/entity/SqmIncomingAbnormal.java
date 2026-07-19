package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 来料异常整改单(严重1件/一般≥3件触发)。
 * lot_id 为 VARCHAR(32) 业务编号(非 FK);可关联 8D/CAPA 闭环。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_incoming_abnormal")
public class SqmIncomingAbnormal extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("abnormal_no")
    private String abnormalNo;

    @TableField("lot_id")
    private String lotId;                 // VARCHAR(32) 业务编号,非 FK

    @TableField("supplier_id")
    private String supplierId;

    @TableField("part_no")
    private String partNo;

    @TableField("part_name")
    private String partName;

    private String description;

    private Integer qty;

    private String level;                 // 严重/一般

    @TableField("occur_date")
    private LocalDate occurDate;

    @TableField("handler_id")
    private String handlerId;

    private String status;

    private String disposal;              // 退货/特采/挑选使用/报废

    @TableField("disposal_remark")
    private String disposalRemark;

    @TableField("d8_id")
    private String d8Id;

    @TableField("capa_id")
    private String capaId;

    @TableField("rectify_type")
    private String rectifyType;           // 8D/CAPA

    @TableField("overdue_days")
    private Integer overdueDays;

    @TableField("close_date")
    private LocalDate closeDate;
}
