package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 产品追溯明细(1:1 关联 sqm_trace_node,按 node_id 唯一)。
 * 无审计字段。
 */
@Data
@TableName("ops.sqm_trace_product_detail")
public class SqmTraceProductDetail {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("node_id")
    private String nodeId;

    @TableField("is_urgent")
    private String isUrgent;              // VARCHAR(8) 是/否

    @TableField("qc_review")
    private String qcReview;              // VARCHAR(16) 待审/通过/驳回

    @TableField("mg_approve")
    private String mgApprove;             // VARCHAR(16) 待审/通过/驳回

    @TableField("inspect_result")
    private String inspectResult;         // VARCHAR(16) 合格/不合格

    @TableField("report_no")
    private String reportNo;

    @TableField("inspect_order_no")
    private String inspectOrderNo;

    @TableField("production_order_no")
    private String productionOrderNo;

    @TableField("material_code")
    private String materialCode;

    @TableField("product_name")
    private String productName;

    @TableField("model_spec")
    private String modelSpec;

    @TableField("batch_no")
    private String batchNo;

    @TableField("product_barcode")
    private String productBarcode;        // 产品条码=产品批号(MES 条码规则)

    @TableField("production_date")
    private LocalDate productionDate;

    @TableField("expiry_date")
    private LocalDate expiryDate;

    @TableField("inspect_qty")
    private BigDecimal inspectQty;        // NUMERIC(14,2)

    @TableField("inspect_count")
    private BigDecimal inspectCount;      // NUMERIC(14,2)

    @TableField("pass_qty")
    private BigDecimal passQty;           // NUMERIC(14,2)

    @TableField("fail_qty")
    private BigDecimal failQty;           // NUMERIC(14,2)

    private String unit;                  // VARCHAR(16)

    private String inspector;

    private String category;

    @TableField("qc_reviewer")
    private String qcReviewer;

    @TableField("qc_review_time")
    private LocalDateTime qcReviewTime;

    @TableField("mg_approver")
    private String mgApprover;

    @TableField("mg_approve_time")
    private LocalDateTime mgApproveTime;

    @TableField("drug_reg_no")
    private String drugRegNo;

    @TableField("perf_inspect_method")
    private String perfInspectMethod;

    @TableField("perf_batch_no")
    private String perfBatchNo;

    private String customer;

    @TableField("customer_code")
    private String customerCode;

    @TableField("customer_order_no")
    private String customerOrderNo;

    @TableField("ship_date")
    private LocalDate shipDate;

    @TableField("tracking_no")
    private String trackingNo;

    @TableField("ship_address")
    private String shipAddress;
}
