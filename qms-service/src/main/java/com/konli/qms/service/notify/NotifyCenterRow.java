package com.konli.qms.service.notify;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知中心聚合行: 以「通知」为粒度(同一 notification_id 的多条投递明细归并为一条),
 * 便于前端统一展示每一条通知的所有渠道投递结果。
 */
@Data
public class NotifyCenterRow {
    /** 聚合主键: notification_id(非空) 或 明细自身 id(手动外发无主记录) */
    private String id;
    /** 发送时间(取该通知最新一条明细的发送时间) */
    private LocalDateTime sendTime;
    private String senderName;
    private String receiverName;
    private String receiverId;
    private String title;
    private String bizType;
    private String bizId;
    private String bizNo;
    /** 主渠道(取首条明细渠道, 通常为 站内弹窗) */
    private String channel;
    /** 综合状态: 全部成功=成功; 任一失败=失败; 任一发送中=发送中 */
    private String status;
    /** 该通知的全部投递明细(站内弹窗 / 钉钉 / 邮件 ...) */
    private List<Delivery> deliveries;

    @Data
    public static class Delivery {
        private String channel;
        private String status;
        private String failReason;
    }
}
