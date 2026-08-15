package com.konli.qms.domain.cs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 售后工单(工单流程控制)。状态机 PENDING->ASSIGNED->DONE->CLOSED。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.cs_work_order")
public class CsWorkOrder extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("order_no")
    private String orderNo;

    @TableField("customer_name")
    private String customerName;

    @TableField("customer_contact")
    private String customerContact;

    @TableField("wo_type")
    private String woType;          // INSTALL / REPAIR

    @TableField("priority")
    private String priority;        // URGENT / NORMAL / LOW

    @TableField("product_name")
    private String productName;

    @TableField("fault_desc")
    private String faultDesc;

    @TableField("status")
    private String status;          // PENDING / ASSIGNED / DONE / CLOSED

    @TableField("owner_id")
    private String ownerId;

    @TableField("owner_name")
    private String ownerName;

    @TableField("assign_at")
    private LocalDateTime assignAt;

    @TableField("handle_detail")
    private String handleDetail;

    @TableField("handle_at")
    private LocalDateTime handleAt;

    @TableField("close_at")
    private LocalDateTime closeAt;

    /** 满意度评分 1~5(客户满意度模块下一轮回填)。 */
    @TableField("satisfaction")
    private Integer satisfaction;

    @TableField("satisfaction_comment")
    private String satisfactionComment;

    @TableField("expect_time")
    private LocalDateTime expectTime;

    @TableField("address")
    private String address;
}
