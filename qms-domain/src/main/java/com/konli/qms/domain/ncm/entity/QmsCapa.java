package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** CAPA(纠正与预防措施,由 8D 阶段或异常源触发)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.qms_capa")
public class QmsCapa extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("capa_no")
    private String capaNo;

    @TableField("d8_id")
    private String d8Id;

    @TableField("abnormal_id")
    private String abnormalId;

    private String issue;

    @TableField("trigger_stage")
    private String triggerStage;

    @TableField("trigger_type")
    private String triggerType;

    @TableField("trigger_condition")
    private String triggerCondition;

    @TableField("capa_type")
    private String capaType;

    private String rootcause;

    @TableField("action_plan")
    private String actionPlan;

    private String owner;

    @TableField("due_date")
    private LocalDate dueDate;

    private Short progress;

    private String status;

    @TableField("esign_id")
    private String esignId;
}
