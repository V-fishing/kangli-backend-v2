package com.konli.qms.domain.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 全平台站内消息通知(收件箱)。 */
@Data
@TableName("ops.sys_notification")
public class SysNotification {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String orgId;
    private String userId;
    private String userName;
    private String title;
    private String content;
    private String bizType;
    private String bizId;
    /** 可读业务单据号(如 8D-1786002805953); 调用方未传时为空, 通知中心回退展示 bizId */
    @TableField("biz_no")
    private String bizNo;
    private String link;
    /** 首选渠道(如 站内弹窗 / 钉钉应用消息); 通知中心聚合投递明细时作为主渠道展示 */
    private String channel;
    private Boolean isRead;

    @TableField("created_at")
    private LocalDateTime createTime;
}
