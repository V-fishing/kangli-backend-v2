package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.NcmDefectTrendReport;
import com.konli.qms.domain.ncm.entity.NcmDefectTrendRule;
import com.konli.qms.service.ncm.dto.TrendRealtimeResult;

import java.time.LocalDateTime;
import java.util.List;

public interface NcmDefectTrendService {

    /** 获取生效的恶化判定规则(优先组织级,否则全局)。 */
    NcmDefectTrendRule getRule();

    /** 保存恶化判定规则(全局/组织级)。 */
    NcmDefectTrendRule saveRule(NcmDefectTrendRule rule);

    /** 实时聚合某产品/全产品在指定时间范围、粒度下的趋势序列。 */
    TrendRealtimeResult realtime(String granularity, String productModel, String start, String end);

    /** 针对单个周期(日/周/月)生成报表快照并落库。 */
    void generateForPeriod(String granularity, LocalDateTime start, LocalDateTime end, String periodValue);

    /** 定时任务:按日/周/月生成报表快照。 */
    void scheduledGenerate();

    /** 手动触发:生成"当前"周期(今日/本周/本月)的报表快照。 */
    void generateCurrent(String granularity);

    /** 分页查询已生成报表。 */
    List<NcmDefectTrendReport> listReports(String productModel, String granularity, int page, int size);
}
