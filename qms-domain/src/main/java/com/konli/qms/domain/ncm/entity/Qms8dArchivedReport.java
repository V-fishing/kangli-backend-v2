package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/** 8D 整改报告归档(15 年留存,无审计字段)。 */
@Data
@TableName("ops.qms_8d_archived_report")
public class Qms8dArchivedReport {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("archive_no")
    private String archiveNo;       // "8D-" + d8No

    @TableField("report_id")
    private String reportId;        // 关联 qms_8d_report.id

    @TableField("d8_no")
    private String d8No;            // 快照

    @TableField("archive_date")
    private LocalDate archiveDate;

    private String status;          // "已归档"

    @TableField("pdf_ref")
    private String pdfRef;          // 本地路径或 placeholder://

    @TableField("report_hash")
    private String reportHash;      // SHA-256

    @TableField("retention_until")
    private LocalDate retentionUntil; // 归档日+15年
}
