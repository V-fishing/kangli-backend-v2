package com.konli.qms.service.sqm;

import java.util.List;
import java.util.Map;

/** SQM 分析报表:来料多维/异常多维/来料看板/供应商绩效排名。sqm.supplier.list */
public interface SqmAnalysisService {

    /** 来料多维分析:按 supplierId/partNo/inspectResult 分组,返回 [{dimValue, totalCount, passCount, failCount, passRate}] */
    List<Map<String, Object>> incomingAnalysis(String dim, String startTime, String endTime);

    /** 来料异常多维分析:按 supplierId/partNo/level 分组,返回 [{dimValue, totalCount, severityCount: {严重:N, 一般:M}}] */
    List<Map<String, Object>> abnormalAnalysis(String dim, String startTime, String endTime);

    /** 来料看板:今日批次/合格率/待处理异常/Top5 不良供应商/7 日趋势 */
    Map<String, Object> dashboard();

    /** 供应商绩效排名:period=YYYY-MM,JOIN sqm_supplier,按 score 降序,返回 [{supplierId, supplierName, score, level, incomingPassRate}] */
    List<Map<String, Object>> ranking(String period);
}
