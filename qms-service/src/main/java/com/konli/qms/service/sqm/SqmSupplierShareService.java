package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmSupplierShare;

import java.util.List;

/** 供应商份额:查询/新增。sqm.supplier.* */
public interface SqmSupplierShareService {

    List<SqmSupplierShare> list(String supplierId);

    SqmSupplierShare get(String id);

    SqmSupplierShare create(SqmSupplierShare share);
}
