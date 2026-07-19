package com.konli.qms.api.sqm.controller;

import com.konli.qms.api.sqm.dto.ApproveChangeRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import com.konli.qms.service.sqm.SqmChangeService;
import com.konli.qms.service.sqm.dto.SqmChangeOrderVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 物料变更单:创建/查询/提交/会签审批/关闭。sqm.change.list / sqm.change.create */
@RestController
@RequestMapping("/api/v1/sqm/changes")
@RequiredArgsConstructor
public class SqmChangeController {

    private final SqmChangeService sqmChangeService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.change.list')")
    public R<List<SqmChangeOrder>> list() {
        return R.ok(sqmChangeService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.change.list')")
    public R<SqmChangeOrderVo> get(@PathVariable String id) {
        return R.ok(sqmChangeService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.change.create')")
    public R<SqmChangeOrder> create(@RequestBody SqmChangeOrder order) {
        return R.ok(sqmChangeService.create(order));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('sqm.change.create')")
    public R<Void> submit(@PathVariable String id) {
        sqmChangeService.submit(id);
        return R.ok();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('sqm.change.create')")
    public R<Void> approve(@PathVariable String id, @RequestBody ApproveChangeRequest req) {
        sqmChangeService.approve(id, req.getApprovalRole(), req.isApproved(), req.getOpinion());
        return R.ok();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('sqm.change.create')")
    public R<Void> close(@PathVariable String id) {
        sqmChangeService.close(id);
        return R.ok();
    }
}
