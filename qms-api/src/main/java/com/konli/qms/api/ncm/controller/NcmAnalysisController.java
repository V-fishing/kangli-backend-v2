package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.service.ncm.NcmAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** NCM 不良分析报表：维度聚合 + 交叉表 + 时间趋势（全员可访问） */
@RestController
@RequestMapping("/api/v1/ncm/aggregate")
@RequiredArgsConstructor
public class NcmAnalysisController {

    private final NcmAnalysisService ncmAnalysisService;

    /** 维度聚合: dim = supplier / type / proc / dev / batch / product / severity */
    @GetMapping("/analysis/aggregate")
    public R<List<Map<String, Object>>> aggregate(@RequestParam String dim) {
        return R.ok(ncmAnalysisService.aggregate(dim));
    }

    /** 交叉分组: dim1 x dim2 交叉表 */
    @GetMapping("/analysis/cross")
    public R<List<Map<String, Object>>> crossTable(
            @RequestParam String dim1, @RequestParam String dim2) {
        return R.ok(ncmAnalysisService.crossTable(dim1, dim2));
    }

    /** 时间趋势: period = day / week / month */
    @GetMapping("/analysis/trend")
    public R<List<Map<String, Object>>> trend(
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        return R.ok(ncmAnalysisService.trend(period, start, end));
    }
}
