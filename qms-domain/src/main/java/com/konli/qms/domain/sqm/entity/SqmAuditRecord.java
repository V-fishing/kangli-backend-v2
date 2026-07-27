package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 供应商审核记录(关联审核计划,产出不符合项)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_audit_record")
public class SqmAuditRecord extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("record_no")
    private String recordNo;

    @TableField("plan_id")
    private String planId;

    @TableField("supplier_id")
    private String supplierId;

    @TableField("audit_type")
    private String auditType;

    @TableField("audit_date")
    private LocalDate auditDate;

    @TableField("audit_lead")
    private String auditLead;

    @TableField("auditor_team")
    private String auditorTeam;

    private String result;                // 通过/有条件通过/不通过

    private BigDecimal score;             // NUMERIC(5,2)

    @TableField("nc_count")
    private Integer ncCount;

    private String conclusion;

    private String status;

    @TableField("archive_id")
    private String archiveId;

    /** JSONB -> String 映射(不加 typeHandler),存放审核记录类型特有字段。 */
    @TableField("ext_json")
    private String extJson;
}
