package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.sqm.entity.SqmSqeVerification;
import com.konli.qms.domain.sqm.mapper.SqmSqeVerificationMapper;
import com.konli.qms.service.sqm.SqmSqeVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmSqeVerificationServiceImpl implements SqmSqeVerificationService {

    private final SqmSqeVerificationMapper sqmSqeVerificationMapper;

    @Override
    public List<SqmSqeVerification> list(String abnormalId) {
        LambdaQueryWrapper<SqmSqeVerification> w = new LambdaQueryWrapper<>();
        if (abnormalId != null && !abnormalId.isBlank()) {
            w.eq(SqmSqeVerification::getAbnormalId, abnormalId);
        }
        w.orderByDesc(SqmSqeVerification::getVerifyDate);
        return sqmSqeVerificationMapper.selectList(w);
    }

    @Override
    public SqmSqeVerification get(String id) {
        return sqmSqeVerificationMapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmSqeVerification create(SqmSqeVerification verification) {
        verification.setVerificationId("SQE-" + System.currentTimeMillis());
        sqmSqeVerificationMapper.insert(verification);
        return verification;
    }
}
