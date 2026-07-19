package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmSupplierCert;
import com.konli.qms.domain.sqm.mapper.SqmSupplierCertMapper;
import com.konli.qms.service.sqm.SqmSupplierCertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmSupplierCertServiceImpl implements SqmSupplierCertService {

    private final SqmSupplierCertMapper sqmSupplierCertMapper;

    @Override
    public List<SqmSupplierCert> list(String supplierId) {
        LambdaQueryWrapper<SqmSupplierCert> w = new LambdaQueryWrapper<>();
        if (supplierId != null && !supplierId.isBlank()) {
            w.eq(SqmSupplierCert::getSupplierId, supplierId);
        }
        w.orderByDesc(SqmSupplierCert::getExpiryDate);
        return sqmSupplierCertMapper.selectList(w);
    }

    @Override
    public SqmSupplierCert get(String id) {
        return sqmSupplierCertMapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmSupplierCert create(SqmSupplierCert cert) {
        if (cert.getStatus() == null) {
            cert.setStatus("生效");
        }
        if (cert.getCertVersion() == null) {
            cert.setCertVersion(1);
        }
        sqmSupplierCertMapper.insert(cert);
        return cert;
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (sqmSupplierCertMapper.selectById(id) == null) {
            throw new BusinessException(404, "供应商资质不存在");
        }
        sqmSupplierCertMapper.deleteById(id);
    }

    @Override
    public List<SqmSupplierCert> expiring(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        return sqmSupplierCertMapper.selectList(
                new LambdaQueryWrapper<SqmSupplierCert>()
                        .le(SqmSupplierCert::getExpiryDate, deadline)
                        .gt(SqmSupplierCert::getExpiryDate, LocalDate.now())
                        .ne(SqmSupplierCert::getStatus, "过期")
                        .orderByAsc(SqmSupplierCert::getExpiryDate));
    }
}
