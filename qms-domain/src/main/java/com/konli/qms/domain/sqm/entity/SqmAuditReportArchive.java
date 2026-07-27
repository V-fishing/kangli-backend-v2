package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 审核报告归档(15 年留存,无审计字段)。
 * <p>由 {@code SqmAuditReportArchiveService.generatePdf} 生成 PDF + SHA-256 hash 后落库。</p>
 */
@Data
@TableName("ops.sqm_audit_report_archive")
public class SqmAuditReportArchive {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("archive_no")
    private String archiveNo;

    @TableField("record_id")
    private String recordId;

    @TableField("plan_id")
    private String planId;

    @TableField("supplier_id")
    private String supplierId;

    @TableField("report_file_path")
    private String reportFilePath;

    @TableField("report_hash")
    private String reportHash;

    @TableField("assembled_at")
    private LocalDateTime assembledAt;

    @TableField("archive_date")
    private LocalDateTime archiveDate;

    @TableField("retention_until")
    private LocalDate retentionUntil;
}
