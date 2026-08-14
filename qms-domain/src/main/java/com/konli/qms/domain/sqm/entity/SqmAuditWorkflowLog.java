package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审核流程轨迹(计划→会签→执行→复核→归档全节点留痕),对应 ops.sqm_audit_workflow_log。
 * 详情页与执行页时间轴读取此表,形成可审计的完整流程记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_audit_workflow_log")
public class SqmAuditWorkflowLog extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("plan_id")
    private String planId;

    /** 流程节点: plan_created / start / checklist_saved / nc_added / review / archived */
    private String node;

    /** 动作描述,如 "开始执行" / "提交复核" */
    private String action;

    private String operator;

    private String remark;
}
