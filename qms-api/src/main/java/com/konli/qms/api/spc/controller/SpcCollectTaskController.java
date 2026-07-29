package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcCollectTask;
import com.konli.qms.service.spc.SpcCollectTaskService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.subgroup.create')")
    public R<Void> update(@PathVariable String id, @RequestBody SpcCollectTask task) {
        task.setId(id);
        spcCollectTaskService.update(task);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.subgroup.create')")
    public R<Void> delete(@PathVariable String id) {
        spcCollectTaskService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/downtime")
    @PreAuthorize("hasAuthority('spc.subgroup.create')")
    public R<Void> markDowntime(@PathVariable String id, @RequestBody DowntimeRequest req) {
        spcCollectTaskService.markDowntime(id, req.getIsPlannedDowntime(), req.getReason());
        return R.ok();
    }

    /** SR-SPC-003:手动标记采集缺失 -> status='缺失' + 告警班组长(停产不告警)。 */
    @PostMapping("/{id}/mark-missing")
    @PreAuthorize("hasAuthority('spc.subgroup.create')")
    public R<Void> markMissing(@PathVariable String id, @RequestBody(required = false) DowntimeRequest req) {
        spcCollectTaskService.markMissing(id, req == null ? null : req.getReason());
        return R.ok();
    }

    /** SR-SPC-003:手动触发到期未录入扫描(定时任务每 60s 自动执行,此接口便于即时验证)。 */
    @PostMapping("/scan-missing")
    @PreAuthorize("hasAuthority('spc.subgroup.create')")
    public R<Integer> scanMissing() {
        return R.ok(spcCollectTaskService.scanOverdueMissing());
    }

    /** 采集任务计划停机请求(是否计划停机 + 原因)。内嵌 DTO 以保持每项 5 文件。 */
    @Data
    public static class DowntimeRequest {
        private Boolean isPlannedDowntime;
        private String reason;
    }
}
