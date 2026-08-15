package com.konli.qms.domain.qmsmgmt.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 不良事件(医疗器械不良事件, 法规口径)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.qms_adverse_event")
public class QmsAdverseEvent extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("event_no")
    private String eventNo;

    @TableField("event_type")
    private String eventType;       // 投诉 / 器械故障 / 伤害 / 召回 / 其他

    @TableField("occur_stage")
    private String occurStage;      // 生产 / 流通 / 使用 / 其他

    @TableField("severity")
    private String severity;        // GENERAL / SERIOUS / CRITICAL

    @TableField("occur_at")
    private LocalDateTime occurAt;

    @TableField("report_at")
    private LocalDateTime reportAt;

    @TableField("root_cause")
    private String rootCause;

    @TableField("handle_desc")
    private String handleDesc;

    @TableField("handle_timeliness")
    private String handleTimeliness; // 及时 / 逾期

    @TableField("owner")
    private String owner;            // 处理负责人

    @TableField("status")
    private String status;          // PENDING / HANDLING / DONE

    @TableField("remark")
    private String remark;
}
