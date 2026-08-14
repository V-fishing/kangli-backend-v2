package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 原料追溯明细(1:1 关联 sqm_trace_node,按 node_id 唯一)。
 * 无审计字段。
 */
@Data
@TableName("ops.sqm_trace_raw_detail")
public class SqmTraceRawDetail {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("node_id")
    private String nodeId;

    @TableField("batch_no")
    private String batchNo;                // 组件批号(SON_LOT_NO)

    private String category;

    @TableField("wo_no")
    private String woNo;

    @TableField("product_barcode")
    private String productBarcode;

    @TableField("product_part_no")
    private String productPartNo;

    @TableField("product_name")
    private String productName;

    @TableField("wo_qty")
    private BigDecimal woQty;             // NUMERIC(14,2)

    @TableField("material_barcode")
    private String materialBarcode;

    @TableField("material_code")
    private String materialCode;

    @TableField("material_name")
    private String materialName;

    @TableField("spec_model")
    private String specModel;

    private String scanner;

    @TableField("scan_time")
    private LocalDateTime scanTime;

    @TableField("process_code")
    private String processCode;

    @TableField("process_name")
    private String processName;
}
