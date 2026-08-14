package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 不良记录发起 8D/CAPA/CA 时的指派处理人记录(含通知方式,可追溯)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.qms_assign_record")
public class QmsAssignRecord extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    /** 不良记录 ID */
    @TableField("defect_id")
    private String defectId;

    /** 不良记录编号(冗余,便于展示) */
    @TableField("defect_no")
    private String defectNo;

    /** 8D / CAPA / CA */
    @TableField("biz_type")
    private String bizType;

    /** 报告 ID */
    @TableField("biz_id")
    private String bizId;

    /** 报告编号(d8_no / capa_no / ca_no) */
    @TableField("biz_no")
    private String bizNo;

    /** 被指派用户 ID(指派角色时为空) */
    @TableField("assignee_user_id")
    private String assigneeUserId;

    @TableField("assignee_user_name")
    private String assigneeUserName;

    /** 被指派角色码(指派用户时为空) */
    @TableField("assignee_role_code")
    private String assigneeRoleCode;

    @TableField("assignee_role_name")
    private String assigneeRoleName;

    /** 通知方式(逗号分隔渠道名,如 站内弹窗,钉钉) */
    @TableField("notify_channels")
    private String notifyChannels;

    /** 指派人(当前操作人) */
    @TableField("assigner_id")
    private String assignerId;

    /** 指派动作: assign=首次指派, reassign=改派(V160 加) */
    @TableField("action")
    private String action;

    private String remark;
}
