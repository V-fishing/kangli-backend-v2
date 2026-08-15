package com.konli.qms.domain.tlm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 工装-工单绑定(寿命计数触发)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.tlm_tool_wo_bind")
public class TlmToolWoBind extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("tool_id")
    private String toolId;

    @TableField("wo_no")
    private String woNo;

    @TableField("bound_at")
    private LocalDateTime boundAt;

    @TableField("unbound_at")
    private LocalDateTime unboundAt;

    /** 绑定时的计量校准状态快照(GAUGE 器具追溯用): 校准单号/校准日期/到期日期。 */
    @TableField("calib_no")
    private String calibNo;

    @TableField("calib_date")
    private LocalDate calibDate;

    @TableField("calib_due_date")
    private LocalDate calibDueDate;
}
