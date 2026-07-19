package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.NcmCorrectiveAction;
import com.konli.qms.service.ncm.NcmCorrectiveActionService;
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

/** 纠正措施:创建/查询/进度更新/关闭。ncm.record.create */
@RestController
@RequestMapping("/api/v1/ncm/corrective-actions")
@RequiredArgsConstructor
public class NcmCorrectiveActionController {

    private final NcmCorrectiveActionService ncmCorrectiveActionService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<List<NcmCorrectiveAction>> list() {
        return R.ok(ncmCorrectiveActionService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<NcmCorrectiveAction> get(@PathVariable String id) {
        return R.ok(ncmCorrectiveActionService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<NcmCorrectiveAction> create(@RequestBody NcmCorrectiveAction action) {
        return R.ok(ncmCorrectiveActionService.create(action));
    }

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<Void> updateProgress(@PathVariable String id, @RequestParam Short progress) {
        ncmCorrectiveActionService.updateProgress(id, progress);
        return R.ok();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<Void> close(@PathVariable String id) {
        ncmCorrectiveActionService.close(id);
        return R.ok();
    }
}
