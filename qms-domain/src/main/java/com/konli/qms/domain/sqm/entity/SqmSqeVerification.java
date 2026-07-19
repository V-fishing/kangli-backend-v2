package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * SQE 验证记录(关联来料异常整改单,连续3批合格闭环)。
 * 无审计字段。
 */
@Data
@TableName("ops.sqm_sqe_verification")
public class SqmSqeVerification {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("verification_id")
    private String verificationId;

    @TableField("abnormal_id")
    private String abnormalId;

    @TableField("sqe_id")
    private String sqeId;

    @TableField("verify_result")
    private String verifyResult;          // 合格/不合格

    @TableField("verify_comment")
    private String verifyComment;

    @TableField("verify_date")
    private LocalDate verifyDate;

    @TableField("qualified_batch_count")
    private Integer qualifiedBatchCount;

    @TableField("esign_id")
    private String esignId;
}
