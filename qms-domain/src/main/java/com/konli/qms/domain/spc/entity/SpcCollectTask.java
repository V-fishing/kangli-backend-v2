package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SPC 采集任务(按参数定义采集计划:频率/上次值/下次到期/计划停机)。
 * extends BaseEntity(有审计 + 软删 + 乐观锁)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.spc_collect_task")
public class SpcCollectTask extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("param_id")
    private String paramId;

    @TableField("collect_freq")
    private String collectFreq;

    @TableField("last_value")
    private BigDecimal lastValue;

    @TableField("last_at")
    private LocalDateTime lastAt;

    @TableField("next_due_at")
    private LocalDateTime nextDueAt;

    private String status;

    @TableField("is_planned_downtime")
    private Boolean isPlannedDowntime;

    /** 指定接收人:为空时通知回退"班组长"。 */
    @TableField("collector")
    private String collector;

    /** 临期提醒是否已发送(去重)。 */
    @TableField("due_reminded")
    private Boolean dueReminded;

    /** 采集模式: MANUAL(手动录入)/OPC(设备直连)/FILE(文件导入)/MES(MES对接)/AUTO(定时自动采集)。 */
    @TableField("collect_mode")
    private String collectMode;
}
