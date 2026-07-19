package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmSupplierEscalation;

import java.util.List;

/** 供应商升级管理:查询/新增。sqm.supplier.* */
public interface SqmSupplierEscalationService {

    List<SqmSupplierEscalation> list(String supplierId);

    SqmSupplierEscalation get(String id);

    SqmSupplierEscalation create(SqmSupplierEscalation escalation);
}
