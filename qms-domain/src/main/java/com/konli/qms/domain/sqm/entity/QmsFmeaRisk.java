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

    @TableField("failure_effect")
    private String failureEffect;         // 失效影响

    @TableField("failure_cause")
    private String failureCause;          // 失效原因

    @TableField("current_prevent_ctrl")
    private String currentPreventCtrl;    // 现有预防控制

    @TableField("current_detect_ctrl")
    private String currentDetectCtrl;     // 现有探测控制

    @TableField("severity_s")
    private Short severityS;              // 1~10

    @TableField("occurrence_o")
    private Short occurrenceO;            // 1~10

    @TableField("detection_d")
    private Short detectionD;             // 1~10

    private Short rpn;                    // =S×O×D

    @TableField("rpn_after")
    private Short rpnAfter;               // 措施实施后重评 RPN(二次 RPN)

    @TableField("reseval_severity")
    private Short resevalSeverity;        // 措施实施后重评严重度 S(1~10)

    @TableField("reseval_occurrence")
    private Short resevalOccurrence;      // 措施实施后重评频度 O(1~10)

    @TableField("reseval_detection")
    private Short resevalDetection;       // 措施实施后重评探测度 D(1~10)

    @TableField("risk_level")
    private String riskLevel;             // 高(≥150)/中高(100~149)/中/低

    @TableField("high_risk_flag")
    private Boolean highRiskFlag;         // RPN≥100 自动置 true

    private String status;                // 待闭环/进行中/已闭环

    private String action;

    @TableField("suggest_measure")
    private String suggestMeasure;        // 建议措施

    private String owner;

    @TableField("owner_dept")
    private String ownerDept;

    @TableField("owner_dept_code")
    private String ownerDeptCode;

    @TableField("owner_user_id")
    private String ownerUserId;

    @TableField("target_date")
    private LocalDate targetDate;

    private String evidence;

    @TableField("note")
    private String note;                  // 闭环备注

    @TableField("close_date")
    private LocalDate closeDate;

    @TableField("change_order_id")
    private String changeOrderId;         // VARCHAR,非 FK

    @TableField("source_type")
    private String sourceType;            // 自动触发来源类型(NCM_DEFECT/INCOMING_ABNORMAL)

    @TableField("source_id")
    private String sourceId;              // 来源业务主键
}
