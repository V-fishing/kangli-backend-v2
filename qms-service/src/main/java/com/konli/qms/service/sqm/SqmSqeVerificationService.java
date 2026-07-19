package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmSqeVerification;

import java.util.List;

/** SQE 验证:查询/新增。sqm.abnormal.* */
public interface SqmSqeVerificationService {

    List<SqmSqeVerification> list(String abnormalId);

    SqmSqeVerification get(String id);

    SqmSqeVerification create(SqmSqeVerification verification);
}
