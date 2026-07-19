package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 审核不符合项(分级整改,严重项现场复核 + 连续3批闭环)。
 * 关联审核记录,产出整改措施与验证结论。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_audit_nc")
public class SqmAuditNc extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("nc_no")
    private String ncNo;

    @TableField("record_id")
    private String recordId;

    @TableField("supplier_id")
    private String supplierId;

    private String clause;

    private String description;

    private String level;                 // 严重/一般/观察项

    private String status;

    private String responsible;

    private LocalDate deadline;

    @TableField("rectify_measure")
    private String rectifyMeasure;

    @TableField("rectify_attachment")
    private String rectifyAttachment;     // JSONB -> String

    @TableField("rectify_date")
    private LocalDateTime rectifyDate;

    @TableField("need_site_review")
    private Boolean needSiteReview;       // 严重项强制现场复核

    @TableField("verify_result")
    private String verifyResult;          // 通过/不通过

    @TableField("verify_comment")
    private String verifyComment;

    @TableField("verify_date")
    private LocalDateTime verifyDate;

    @TableField("verify_by")
    private String verifyBy;

    @TableField("verified_batches")
    private Integer verifiedBatches;      // 连续3批合格闭环

    @TableField("close_date")
    private LocalDateTime closeDate;
}
