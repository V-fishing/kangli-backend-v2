package com.konli.qms.service.ncm;

import java.util.List;
import java.util.Map;

/** NCM 不良分析聚合服务 */
public interface NcmAnalysisService {

    /** 维度聚合: dim = supplier / type / proc / dev / batch / product */
    List<Map<String, Object>> aggregate(String dim);

    /** 交叉分组: dim1 x dim2 交叉表 */
    List<Map<String, Object>> crossTable(String dim1, String dim2);

    /** 时间趋势: 按 day/week/month 统计不良数量 */
    List<Map<String, Object>> trend(String period, String start, String end);
}
