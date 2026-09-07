package com.konli.qms.service.sqm;

import com.konli.qms.common.api.PageResult;

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

    /** 供应商合格率分布:五档分桶,返回 [{bucket,label,count,suppliers:[{supplierId,supplierName,level,passRate,abnormalCount}]}] */
    List<Map<String, Object>> passRateDist(String level, String keyword, String startYm, String endYm);

    /** 重点供应商合格率趋势:按月多线,observeOnly=true 仅重点观察,supplierIds 追加对比;返回 [{period,supplierId,supplierName,passRate}] */
    List<Map<String, Object>> passRateTrend(boolean observeOnly, List<String> supplierIds, String startYm, String endYm);

    /** 质量异常热力图:供应商×月份交叉计数;year 指定年份,topN 取异常最多供应商;返回 [{supplierId,supplierName,months:{YYYY-MM:count}}] */
    List<Map<String, Object>> abnormalHeat(String year, int topN);

    /** 供应商等级占比:全量 A/B/C/D 分布;返回 [{level,count}] */
    List<Map<String, Object>> levelRatio(String level, String keyword);

    /** 检验结论分布:筛选供应商来料批次的 inspect_result 聚合;返回 [{result,count}] */
    List<Map<String, Object>> inspectResult(String level, String keyword);

    /** 交付率×合格率散点:返回 [{supplierId,supplierName,level,deliveryRate,incomingPassRate,lotCount}] */
    List<Map<String, Object>> deliveryVsPass(String level, String keyword);

    // ==================== 物料看板:三聚合 ====================

    /** 物料合格率分布:五档分桶,keyOnly=true 仅关键物料;返回 [{bucket,label,count,materials:[{partNo,partName,passRate,totalCount,failCount}]}] */
    List<Map<String, Object>> materialPassRateDist(boolean keyOnly, String startYm, String endYm);

    /** 重点物料合格率趋势:按月多线,keyOnly=true 仅关键物料,partNos 追加对比;返回 [{period,partNo,partName,passRate}] */
    List<Map<String, Object>> materialPassRateTrend(boolean keyOnly, List<String> partNos, String startYm, String endYm);

    /** 物料劣化预警:对比最新月与上一月合格率,跌破95%警戒线或环比降幅>=2pp 预警、>=5pp 严重;分页返回 PageResult<{partNo,partName,prevRate,curRate,dropPp,level,reason}>,按严重优先+降幅降序 */
    PageResult<Map<String, Object>> materialDeterioration(boolean keyOnly, String startYm, String endYm, int page, int size);

    /** 物料搜索:按 partNo/partName 模糊匹配去重,返回 [{partNo, partName}],供看板追加对比下拉远程搜索 */
    List<Map<String, Object>> materialSearch(String keyword, int limit);
}
