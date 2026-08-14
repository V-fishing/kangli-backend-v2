package com.konli.qms.domain.tlm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 工装报废单(接入统一审批中心)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.tlm_scrap")
public class TlmScrap extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("tool_id")
    private String toolId;

    @TableField("scrap_no")
    private String scrapNo;

    @TableField("scrap_method")
    private String scrapMethod;   // RECYCLE/DESTROY/REPAIR

    @TableField("reason")
    private String reason;

    private String status;   // PENDING/APPROVED/REJECTED/DONE

    @TableField("approver_id")
    private String approverId;

    @TableField("approval_id")
    private String approvalId;

    /** 非持久化: 列表展示用,由 scrapPage 回填关联工装信息。 */
    @TableField(exist = false)
    private String toolNo;

    @TableField(exist = false)
    private String toolName;
}
