package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * FMEA 高风险项(跨模块共享,RPN≥100 入清单)。
 * RPN = S×O×D;RPN≥100 自动置 high_risk_flag=true。
 * change_order_id 为 VARCHAR(非 FK),关联物料变更评估。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.qms_fmea_risk")
public class QmsFmeaRisk extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("risk_no")
    private String riskNo;

    @TableField("fmea_type")
    private String fmeaType;              // DFMEA/PFMEA

    private String product;

    private String process;

    @TableField("failure_mode")
    private String failureMode;

    @TableField("severity_s")
    private Short severityS;              // 1~10

    @TableField("occurrence_o")
    private Short occurrenceO;            // 1~10

    @TableField("detection_d")
    private Short detectionD;             // 1~10

    private Short rpn;                    // =S×O×D

    @TableField("risk_level")
    private String riskLevel;             // 高(≥150)/中高(100~149)/中/低

    @TableField("high_risk_flag")
    private Boolean highRiskFlag;         // RPN≥100 自动置 true

    private String status;                // 待闭环/进行中/已闭环

    private String action;

    private String owner;

    @TableField("target_date")
    private LocalDate targetDate;

    private String evidence;

    @TableField("close_date")
    private LocalDate closeDate;

    @TableField("change_order_id")
    private String changeOrderId;         // VARCHAR,非 FK
}
