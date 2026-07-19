package com.konli.qms.api.spc.controller;

import com.konli.qms.api.spc.dto.CreateSubgroupRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.service.spc.SpcSubgroupService;
import com.konli.qms.service.spc.dto.SpcSubgroupVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SPC 子组录入(手动采集一组测量值,产出 xbar/rangeR)。 */
@RestController
@RequestMapping("/api/v1/spc/subgroups")
@RequiredArgsConstructor
public class SpcSubgroupController {

    private final SpcSubgroupService spcSubgroupService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.subgroup.list')")
    public R<List<SpcSubgroup>> list() {
        return R.ok(spcSubgroupService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.subgroup.list')")
    public R<SpcSubgroupVo> get(@PathVariable String id) {
        return R.ok(spcSubgroupService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('spc.subgroup.create')")
    public R<SpcSubgroup> create(@Valid @RequestBody CreateSubgroupRequest req) {
        SpcSubgroup sg = new SpcSubgroup();
        sg.setOrgId(req.getOrgId());
        sg.setParamId(req.getParamId());
        sg.setSubgroupTime(req.getSubgroupTime());
        sg.setShift(req.getShift());
        sg.setWoNo(req.getWoNo());
        sg.setBatchNo(req.getBatchNo());
        return R.ok(spcSubgroupService.create(sg, req.getValues()));
    }
}
