package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 来料异常三批验证记录(V21) */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_abnormal_batch_verify")
public class SqmAbnormalBatchVerify extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("abnormal_id")
    private String abnormalId;

    @TableField("batch_no")
    private String batchNo;

    private String result;

    @TableField("verify_date")
    private LocalDate verifyDate;
}
