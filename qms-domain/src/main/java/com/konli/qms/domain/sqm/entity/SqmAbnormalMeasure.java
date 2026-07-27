package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 来料异常整改措施记录(V21) */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_abnormal_measure")
public class SqmAbnormalMeasure extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("abnormal_id")
    private String abnormalId;

    private Integer seq;

    private String content;

    private String operator;

    @TableField("complete_date")
    private LocalDate completeDate;

    private String status;
}
