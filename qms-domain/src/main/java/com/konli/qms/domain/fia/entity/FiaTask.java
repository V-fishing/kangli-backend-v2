package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 首件检验任务(校验单)。双签名:inspector_id(检验人)+ reviewer_id(复核人,V08 加)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.fia_task")
public class FiaTask extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    private String code;
    @TableField("wo_no")
    private String woNo;
    @TableField("line_name")
    private String lineName;
    @TableField("product_name")
    private String productName;
    @TableField("proc_name")
    private String procName;
    @TableField("trigger_type")
    private String triggerType;
    @TableField("std_id")
    private String stdId;
    @TableField("std_version")
    private String stdVersion;
    private String aql;
    @TableField("sample_size")
    private Integer sampleSize;
    @TableField("sample_count")
    private Integer sampleCount;
    @TableField("batch_no")
    private String batchNo;
    private String status;            // 待检/进行中/待复核/已完成/超时/已作废
    @TableField("overall_judge")
    private String overallJudge;      // 合格/警告/不合格
    @TableField("inspector_id")
    private String inspectorId;
    @TableField("is_urgent")
    private Boolean isUrgent;
    @TableField("sla_due_at")
    private LocalDateTime slaDueAt;
    @TableField("is_overdue")
    private Boolean isOverdue;
    private String disposition;
    private String remark;
    @TableField("submitted_at")
    private LocalDateTime submittedAt;
    @TableField("reviewer_id")
    private String reviewerId;        // V08 加
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt; // V08 加
    @TableField("approver_id")
    private String approverId;        // V09 加(三级签名第三签)
    @TableField("approved_at")
    private LocalDateTime approvedAt; // V09 加
}
