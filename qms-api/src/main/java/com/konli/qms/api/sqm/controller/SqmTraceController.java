package com.konli.qms.api.sqm.controller;

import com.konli.qms.api.sqm.dto.CreateLotRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmTraceNode;
import com.konli.qms.service.sqm.SqmTraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 来料批次 + 追溯节点:创建/查询/追溯树。sqm.trace.list / sqm.trace.create */
@RestController
@RequestMapping("/api/v1/sqm")
@RequiredArgsConstructor
public class SqmTraceController {

    private final SqmTraceService sqmTraceService;

    // ---- 来料批次 ----

    @GetMapping("/lots")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<SqmIncomingLot>> listLots() {
        return R.ok(sqmTraceService.listLots());
    }

    @PostMapping("/lots")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<SqmIncomingLot> createLot(@RequestBody CreateLotRequest req) {
        SqmIncomingLot lot = new SqmIncomingLot();
        lot.setOrgId(req.getOrgId());
        lot.setSupplierId(req.getSupplierId());
        lot.setPartNo(req.getPartNo());
        lot.setPartName(req.getPartName());
        lot.setQty(req.getQty());
        lot.setUnit(req.getUnit());
        lot.setIncomingDate(req.getIncomingDate());
        lot.setInspectResult(req.getInspectResult());
        lot.setInspectType(req.getInspectType());
        lot.setPoNo(req.getPoNo());
        lot.setIsKeyPart(req.getIsKeyPart());
        return R.ok(sqmTraceService.createLot(lot));
    }

    // ---- 追溯树 ----

    @GetMapping("/trace/tree")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<SqmTraceNode>> traceTree(@RequestParam String rootLotId) {
        return R.ok(sqmTraceService.traceTree(rootLotId));
    }

    @PostMapping("/trace/nodes")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<SqmTraceNode> createNode(@RequestBody SqmTraceNode node) {
        return R.ok(sqmTraceService.createNode(node));
    }
}
