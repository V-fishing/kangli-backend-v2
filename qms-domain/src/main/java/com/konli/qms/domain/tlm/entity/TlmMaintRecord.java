package com.konli.qms.domain.tlm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 工装保养记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.tlm_maint_record")
public class TlmMaintRecord extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("plan_id")
    private String planId;

    @TableField("tool_id")
    private String toolId;

    @TableField("maint_date")
    private LocalDate maintDate;

    @TableField("result")
    private String result;

    @TableField("responsible_id")
    private String responsibleId;

    @TableField("attachment")
    private String attachment;
}
