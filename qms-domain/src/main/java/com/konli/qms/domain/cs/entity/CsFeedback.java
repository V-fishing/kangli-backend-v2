package com.konli.qms.domain.cs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 客户反馈(投诉/建议/表扬/咨询),手动录入并跟踪处理。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.cs_feedback")
public class CsFeedback extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("customer_name")
    private String customerName;

    @TableField("customer_contact")
    private String customerContact;

    @TableField("fb_type")
    private String fbType;          // COMPLAINT / SUGGESTION / PRAISE / INQUIRY

    @TableField("content")
    private String content;

    @TableField("related_wo_no")
    private String relatedWoNo;

    @TableField("status")
    private String status;          // OPEN / HANDLING / DONE

    @TableField("handle_detail")
    private String handleDetail;

    @TableField("handle_at")
    private LocalDateTime handleAt;

    @TableField("owner_name")
    private String ownerName;

    @TableField("satisfaction")
    private Integer satisfaction;
}
