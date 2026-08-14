package com.konli.qms.service.sqm.impl;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.sqm.entity.SqmSupplierPerformance;
import com.konli.qms.domain.sqm.mapper.PerfAnalysisMapper;
import com.konli.qms.service.sqm.PerfAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PerfAnalysisServiceImpl implements PerfAnalysisService {

    private final PerfAnalysisMapper analysisMapper;

    @Override
    public PageResult<Map<String, Object>> rank(String period, String category, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        long total = analysisMapper.rankCount(period, category);
        int offset = (page - 1) * size;
        List<Map<String, Object>> records = analysisMapper.rank(period, category, size, offset);
        return new PageResult<>(records, total, page, size);
    }

    @Override
    public List<SqmSupplierPerformance> trend(List<String> supplierIds, String periodStart, String periodEnd) {
        if (supplierIds == null || supplierIds.isEmpty()) {
            return List.of();
        }
        return analysisMapper.trend(supplierIds, periodStart, periodEnd);
    }

    @Override
    public List<Map<String, Object>> pareto(String periodStart, String periodEnd, int topN) {
        return analysisMapper.pareto(periodStart, periodEnd, topN <= 0 ? 10 : topN);
    }
}
