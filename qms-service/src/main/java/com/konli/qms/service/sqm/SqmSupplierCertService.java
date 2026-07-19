package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmSupplierCert;

import java.util.List;

/** 供应商资质:查询/新增/删除。sqm.supplier.* */
public interface SqmSupplierCertService {

    List<SqmSupplierCert> list(String supplierId);

    SqmSupplierCert get(String id);

    SqmSupplierCert create(SqmSupplierCert cert);

    void delete(String id);

    /** 即将过期的资质(expiryDate 在 now ~ now+days 之间,且状态非'过期')。 */
    List<SqmSupplierCert> expiring(int days);
}
