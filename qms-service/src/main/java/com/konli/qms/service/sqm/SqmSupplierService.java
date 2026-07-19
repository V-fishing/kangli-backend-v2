package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmSupplier;

import java.util.List;

/** 供应商档案:查询/新增/编辑/删除。sqm.supplier.* */
public interface SqmSupplierService {

    List<SqmSupplier> list();

    SqmSupplier get(String id);

    SqmSupplier create(SqmSupplier supplier);

    void update(SqmSupplier supplier);

    void delete(String id);
}
