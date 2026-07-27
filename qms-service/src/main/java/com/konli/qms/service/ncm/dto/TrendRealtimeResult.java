package com.konli.qms.service.ncm.dto;

import com.konli.qms.domain.ncm.entity.NcmDefectTrendRule;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** 实时趋势报表结果。 */
@Data
public class TrendRealtimeResult {
    private String generatedAt;
    private NcmDefectTrendRule rule;
    private List<TrendPoint> points;
    private Map<String, Object> summary;   // 整体概览:最差产品、恶化点数等
}
