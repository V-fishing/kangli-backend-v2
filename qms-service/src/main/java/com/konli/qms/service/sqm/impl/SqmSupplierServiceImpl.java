package com.konli.qms.service.sqm.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmSupplier;
import com.konli.qms.domain.sqm.mapper.SqmSupplierMapper;
import com.konli.qms.service.sqm.SqmSupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmSupplierServiceImpl implements SqmSupplierService {

    private final SqmSupplierMapper sqmSupplierMapper;

    @Override
    public List<SqmSupplier> list() {
        return sqmSupplierMapper.selectList(null);
    }

    @Override
    public SqmSupplier get(String id) {
        return sqmSupplierMapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmSupplier create(SqmSupplier supplier) {
        if (supplier.getSupplierNo() == null) {
            supplier.setSupplierNo("SUP-" + System.currentTimeMillis());
        }
        if (supplier.getStatus() == null) {
            supplier.setStatus("启用");
        }
        sqmSupplierMapper.insert(supplier);
        return supplier;
    }

    @Override
    @Transactional
    public void update(SqmSupplier supplier) {
        if (supplier.getId() == null || sqmSupplierMapper.selectById(supplier.getId()) == null) {
            throw new BusinessException(404, "供应商不存在");
        }
        sqmSupplierMapper.updateById(supplier);
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (sqmSupplierMapper.selectById(id) == null) {
            throw new BusinessException(404, "供应商不存在");
        }
        sqmSupplierMapper.deleteById(id);
    }
}
