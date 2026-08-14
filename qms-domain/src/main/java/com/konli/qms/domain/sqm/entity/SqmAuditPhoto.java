package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审核现场照片(时间 + 位置水印),对应 ops.sqm_audit_photo。
 * 本期前端以"上传后回填路径"方式保存,水印字段保留供后续移动端采集复用。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_audit_photo")
public class SqmAuditPhoto extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("record_id")
    private String recordId;

    @TableField("checklist_item_id")
    private String checklistItemId;

    @TableField("file_path")
    private String filePath;

    @TableField("file_name")
    private String fileName;

    @TableField("file_hash")
    private String fileHash;

    @TableField("watermark_time")
    private LocalDateTime watermarkTime;

    @TableField("watermark_location")
    private String watermarkLocation;

    @TableField("shoot_by")
    private String shootBy;

    @TableField("shoot_time")
    private LocalDateTime shootTime;
}
