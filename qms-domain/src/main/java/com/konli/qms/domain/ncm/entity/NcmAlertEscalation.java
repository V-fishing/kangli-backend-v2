package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 告警升级配置(ESC 三级升级,通用;无审计字段)。 */
@Data
@TableName("ops.ncm_alert_escalation")
public class NcmAlertEscalation {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 可空(全局配置时 org_id 为 null) */
    @TableField("org_id")
    private String orgId;

    private Short level;

    @TableField("timeout_minutes")
    private Integer timeoutMinutes;

    @TableField("notify_role")
    private String notifyRole;

    @TableField("off_hours_delay")
    private Boolean offHoursDelay;
}
