package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.service.spc.SpcDashboardService;
import com.konli.qms.service.spc.SpcSubgroupService;
import com.konli.qms.service.spc.dto.ControlChartVo;
import com.konli.qms.service.spc.dto.SpcDashboardVo;
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

    /** 控制图数据:子组时间序列 + 当前激活控制限。 */
    @GetMapping("/control-chart")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<ControlChartVo> controlChart(@RequestParam String paramId,
                                          @RequestParam(required = false) String startTime,
                                          @RequestParam(required = false) String endTime) {
        return R.ok(spcSubgroupService.getControlChart(paramId, startTime, endTime));
    }

    /** SPC 看板:Cpk 分布 / 待确认告警 / 今日采集完成率。 */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<SpcDashboardVo> dashboard() {
        return R.ok(spcDashboardService.dashboard());
    }
}
