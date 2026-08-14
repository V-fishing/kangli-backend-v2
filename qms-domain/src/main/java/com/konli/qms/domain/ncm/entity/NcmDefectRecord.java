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

    /** 不良阶段(与缺陷现象正交): 来料不良/半成品不良/成品不良/首件不良 */
    @TableField("stage")
    private String stage;

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

    /** 关联 8D 报告单号(d8_no),发起8D后回写;为null表示未发起。 */
    @TableField("d8_no")
    private String d8No;

    /** 关联 CAPA 单号(capa_no),发起CAPA后回写;为null表示未发起。 */
    @TableField("capa_no")
    private String capaNo;

    /** 关联 CA 纠正措施单号(ca_no),发起CA后回写;为null表示未发起。 */
    @TableField("ca_no")
    private String caNo;

    // ---- 以下为展示用临时字段(不存库),列表查询时由 Service 回填报告状态 ----

    /** 8D 报告 id(UUID),用于前端跳转详情页。 */
    @TableField(exist = false)
    private String d8Id;

    /** 8D 报告状态(进行中/已闭环),由 listPage 实时 JOIN 回填。 */
    @TableField(exist = false)
    private String d8Status;

    /** CAPA id(UUID),用于前端跳转详情页。 */
    @TableField(exist = false)
    private String capaId;

    /** CAPA 报告状态(待启动/分析中/待审批/实施中/已验证/已关闭),由 listPage 实时 JOIN 回填。 */
    @TableField(exist = false)
    private String capaStatus;

    /** CA id(UUID),用于前端跳转详情页。 */
    @TableField(exist = false)
    private String caId;

    /** CA 纠正措施状态(待启动/进行中/已完成/已关闭),由 listPage 实时 JOIN 回填。 */
    @TableField(exist = false)
    private String caStatus;
}
