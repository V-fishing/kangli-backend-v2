package com.konli.qms.api.fia.controller;

import com.konli.qms.domain.fia.dto.CreateInspStdRequest;
import com.konli.qms.domain.fia.dto.FiaStdItemRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.service.fia.InspStdService;
import com.konli.qms.service.fia.dto.InspStdVo;
import jakarta.validation.Valid;
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

import java.util.ArrayList;
import java.util.List;

/** 检验标准库 CRUD(标准 + 检测项)。fia.std.list / fia.std.create。 */
@RestController
@RequestMapping("/api/v1/fia/stds")
@RequiredArgsConstructor
public class InspStdController {

    private final InspStdService inspStdService;

    @GetMapping
    @PreAuthorize("hasAuthority('fia.std.list')")
    public R<List<FiaInspStd>> list() {
        return R.ok(inspStdService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.std.list')")
    public R<InspStdVo> get(@PathVariable String id) {
        return R.ok(inspStdService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<FiaInspStd> create(@Valid @RequestBody CreateInspStdRequest req) {
        FiaInspStd std = new FiaInspStd();
        std.setOrgId(req.getOrgId());
        std.setCode(req.getCode());
        std.setMaterial(req.getMaterial());
        std.setProcName(req.getProcName());
        std.setAql(req.getAql());
        std.setInspectLevel(req.getInspectLevel());
        std.setSamplePlan(req.getSamplePlan());
        std.setCtqText(req.getCtqText());
        std.setStdVersion(req.getStdVersion());
        std.setStatus(req.getStatus());
        List<FiaInspStdItem> items = new ArrayList<>();
        if (req.getItems() != null) {
            for (FiaStdItemRequest ir : req.getItems()) {
                FiaInspStdItem it = new FiaInspStdItem();
                it.setSeq(ir.getSeq());
                it.setItemName(ir.getItemName());
                it.setIsCtq(ir.getIsCtq());
                it.setStdValue(ir.getStdValue());
                it.setTolerance(ir.getTolerance());
                it.setUnit(ir.getUnit());
                it.setValueType(ir.getValueType());
                it.setEnumValues(ir.getEnumValues());
                items.add(it);
            }
        }
        return R.ok(inspStdService.create(std, items));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<Void> update(@PathVariable String id, @Valid @RequestBody CreateInspStdRequest req) {
        inspStdService.update(id, req);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.std.delete')")
    public R<Void> delete(@PathVariable String id) {
        inspStdService.delete(id);
        return R.ok();
    }
}
