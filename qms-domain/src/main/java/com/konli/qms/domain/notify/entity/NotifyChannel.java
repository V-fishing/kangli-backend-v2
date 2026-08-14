package com.konli.qms.domain.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 全局通知外部渠道(钉钉/企微/自定义 webhook/站内弹窗/点对点渠道)。
 * webhook_url 为空字符串表示未配置; 站内弹窗无需 webhook。
 * channel_type=webhook 为群机器人渠道(走 ExternalNotifyService), direct 为点对点渠道(走 DirectNotifyService)。
 */
@Data
@TableName("ops.notify_channel")
public class NotifyChannel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String channel;

    @TableField("webhook_url")
    private String webhookUrl;

    @TableField("is_enabled")
    private Boolean isEnabled;

    private String level;

    private String remark;

    /** 渠道类型: webhook=群机器人 / direct=点对点 */
    @TableField("channel_type")
    private String channelType;

    /** 渠道凭据 JSON(secret 字段返回时脱敏为 ****) */
    @TableField("config_json")
    private String configJson;
}
