package com.konli.qms.domain.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 统一通知配置: 模块(module) × 事件(event_code) 唯一对应一行,
 * 记录该事件通知的接收角色(role_codes)、具体接收人(receiver_ids)与外部渠道(channels)及启停(enabled)。
 * 接收人 = 按角色解析出的所有用户 ∪ 具体接收人, 取并集去重。
 */
@Data
@TableName("ops.notify_config")
public class NotifyConfig {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String module;

    @TableField("event_code")
    private String eventCode;

    @TableField("event_name")
    private String eventName;

    @TableField("role_codes")
    private String roleCodes;

    /** 具体接收人用户ID(逗号分隔, 与角色并存取并集) */
    @TableField("receiver_ids")
    private String receiverIds;

    private String channels;

    private Boolean enabled;

    @TableField("org_id")
    private String orgId;
}
