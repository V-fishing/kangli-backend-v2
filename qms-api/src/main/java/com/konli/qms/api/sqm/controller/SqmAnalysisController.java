package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.service.sqm.SqmAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** SQM 分析报表:来料多维/异常多维/来料看板/供应商绩效排名。sqm.supplier.list */
@RestController
@RequestMapping("/api/v1/sqm")
@RequiredArgsConstructor
public class SqmAnalysisController {

    private final SqmAnalysisService sqmAnalysisService;

    /** 来料多维分析:按 dim(supplierId/partNo/inspectResult) 分组,可选时间范围 */
    @GetMapping("/analysis/incoming")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> incomingAnalysis(
            @RequestParam String dim,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return R.ok(sqmAnalysisService.incomingAnalysis(dim, startTime, endTime));
    }

    /** 来料异常多维分析:按 dim(supplierId/partNo/level) 分组,可选时间范围 */
    @GetMapping("/analysis/abnormal")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> abnormalAnalysis(
            @RequestParam String dim,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return R.ok(sqmAnalysisService.abnormalAnalysis(dim, startTime, endTime));
    }

    /** 来料看板:今日批次/合格率/待处理异常/Top5 不良供应商/7 日趋势 */
    @GetMapping("/dashboard/incoming")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<Map<String, Object>> dashboard() {
        return R.ok(sqmAnalysisService.dashboard());
    }

    /** 供应商绩效排名:period=YYYY-MM,按 score 降序 */
    @GetMapping("/performance/ranking")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> ranking(@RequestParam(required = false) String period) {
        return R.ok(sqmAnalysisService.ranking(period));
    }
}
