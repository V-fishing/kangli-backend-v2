package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.NcmDefectRecord;

import java.util.List;
import java.util.Map;

/** 不良记录查询与录入。ncm.record.* */
public interface NcmDefectRecordService {

    List<NcmDefectRecord> list();

    NcmDefectRecord get(String id);

    NcmDefectRecord create(NcmDefectRecord record);

    /** 多维分析:按 processCode/defectDictCode/deviceCode/batchNo 分组,返回 [{dimValue, totalCount, severityCount: {严重:N, 一般:M}}] */
    List<Map<String, Object>> multiDimAnalysis(String dim, String startTime, String endTime);

    /** 趋势报表:按 day/week/month 聚合,返回 [{period, count, defectRate}] */
    List<Map<String, Object>> trendAnalysis(String granularity, String startTime, String endTime);

    /** 环比同比:period=YYYY-MM,返回 {current, previous, yoy, mom, changeRate} */
    Map<String, Object> compareAnalysis(String period);

    /** 实时看板:今日不良数/当前班次不良率/Top5 不良类型/工序热力图/数据新鲜度 */
    Map<String, Object> dashboard();
}
