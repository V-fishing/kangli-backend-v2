package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * FMEA 高风险闭环跟踪日志：每次状态流转(识别 → 措施分配 → 措施验证 → 闭环)写入一条记录。
 * 对应表 ops.qms_fmea_risk_track。
 */
@Data
@TableName("ops.qms_fmea_risk_track")
public class QmsFmeaRiskTrack {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("risk_id")
    private String riskId;

    @TableField("from_status")
    private String fromStatus;

    @TableField("to_status")
    private String toStatus;

    private String operator;

    @TableField("operate_time")
    private LocalDateTime operateTime;

    @TableField("action_note")
    private String actionNote;

    private String evidence;

    @TableField("esign_id")
    private String esignId;
}
