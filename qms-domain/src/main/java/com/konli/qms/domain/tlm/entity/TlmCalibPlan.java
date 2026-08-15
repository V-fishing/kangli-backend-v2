package com.konli.qms.domain.tlm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 计量校准计划单: 计量器具(GAUGE)按校准周期到期自动生成(P1)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.tlm_calib_plan")
public class TlmCalibPlan extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("tool_id")
    private String toolId;

    @TableField("tool_no")
    private String toolNo;

    @TableField("tool_name")
    private String toolName;

    @TableField("plan_cycle")
    private Integer planCycle;       // 计划校验周期(月)

    @TableField("plan_due_date")
    private LocalDate planDueDate;   // 计划到期日(应完成校准日)

    @TableField("status")
    private String status;           // PENDING/DONE/OVERDUE

    @TableField("owner_id")
    private String ownerId;          // 计量管理员(负责人)

    @TableField("source")
    private String source;           // AUTO/MANUAL

    // ===== 校准结果明细(录入后回填, 长期留存计量履历) =====
    @TableField("calib_no")
    private String calibNo;          // 校准记录流水号(系统生成: 器具号-C时间戳)

    @TableField("cert_no")
    private String certNo;           // 校准证书/报告编号(用户录入, 审计追溯)

    @TableField("calib_date")
    private LocalDate calibDate;     // 实际校准日期

    @TableField("calib_due_date")
    private LocalDate calibDueDate;  // 回写后的下次校准到期

    @TableField("calib_cycle")
    private Integer calibCycle;      // 实际采用周期(月)

    @TableField("upper_limit")
    private String upperLimit;       // 允许误差上限

    @TableField("result")
    private String result;           // 合格/限用/不合格

    @TableField("remark")
    private String remark;
}
