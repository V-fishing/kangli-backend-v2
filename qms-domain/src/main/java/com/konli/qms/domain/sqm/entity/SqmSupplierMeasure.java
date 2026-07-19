package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 供应商改善措施(关联来料异常整改单)。
 * 无审计字段;evidence_files JSONB -> String。
 */
@Data
@TableName("ops.sqm_supplier_measure")
public class SqmSupplierMeasure {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("measure_id")
    private String measureId;

    @TableField("abnormal_id")
    private String abnormalId;

    @TableField("supplier_id")
    private String supplierId;

    private String content;

    private String rootcause;

    private String prevention;

    private LocalDate deadline;

    private String owner;

    @TableField("submit_date")
    private LocalDate submitDate;

    @TableField("evidence_files")
    private String evidenceFiles;        // JSONB -> String

    @TableField("esign_id")
    private String esignId;
}
