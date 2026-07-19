package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcCollectTask;
import com.konli.qms.service.spc.SpcCollectTaskService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SPC 采集任务(计划采集频率/计划停机)。 */
@RestController
@RequestMapping("/api/v1/spc/collect-tasks")
@RequiredArgsConstructor
public class SpcCollectTaskController {

    private final SpcCollectTaskService spcCollectTaskService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.subgroup.create')")
    public R<List<SpcCollectTask>> list() {
        return R.ok(spcCollectTaskService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('spc.subgroup.create')")
    public R<SpcCollectTask> create(@RequestBody SpcCollectTask task) {
        return R.ok(spcCollectTaskService.create(task));
    }

    @PostMapping("/{id}/downtime")
    @PreAuthorize("hasAuthority('spc.subgroup.create')")
    public R<Void> markDowntime(@PathVariable String id, @RequestBody DowntimeRequest req) {
        spcCollectTaskService.markDowntime(id, req.getIsPlannedDowntime(), req.getReason());
        return R.ok();
    }

    /** 采集任务计划停机请求(是否计划停机 + 原因)。内嵌 DTO 以保持每项 5 文件。 */
    @Data
    public static class DowntimeRequest {
        private Boolean isPlannedDowntime;
        private String reason;
    }
}
