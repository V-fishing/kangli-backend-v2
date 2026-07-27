package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 首件工单锁定记录(SR-FIA-022~026)。
 *
 * <p>工单(woNo)为业务键:首件未完成/不合格 -> 锁定(wip_hold=true,禁止流转);
 * 重新校验通过 -> 自动解锁;质量主管紧急放行 -> 审批解锁并留痕(trace_tag 追溯标签)。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.fia_wo_lock")
public class FiaWoLock extends BaseEntity {

    @TableField("org_id")
    private String orgId;
    private String woNo;
    private String lockStatus;      // 锁定 / 正常
    private String lockReason;      // 首件未完成 / 首件不合格
    private LocalDateTime lockedAt;
    private Boolean wipHold;        // 在制品待处理
    private String unlockType;      // 自动解锁 / 紧急放行
    private LocalDateTime unlockedAt;
    private String approverId;      // 紧急放行审批人
    private String releaseReason;   // 放行原因/审批意见
    private String traceTag;        // 放行产品追溯标签
    private String taskCode;        // 触发锁定的首件校验单号
}
