package com.konli.qms.api.qmsmgmt.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.qmsmgmt.entity.QmsQualityGoal;
import com.konli.qms.service.qmsmgmt.QmsQualityGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 质量目标管理。qms-mgmt.goal.* */
@RestController
@RequestMapping("/api/v1/qms-mgmt/goals")
@RequiredArgsConstructor
@Slf4j
@Validated
public class QmsQualityGoalController {

    private final QmsQualityGoalService service;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('qms-mgmt.goal.list')")
    public R<PageResult<QmsQualityGoal>> page(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String goalType,
                                              @RequestParam(required = false) String period,
                                                @RequestParam(defaultValue = "1") @Min(1) int page,
                                                @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return R.ok(service.page(keyword, goalType, period, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('qms-mgmt.goal.list')")
    public R<QmsQualityGoal> get(@PathVariable String id) {
        return R.ok(service.get(id));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('qms-mgmt.goal.list')")
    public R<Map<String, Object>> stats() {
        return R.ok(service.stats());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('qms-mgmt.goal.create')")
    public R<QmsQualityGoal> create(@RequestBody QmsQualityGoal goal) {
        return R.ok(service.create(goal));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('qms-mgmt.goal.edit')")
    public R<QmsQualityGoal> update(@RequestBody QmsQualityGoal goal) {
        return R.ok(service.update(goal));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('qms-mgmt.goal.delete')")
    public R<Void> delete(@PathVariable String id) {
        service.delete(id);
        return R.ok();
    }
}
