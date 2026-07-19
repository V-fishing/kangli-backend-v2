package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 不良记录(单条缺陷发生记录,关联工单/批次/字典)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.ncm_defect_record")
public class NcmDefectRecord extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("defect_no")
    private String defectNo;

    @TableField("wo_no")
    private String woNo;

    @TableField("process_code")
    private String processCode;

    @TableField("defect_dict_code")
    private String defectDictCode;

    private String severity;

    @TableField("defect_count")
    private Integer defectCount;

    @TableField("batch_total")
    private Integer batchTotal;

    @TableField("defect_rate")
    private BigDecimal defectRate;

    @TableField("device_code")
    private String deviceCode;

    @TableField("batch_no")
    private String batchNo;

    @TableField("product_model")
    private String productModel;

    @TableField("operator_id")
    private String operatorId;

    private String source;

    @TableField("device_payload")
    private String devicePayload;

    @TableField("occurred_at")
    private LocalDateTime occurredAt;

    private String remark;

    private String disposition;
}
