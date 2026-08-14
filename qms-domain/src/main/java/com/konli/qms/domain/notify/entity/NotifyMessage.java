package com.konli.qms.domain.notify.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 点对点通知发送记录(ops.notify_message)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.notify_message")
public class NotifyMessage extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    /** 发送人用户 ID */
    @TableField("sender_id")
    private String senderId;

    @TableField("sender_name")
    private String senderName;

    /** 接收人用户 ID */
    @TableField("receiver_id")
    private String receiverId;

    @TableField("receiver_name")
    private String receiverName;

    /** 接收人类型: user / role(预留) */
    @TableField("receiver_type")
    private String receiverType;

    /** 渠道名, 如 钉钉应用消息 / 邮件 */
    private String channel;

    /** 渠道类型: direct / webhook(预留) */
    @TableField("channel_type")
    private String channelType;

    private String title;

    private String content;

    /** 业务类型: 8D / CAPA / CA / MANUAL */
    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private String bizId;

    @TableField("biz_no")
    private String bizNo;

    /** 发送状态: 发送中 / 成功 / 失败 */
    private String status;

    @TableField("fail_reason")
    private String failReason;

    @TableField("send_time")
    private LocalDateTime sendTime;
}
