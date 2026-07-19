package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** BI 固定报表(无审计字段)。 */
@Data
@TableName("ops.ncm_bi_report")
public class NcmBiReport {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("report_no")
    private String reportNo;

    @TableField("report_type")
    private String reportType;

    private String period;

    @TableField("generated_at")
    private LocalDateTime generatedAt;

    @TableField("file_url")
    private String fileUrl;

    private String status;
}
