package com.konli.qms.service.cs.dto;

import lombok.Data;

/**
 * 客户反馈触发质量改进纠正措施(需求 2.4.2.5 闭环升级)。
 * 替代原"手动填写 NCM ID"的弱联动, 由系统在反馈页直接创建 8D / CAPA / CA 并回填来源。
 */
@Data
public class TriggerNcmRequest {
    /** 触发类型: 8D / CAPA / CA */
    private String type;
    /** 问题描述(默认带反馈内容)。 */
    private String issue;
    /** 负责人用户 ID(8D/CAPA/CA 均可指定)。 */
    private String ownerUserId;
    /** 负责人姓名(文本快照, 可选)。 */
    private String ownerName;
    /** 期限(CAPA/CA 可选)。 */
    private String dueDate;
}
