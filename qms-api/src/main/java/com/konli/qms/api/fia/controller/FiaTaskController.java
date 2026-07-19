package com.konli.qms.api.fia.controller;

import com.konli.qms.api.fia.dto.CreateFiaTaskRequest;
import com.konli.qms.api.fia.dto.InspItemResultRequest;
import com.konli.qms.api.fia.dto.SignRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.fia.entity.FiaArchivedReport;
import com.konli.qms.domain.fia.entity.FiaInspItem;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.service.fia.FiaDashboardService;
import com.konli.qms.service.fia.FiaTaskService;
import com.konli.qms.service.fia.dto.FiaTaskVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 首件检验任务:create -> 录入 -> 检验签名 -> 复核签名(两级完成/三级待批准)-> 批准签名(三级完成)。
 * 签名带密码(SignRequest),经配置的 sign_methods 校验 + 锁定。
 */
@RestController
@RequestMapping("/api/v1/fia/tasks")
@RequiredArgsConstructor
public class FiaTaskController {

    private final FiaTaskService fiaTaskService;
    private final FiaDashboardService fiaDashboardService;

    /** FIA 看板:今日任务/完成数、合格率、超时数、状态分布、近7天趋势。 */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<Map<String, Object>> dashboard() {
        return R.ok(fiaDashboardService.dashboard());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<List<FiaTask>> list() {
        return R.ok(fiaTaskService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<FiaTaskVo> get(@PathVariable String id) {
        return R.ok(fiaTaskService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('fia.task.create')")
    public R<FiaTask> create(@Valid @RequestBody CreateFiaTaskRequest req) {
        FiaTask task = new FiaTask();
        task.setOrgId(req.getOrgId());
        task.setWoNo(req.getWoNo());
        task.setLineName(req.getLineName());
        task.setProductName(req.getProductName());
        task.setProcName(req.getProcName());
        task.setTriggerType(req.getTriggerType());
        task.setStdId(req.getStdId());
        task.setBatchNo(req.getBatchNo());
        task.setIsUrgent(req.getIsUrgent());
        return R.ok(fiaTaskService.create(task));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('fia.task.create')")
    public R<Void> enterResults(@PathVariable String id, @RequestBody InspItemResultRequest req) {
        List<FiaInspItem> items = new ArrayList<>();
        if (req.getItems() != null) {
            for (InspItemResultRequest.Item ir : req.getItems()) {
                FiaInspItem it = new FiaInspItem();
                it.setId(ir.getId());
                it.setMeasuredValue(ir.getMeasuredValue());
                it.setJudge(ir.getJudge());
                items.add(it);
            }
        }
        fiaTaskService.enterResults(id, items);
        return R.ok();
    }

    @PostMapping("/{id}/sign-inspector")
    @PreAuthorize("hasAuthority('fia.task.submit')")
    public R<Void> signInspector(@PathVariable String id, @RequestBody SignRequest req) {
        fiaTaskService.signInspector(id, req.getPassword(), req.getItemId());
        return R.ok();
    }

    @PostMapping("/{id}/sign-reviewer")
    @PreAuthorize("hasAuthority('fia.task.submit')")
    public R<Void> signReviewer(@PathVariable String id, @RequestBody SignRequest req) {
        fiaTaskService.signReviewer(id, req.getPassword(), req.getItemId());
        return R.ok();
    }

    @PostMapping("/{id}/sign-approver")
    @PreAuthorize("hasAuthority('fia.task.submit')")
    public R<Void> signApprover(@PathVariable String id, @RequestBody SignRequest req) {
        fiaTaskService.signApprover(id, req.getPassword());
        return R.ok();
    }

    @GetMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<FiaArchivedReport> getArchive(@PathVariable String id) {
        return R.ok(fiaTaskService.getArchive(id));
    }
}
