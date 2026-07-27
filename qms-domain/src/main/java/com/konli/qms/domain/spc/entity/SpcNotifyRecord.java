package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * SPC 推送通知记录(报警触发后按启用渠道生成,留痕以满足 FDA/CNAS 可追溯要求)。
 * plain 实体(无审计),继承 BaseEntity 复用 id/created_at 等。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.spc_notify_record")
public class SpcNotifyRecord extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("alarm_id")
    private String alarmId;

    private String channel;

    @TableField("channel_name")
    private String channelName;

    @TableField("message")
    private String message;

    /** SENT / FAILED / PENDING */
    private String status;

    @TableField("sent_at")
    private LocalDateTime sentAt;

    private String error;
}
