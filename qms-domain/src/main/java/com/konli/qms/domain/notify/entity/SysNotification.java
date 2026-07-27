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
    private String link;
    private Boolean isRead;

    @TableField("created_at")
    private LocalDateTime createTime;
}
