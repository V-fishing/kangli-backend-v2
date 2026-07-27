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
    private String lotId;                 // VARCHAR(32) 业务编号,非 FK(可能为来料批次记录 UUID)

    @TableField("batch_no")
    private String batchNo;               // 可读批次号(优先展示),V43 起填充

    @TableField("supplier_id")
    private String supplierId;

    @TableField(exist = false)
    private String supplierName;       // 关联 sqm_supplier.name,列表查询时填充

    @TableField("part_no")
    private String partNo;

    @TableField("part_name")
    private String partName;

    private String description;

    private Integer qty;                 // 不良数

    @TableField("incoming_qty")
    private Integer incomingQty;         // 来料数

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

    // ---- V21: 整改持久化 ----

    @TableField("notice_date")
    private LocalDate noticeDate;

    @TableField("notice_content")
    private String noticeContent;

    @TableField("plan_date")
    private LocalDate planDate;

    @TableField("extension_approved")
    private Boolean extensionApproved;

    @TableField("extension_date")
    private LocalDate extensionDate;

    @TableField("verify_result")
    private String verifyResult;

    @TableField("verify_comment")
    private String verifyComment;

    @TableField("verify_date")
    private java.time.LocalDateTime verifyDate;

    @TableField("verify_by")
    private String verifyBy;

    @TableField("return_reason")
    private String returnReason;

    @TableField("close_auditor")
    private String closeAuditor;
}
