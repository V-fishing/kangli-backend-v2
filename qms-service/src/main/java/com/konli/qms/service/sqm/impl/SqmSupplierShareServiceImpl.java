package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.sqm.entity.SqmSupplierShare;
import com.konli.qms.domain.sqm.mapper.SqmSupplierShareMapper;
import com.konli.qms.service.sqm.SqmSupplierShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmSupplierShareServiceImpl implements SqmSupplierShareService {

    private final SqmSupplierShareMapper sqmSupplierShareMapper;

    @Override
    public List<SqmSupplierShare> list(String supplierId) {
        LambdaQueryWrapper<SqmSupplierShare> w = new LambdaQueryWrapper<>();
        if (supplierId != null && !supplierId.isBlank()) {
            w.eq(SqmSupplierShare::getSupplierId, supplierId);
        }
        w.orderByDesc(SqmSupplierShare::getEffectiveDate);
        return sqmSupplierShareMapper.selectList(w);
    }

    @Override
    public SqmSupplierShare get(String id) {
        return sqmSupplierShareMapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmSupplierShare create(SqmSupplierShare share) {
        sqmSupplierShareMapper.insert(share);
        return share;
    }
}
