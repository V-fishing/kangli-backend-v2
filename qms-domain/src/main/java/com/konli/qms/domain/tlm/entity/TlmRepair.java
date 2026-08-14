package com.konli.qms.domain.tlm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 工装维修工单。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.tlm_repair")
public class TlmRepair extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("tool_id")
    private String toolId;

    @TableField("repair_no")
    private String repairNo;

    @TableField("fault_desc")
    private String faultDesc;

    @TableField("measure")
    private String measure;

    private String status;   // PENDING/REPAIRING/DONE/VERIFYING/VERIFIED

    @TableField("approver_id")
    private String approverId;

    @TableField("verify_task_id")
    private String verifyTaskId;
}
