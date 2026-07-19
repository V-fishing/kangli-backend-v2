package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.NcmDailyReportConfig;

import java.util.List;

/** 定时日报配置:查询/upsert(by orgId)/启停。ncm.record.create */
public interface NcmDailyReportConfigService {

    List<NcmDailyReportConfig> list();

    /** 按 orgId upsert:存在则更新,否则插入。 */
    NcmDailyReportConfig save(NcmDailyReportConfig config);

    /** 切换启停状态。 */
    void toggle(String id, Boolean enabled);
}
