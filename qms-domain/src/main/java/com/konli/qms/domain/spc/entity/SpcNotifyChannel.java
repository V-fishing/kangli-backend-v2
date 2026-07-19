package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * SPC 通知渠道(邮件/短信/钉钉等,org_id 可空表示全局)。
 * config_json: DB 为 JSONB,代码层按 String 存储。
 * plain 实体(无审计)。
 */
@Data
@TableName("ops.spc_notify_channel")
public class SpcNotifyChannel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    private String channel;

    @TableField("is_enabled")
    private Boolean isEnabled;

    @TableField("config_json")
    private String configJson;
}
