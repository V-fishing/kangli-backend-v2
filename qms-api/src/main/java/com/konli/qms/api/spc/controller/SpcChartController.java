package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.service.spc.SpcDashboardService;
import com.konli.qms.service.spc.SpcSubgroupService;
import com.konli.qms.service.spc.dto.ControlChartVo;
import com.konli.qms.service.spc.dto.SpcDashboardVo;
import com.konli.qms.service.spc.dto.SpcHistogramVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** SPC 分析报表(控制图数据 + 看板)。 */
@RestController
@RequestMapping("/api/v1/spc")
@RequiredArgsConstructor
public class SpcChartController {

    private final SpcSubgroupService spcSubgroupService;
    private final SpcDashboardService spcDashboardService;

    /** 控制图数据:子组时间序列 + 当前激活控制限。stage 可选: FIRST/Routine/ALL;sampleTaskId 可选按抽样任务过滤。 */
    @GetMapping("/control-chart")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<ControlChartVo> controlChart(@RequestParam String paramId,
                                          @RequestParam(required = false) String startTime,
                                          @RequestParam(required = false) String endTime,
                                          @RequestParam(required = false) String stage,
                                          @RequestParam(required = false) String sampleTaskId) {
        return R.ok(spcSubgroupService.getControlChart(paramId, startTime, endTime, stage, sampleTaskId));
    }

    /** 过程能力直方图:基于参数子组均值分箱。stage 可选: FIRST/ROUTINE/ALL;sampleTaskId 可选按抽样任务过滤。 */
    @GetMapping("/histogram")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<SpcHistogramVo> histogram(@RequestParam(required = false) String paramId,
                                       @RequestParam(required = false) String stage,
                                       @RequestParam(required = false) String sampleTaskId) {
        return R.ok(spcSubgroupService.getHistogram(paramId, stage, sampleTaskId));
    }

    /** SPC 看板:Cpk 分布 / 待确认告警 / 今日采集完成率。 */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<SpcDashboardVo> dashboard() {
        return R.ok(spcDashboardService.dashboard());
    }
}
