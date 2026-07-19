package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 8D 报告主表(D1-D8 阶段推进式问题解决)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.qms_8d_report")
public class Qms8dReport extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("d8_no")
    private String d8No;

    private String source;

    @TableField("source_ref_id")
    private String sourceRefId;

    private String issue;

    private String severity;

    @TableField("current_stage")
    private String currentStage;

    private String status;

    @TableField("flow_type")
    private String flowType;

    private String team;

    @TableField("capa_triggered")
    private Boolean capaTriggered;

    @TableField("close_date")
    private LocalDate closeDate;
}
