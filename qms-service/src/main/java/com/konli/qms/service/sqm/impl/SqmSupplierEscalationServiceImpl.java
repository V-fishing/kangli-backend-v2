package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.sqm.entity.SqmSupplierEscalation;
import com.konli.qms.domain.sqm.mapper.SqmSupplierEscalationMapper;
import com.konli.qms.service.sqm.SqmSupplierEscalationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmSupplierEscalationServiceImpl implements SqmSupplierEscalationService {

    private final SqmSupplierEscalationMapper sqmSupplierEscalationMapper;

    @Override
    public List<SqmSupplierEscalation> list(String supplierId) {
        LambdaQueryWrapper<SqmSupplierEscalation> w = new LambdaQueryWrapper<>();
        if (supplierId != null && !supplierId.isBlank()) {
            w.eq(SqmSupplierEscalation::getSupplierId, supplierId);
        }
        w.orderByDesc(SqmSupplierEscalation::getCreatedAt);
        return sqmSupplierEscalationMapper.selectList(w);
    }

    @Override
    public SqmSupplierEscalation get(String id) {
        return sqmSupplierEscalationMapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmSupplierEscalation create(SqmSupplierEscalation escalation) {
        if (escalation.getNoticeSentFlag() == null) {
            escalation.setNoticeSentFlag(false);
        }
        sqmSupplierEscalationMapper.insert(escalation);
        return escalation;
    }
}
