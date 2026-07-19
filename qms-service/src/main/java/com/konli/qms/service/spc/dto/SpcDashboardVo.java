package com.konli.qms.service.spc.dto;

import lombok.Data;

import java.util.Map;

/** SPC 看板数据(Cpk 等级分布 / 待确认告警 / 今日采集完成率)。 */
@Data
public class SpcDashboardVo {

    /** Cpk 等级分布: {充足, 尚可, 不足} */
    private Map<String, Long> cpkDistribution;

    /** 待确认告警数 */
    private Long pendingAlarms;

    /** 今日子组数(完成) */
    private Long todaySubgroups;

    /** 今日到期任务数(应完成,nextDueAt < 今日) */
    private Long todayDue;
}
