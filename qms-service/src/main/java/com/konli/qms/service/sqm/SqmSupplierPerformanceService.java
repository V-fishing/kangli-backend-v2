package com.konli.qms.service.sqm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.sqm.entity.SqmSupplierPerformance;

import java.math.BigDecimal;
import java.util.List;

/** 供应商绩效:查询/新增/自动计算。sqm.supplier.* */
public interface SqmSupplierPerformanceService {

    List<SqmSupplierPerformance> list(String supplierId);

    PageResult<SqmSupplierPerformance> listPage(String supplierId, String period, int page, int size);

    SqmSupplierPerformance get(String id);

    SqmSupplierPerformance create(SqmSupplierPerformance performance);

    /** 按供应商+周期计算绩效(来料合格率 + 交付及时率 -> score -> level)。 */
    SqmSupplierPerformance calc(String supplierId, String period);

    /** SPC CPK→供应商质量分联动:按 (supplier_id, period) upsert 质量分,并加权重算总分与等级。 */
    void applySpcQualityScore(String supplierId, String period, BigDecimal qualityScore);
}
