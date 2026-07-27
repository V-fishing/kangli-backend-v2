package com.konli.qms.api.ncm.controller;

import com.konli.qms.api.ncm.dto.AdvanceStageRequest;
import com.konli.qms.api.ncm.dto.StageApproveDTO;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.Qms8dApprovalConfig;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.service.ncm.Ncm8dApprovalConfigService;
import com.konli.qms.service.ncm.Ncm8dService;
import com.konli.qms.service.ncm.dto.EightDVo;
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

/**
 * 8D 报告: 创建/查询/阶段推进/审批/重开。
 *
 * 权限码:
 *   ncm.8d.list    查看 8D 列表和详情 (检验员+)
 *   ncm.8d.create  创建 8D + 填写阶段内容 (检验员+)
 *   ncm.8d.advance 推进到下一阶段 (班组长+)
 *   ncm.8d.approve 审批/驳回阶段 D3/D5/D7 (质量经理+)
 *   ncm.8d.reopen  重新打开已闭环 8D (质量工程师+)
 */
@RestController
@RequestMapping("/api/v1/ncm/8d-reports")
@RequiredArgsConstructor
public class Ncm8dController {

    private final Ncm8dService ncm8dService;
    private final Ncm8dApprovalConfigService ncm8dApprovalConfigService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.8d.list')")
    public R<List<Qms8dReport>> list() {
        return R.ok(ncm8dService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.8d.list')")
    public R<EightDVo> get(@PathVariable String id) {
        return R.ok(ncm8dService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.8d.create')")
    public R<Qms8dReport> create(@RequestBody Qms8dReport report) {
        return R.ok(ncm8dService.create(report));
    }

    @PostMapping("/launch")
    @PreAuthorize("hasAuthority('ncm.8d.create')")
    public R<Qms8dReport> launchFromAbnormal(@RequestBody Qms8dReport report) {
        return R.ok(ncm8dService.launchFromAbnormal(report));
    }

    @PostMapping("/{id}/advance")
    @PreAuthorize("hasAuthority('ncm.8d.advance')")
    public R<Void> advance(@PathVariable String id, @RequestBody AdvanceStageRequest req) {
        ncm8dService.advanceStage(id, req.getStageCode(), req.getContent(), req.getOwner());
        return R.ok();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ncm.8d.approve')")
    public R<Void> approve(@PathVariable String id, @RequestBody StageApproveDTO dto) {
        ncm8dService.approveStage(id, dto.getStageCode(), dto.isApproved(), dto.getComment(), dto.getPassword());
        return R.ok();
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAuthority('ncm.8d.reopen')")
    public R<Void> reopen(@PathVariable String id, @RequestParam(required = false) String reason) {
        ncm8dService.reopen(id, reason);
        return R.ok();
    }

    /** 8D 阶段审核配置:读取当前公司的“哪些阶段需审核人签名及指定签批人”。 */
    @GetMapping("/approval-config")
    @PreAuthorize("hasAuthority('ncm.8d.list')")
    public R<List<Qms8dApprovalConfig>> approvalConfig() {
        return R.ok(ncm8dApprovalConfigService.getConfig());
    }

    /** 8D 阶段审核配置:全量保存当前公司的配置(D1-D8 共 8 条)。 */
    @PostMapping("/approval-config")
    @PreAuthorize("hasAuthority('ncm.8d.create')")
    public R<Void> saveApprovalConfig(@RequestBody List<Qms8dApprovalConfig> items) {
        ncm8dApprovalConfigService.saveConfig(items);
        return R.ok();
    }
}
