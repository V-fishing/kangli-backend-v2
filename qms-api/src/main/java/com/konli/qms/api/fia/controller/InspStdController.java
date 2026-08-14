package com.konli.qms.api.fia.controller;

import com.konli.qms.domain.fia.dto.CreateInspStdRequest;
import com.konli.qms.domain.fia.dto.FiaStdItemRequest;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.service.fia.InspStdService;
import com.konli.qms.service.fia.dto.CtqItemVo;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    public R<List<FiaInspStd>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int limit) {
        return R.ok(inspStdService.listByKeyword(keyword, limit));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('fia.std.list')")
    public R<PageResult<FiaInspStd>> page(@RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return R.ok(inspStdService.listPage(keyword, page, size));
    }

    /** 查询所有生效标准下的 CTQ 检验项(供 SPC 参数关联选择器用) — 必须在 /{id} 之前定义，防止 ctq-items 被解释为 id */
    @GetMapping("/ctq-items")
    @PreAuthorize("hasAuthority('fia.std.list')")
    public R<List<CtqItemVo>> ctqItems() {
        return R.ok(inspStdService.listCtqItems());
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
        std.setSpcProcessId(req.getSpcProcessId());
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
                it.setPassValues(ir.getPassValues());
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

    /** 启用/停用标准(仅改 status:生效/停用),不删除明细与历史数据 */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<Void> changeStatus(@PathVariable String id, @RequestParam String status) {
        inspStdService.changeStatus(id, status);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.std.delete')")
    public R<Void> delete(@PathVariable String id) {
        inspStdService.delete(id);
        return R.ok();
    }

}
