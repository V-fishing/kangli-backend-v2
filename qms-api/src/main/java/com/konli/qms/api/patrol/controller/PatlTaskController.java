package com.konli.qms.api.patrol.controller;

import com.konli.qms.api.patrol.dto.CreateTaskRequest;
import com.konli.qms.api.patrol.dto.SubmitRecordRequest;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.patrol.entity.PatlTask;
import com.konli.qms.service.patrol.PatlTaskService;
import com.konli.qms.service.patrol.dto.PatlTaskVo;
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

/** 巡检任务:按路线生成/提交点位结果/关闭。patl.task.list / patl.task.create */
@RestController
@RequestMapping("/api/v1/patrol/tasks")
@RequiredArgsConstructor
public class PatlTaskController {

    private final PatlTaskService patlTaskService;

    @GetMapping
    @PreAuthorize("hasAuthority('patl.task.list')")
    public R<List<PatlTask>> list() {
        return R.ok(patlTaskService.list());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('patl.task.list')")
    public R<PageResult<PatlTask>> page(@RequestParam(required = false) String keyword,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return R.ok(patlTaskService.listPage(keyword, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('patl.task.list')")
    public R<PatlTaskVo> get(@PathVariable String id) {
        return R.ok(patlTaskService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('patl.task.create')")
    public R<PatlTask> create(@RequestBody CreateTaskRequest req) {
        return R.ok(patlTaskService.create(req.getOrgId(), req.getRouteId(), req.getShift(), req.getPlanTime()));
    }

    @PostMapping("/{id}/records")
    @PreAuthorize("hasAuthority('patl.task.record')")
    public R<Void> submitRecord(@PathVariable String id, @RequestBody SubmitRecordRequest req) {
        patlTaskService.submitRecord(id, req.getCheckpointId(), req.getCheckpointName(),
                req.getResult(), req.getRemark(), currentOperator());
        return R.ok();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('patl.task.close')")
    public R<Void> close(@PathVariable String id) {
        patlTaskService.close(id);
        return R.ok();
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null || u.userId() == null ? "系统" : u.userId();
    }
}
