package com.konli.qms.api.spc.controller;

import com.konli.qms.api.spc.dto.CreateSampleTaskRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.spc.entity.SpcSampleTask;
import com.konli.qms.service.spc.SpcSampleTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SPC 抽样批次任务(量产监控采集载体)。 */
@RestController
@RequestMapping("/api/v1/spc/sample-tasks")
@RequiredArgsConstructor
public class SpcSampleTaskController {

    private final SpcSampleTaskService spcSampleTaskService;

    @PostMapping
    @PreAuthorize("hasAuthority('spc.sample-task.create')")
    public R<List<SpcSampleTask>> create(@Valid @RequestBody CreateSampleTaskRequest req) {
        String orgId = req.getOrgId();
        if ((orgId == null || orgId.isBlank()) && CompanyContext.get() != null) {
            orgId = CompanyContext.get().orgId();
        }
        if ("ROOT".equals(orgId)) {
            orgId = null;
        }
        String operator = CompanyContext.get() == null ? "系统" : CompanyContext.get().userId();
        List<SpcSampleTask> tasks = spcSampleTaskService.createBatch(orgId, req.getWoNo(), req.getPartNo(),
                req.getProcName(), req.getProductName(), req.getTargetCount(), req.getParamIds(),
                req.getFiaStdItemIds(),
                req.getTriggerType(), req.getCategory(), req.getSupplierId(), req.getSupplierName(),
                req.getIsUrgent(), req.getRemark(), operator);
        return R.ok(tasks);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('spc.sample-task.list')")
    public R<List<SpcSampleTask>> list(@RequestParam(required = false) String woNo,
                                       @RequestParam(required = false) String partNo,
                                       @RequestParam(required = false) String status) {
        String orgId = CompanyContext.get() == null ? null : CompanyContext.get().orgId();
        if ("ROOT".equals(orgId)) orgId = null;
        return R.ok(spcSampleTaskService.list(orgId, woNo, partNo, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.sample-task.list')")
    public R<SpcSampleTask> get(@PathVariable String id) {
        return R.ok(spcSampleTaskService.get(id));
    }
}
