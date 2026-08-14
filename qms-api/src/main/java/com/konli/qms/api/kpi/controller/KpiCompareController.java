package com.konli.qms.api.kpi.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.service.kpi.KpiCompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 分公司 KPI 对比接口。仅对超管或拥有 {@code system.org.switch} 权限者开放(与切换器一致)。
 */
@RestController
@RequestMapping("/api/v1/kpi")
@RequiredArgsConstructor
public class KpiCompareController {

    private final KpiCompareService kpiCompareService;

    @GetMapping("/compare")
    @PreAuthorize("hasAuthority('system.org.switch')")
    public R<Map<String, Object>> compare() {
        return R.ok(kpiCompareService.compare());
    }
}
