package com.konli.qms.service.ncm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;

import java.util.List;
import java.util.Map;

/** 不良记录查询与录入。ncm.record.* */
public interface NcmDefectRecordService {

    List<NcmDefectRecord> list();

    /** 不良记录分页查询,支持关键字与条件过滤。
     *  keyword 模糊匹配记录编号/工单号/工序/缺陷编码;defectDictCode/woNo/severity 精确或模糊过滤。
     *  page/size 从 1 开始计数,默认 page=1,size=20。供不良记录列表大数据量分页加载。 */
    PageResult<NcmDefectRecord> listPage(String keyword, String defectDictCode, String woNo, String severity, String stage, String source, int page, int size);

    NcmDefectRecord get(String id);

    /** 按不良编号精确查询。 */
    NcmDefectRecord getByDefectNo(String defectNo);

    NcmDefectRecord create(NcmDefectRecord record);

    /** 多维分析:按 processCode/defectDictCode/deviceCode/batchNo 分组,返回 [{dimValue, totalCount, severityCount: {严重:N, 一般:M}}] */
    List<Map<String, Object>> multiDimAnalysis(String dim, String startTime, String endTime);

    /** 趋势报表:按 day/week/month 聚合,返回 [{period, count, defectRate}] */
    List<Map<String, Object>> trendAnalysis(String granularity, String startTime, String endTime);

    /** 环比同比:period=YYYY-MM,type∈{week,month,year,mtd};返回不良率% {current, previous, yoy, curLabel, prevLabel, yoyLabel, unit, mom, changeRate} */
    Map<String, Object> compareAnalysis(String period, String type);

    /** 实时看板:今日不良数/当前班次不良率/PPM/Top5 不良类型/工序热力图/数据新鲜度 */
    Map<String, Object> dashboard();

    /** 趋势异常检测(SR-NCM-017):连续5天defectRate上升->写ncm_trend_alert+通知。返回{anomaly,consecutiveIncr,trendPoints} */
    Map<String, Object> checkTrendAnomaly();

    /** 不良记录一键发起8D(SR-NCM处置决策):创建8D单,source=不良记录,并按 req 指派处理人+通知。返回8D记录 */
    Object launch8dFromDefect(String defectId, DefectLaunchRequest req);

    /** 不良记录一键发起CAPA(SR-NCM处置决策):创建CAPA单,并按 req 指派处理人+通知。返回CAPA记录 */
    Object launchCapaFromDefect(String defectId, DefectLaunchRequest req);

    /** 不良记录一键发起CA(SR-NCM处置决策):创建纠正措施单,并按 req 指派处理人+通知。返回CA记录 */
    Object launchCaFromDefect(String defectId, DefectLaunchRequest req);

    /** 指派候选:启用用户列表 + 启用角色列表 + 启用的通知渠道列表。 */
    Map<String, Object> assignCandidates();
}
