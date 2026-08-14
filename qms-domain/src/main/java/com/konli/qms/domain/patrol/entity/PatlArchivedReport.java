package com.konli.qms.domain.patrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/** 巡检任务归档报告(15 年留存,无审计字段)。 */
@Data
@TableName("ops.patl_archived_report")
public class PatlArchivedReport {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("archive_no")
    private String archiveNo;       // "PT-" + taskNo

    @TableField("task_id")
    private String taskId;          // 关联 patl_task.id

    @TableField("task_no")
    private String taskNo;          // 快照

    @TableField("route_id")
    private String routeId;         // 快照

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
