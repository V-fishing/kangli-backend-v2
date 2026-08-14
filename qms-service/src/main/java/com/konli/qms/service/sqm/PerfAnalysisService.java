package com.konli.qms.service.sqm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.sqm.entity.SqmSupplierPerformance;

import java.util.List;
import java.util.Map;

/** 供应商绩效分析:排名 / 趋势 / 柏拉图聚合(只读)。 */
public interface PerfAnalysisService {

    /** 按周期对供应商综合分排名(分页,含名次)。 */
    PageResult<Map<String, Object>> rank(String period, String category, int page, int size);

    /** 多供应商近 N 周期综合分与单项指标趋势。 */
    List<SqmSupplierPerformance> trend(List<String> supplierIds, String periodStart, String periodEnd);

    /** 按缺陷类型聚合 TOP N 累计占比(柏拉图)。 */
    List<Map<String, Object>> pareto(String periodStart, String periodEnd, int topN);
}
