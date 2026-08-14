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

    /** 通用来源单据 ID（内审不符合项 / 不良趋势异常记录 等未单独建字段的触发来源）。 */
    @TableField("source_ref_id")
    private String sourceRefId;

    /** 来源类型（如 审核不符合项 / 不良记录 / 8D / 来料异常），用于详情页/弹窗跳转。 */
    @TableField("source_type")
    private String sourceType;

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

    /** 负责人用户 ID,支撑按当前登录用户聚合"我的任务"(V158 加) */
    @TableField("owner_user_id")
    private String ownerUserId;

    @TableField("due_date")
    private LocalDate dueDate;

    private Short progress;

    private String status;

    @TableField("esign_id")
    private String esignId;
}
