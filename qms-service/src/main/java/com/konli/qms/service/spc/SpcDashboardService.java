package com.konli.qms.service.spc;

import com.konli.qms.service.spc.dto.SpcDashboardVo;

/** SPC 看板(Cpk 等级分布 / 待确认告警 / 今日采集完成率)。spc.param.list */
public interface SpcDashboardService {

    SpcDashboardVo dashboard();
}
