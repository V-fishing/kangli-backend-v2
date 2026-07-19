package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/** 归档报告(15 年留存,无审计字段) */
@Data
@TableName("ops.fia_archived_report")
public class FiaArchivedReport {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("report_no")
    private String reportNo;

    @TableField("task_id")
    private String taskId;

    @TableField("wo_no")
    private String woNo;

    @TableField("archive_date")
    private LocalDate archiveDate;

    private String status;

    @TableField("pdf_ref")
    private String pdfRef;            // MinIO key(占位,实际 PDF 生成待 openhtmltopdf)

    @TableField("report_hash")
    private String reportHash;        // SHA-256 入哈希链

    @TableField("retention_until")
    private LocalDate retentionUntil; // 归档日+15年
}
