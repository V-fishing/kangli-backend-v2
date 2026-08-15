package com.konli.qms.api.tlm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.tlm.entity.TlmCalibPlan;
import com.konli.qms.service.tlm.TlmCalibPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/** 计量校准计划单接口(P1)。 */
@RestController
@RequestMapping("/api/v1/tlm")
@RequiredArgsConstructor
@Slf4j
public class TlmCalibPlanController {

    private final TlmCalibPlanService calibPlanService;

    @GetMapping("/calib-plans/page")
    @PreAuthorize("hasAuthority('tlm.metro.list')")
    public R<PageResult<TlmCalibPlan>> page(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return R.ok(calibPlanService.page(keyword, status, page, size));
    }

    @PostMapping("/calib-plans/{planId}/record")
    @PreAuthorize("hasAuthority('tlm.metro.calib')")
    public R<Void> record(@PathVariable String planId, @RequestBody Map<String, Object> body) {
        LocalDate calibDate = body.get("calibDate") != null ? LocalDate.parse(body.get("calibDate").toString()) : null;
        LocalDate calibDueDate = body.get("calibDueDate") != null ? LocalDate.parse(body.get("calibDueDate").toString()) : null;
        Integer calibCycle = body.get("calibCycle") != null ? Integer.valueOf(body.get("calibCycle").toString()) : null;
        String upperLimit = body.get("upperLimit") != null ? body.get("upperLimit").toString() : null;
        String result = body.get("result") != null ? body.get("result").toString() : "合格";
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        calibPlanService.recordResult(planId, calibDate, calibDueDate, calibCycle, upperLimit, result, remark);
        return R.ok();
    }

    @PostMapping("/calib-plans/manual")
    @PreAuthorize("hasAuthority('tlm.metro.calib')")
    public R<TlmCalibPlan> createManual(@RequestParam String toolId,
                                        @RequestParam(required = false) Integer planCycle,
                                        @RequestParam String planDueDate) {
        return R.ok(calibPlanService.createManual(toolId, planCycle, LocalDate.parse(planDueDate)));
    }
}
