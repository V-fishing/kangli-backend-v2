package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmSupplierPerformance;

import java.util.List;

/** 供应商绩效:查询/新增/自动计算。sqm.supplier.* */
public interface SqmSupplierPerformanceService {

    List<SqmSupplierPerformance> list(String supplierId);

    SqmSupplierPerformance get(String id);

    SqmSupplierPerformance create(SqmSupplierPerformance performance);

    /** 按供应商+周期计算绩效(来料合格率 + 交付及时率 -> score -> level)。 */
    SqmSupplierPerformance calc(String supplierId, String period);
}
