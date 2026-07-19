package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 8D 阶段明细(每个 D 阶段一条;UNIQUE(d8_id, stage_code))。
 * 含审批状态/电签/证据附件;D3/D5/D7 默认需审批。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.qms_8d_stage_detail")
public class Qms8dStageDetail extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("d8_id")
    private String d8Id;

    @TableField("stage_code")
    private String stageCode;

    private String content;

    @TableField("team_members")
    private String teamMembers;

    private String owner;

    @TableField("plan_date")
    private LocalDate planDate;

    @TableField("actual_cost_hours")
    private BigDecimal actualCostHours;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("approved_by")
    private String approvedBy;

    @TableField("approved_at")
    private LocalDateTime approvedAt;

    @TableField("approval_comment")
    private String approvalComment;

    @TableField("esign_id")
    private String esignId;

    @TableField("evidence_files")
    private String evidenceFiles;
}
