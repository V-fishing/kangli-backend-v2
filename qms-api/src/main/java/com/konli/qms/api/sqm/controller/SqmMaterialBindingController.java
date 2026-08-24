package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.service.sqm.SqmMaterialBindingService;
import com.konli.qms.service.sqm.dto.MaterialBindingCreateRequest;
import com.konli.qms.service.sqm.dto.MaterialBindingUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MES 关键物料绑定关系管理: sqm.binding.list / sqm.binding.create / sqm.binding.edit / sqm.binding.delete */
@RestController
@RequestMapping("/api/v1/sqm/bindings")
@RequiredArgsConstructor
public class SqmMaterialBindingController {

    private final SqmMaterialBindingService bindingService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('sqm.binding.list')")
    public R<PageResult<Map<String, Object>>> page(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String category,
                                                   @RequestParam(required = false) String isActive,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return R.ok(bindingService.list(keyword, category, isActive, page, size));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.binding.create')")
    public R<Map<String, Object>> create(@RequestBody MaterialBindingCreateRequest req) {
        return R.ok(bindingService.create(req));
    }

    /** 绑定候选搜索(完工检验「绑定父子级」): role=child 搜子件(半成品/来料); role=parent 搜父级(成品/半成品)。 */
    @GetMapping("/candidates")
    @PreAuthorize("hasAuthority('sqm.binding.create')")
    public R<java.util.List<Map<String, Object>>> candidates(@RequestParam String role,
                                                             @RequestParam(required = false) String keyword,
                                                             @RequestParam(defaultValue = "50") int limit) {
        return R.ok(bindingService.candidates(role, keyword, limit));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('sqm.binding.edit')")
    public R<Map<String, Object>> update(@RequestParam String productBarcode,
                                         @RequestParam String materialBarcode,
                                         @RequestParam String category,
                                         @RequestBody MaterialBindingUpdateRequest req) {
        return R.ok(bindingService.update(productBarcode, materialBarcode, category, req));
    }

    @PutMapping("/deactivate")
    @PreAuthorize("hasAuthority('sqm.binding.edit')")
    public R<Void> deactivate(@RequestParam String productBarcode,
                              @RequestParam String materialBarcode,
                              @RequestParam String category) {
        bindingService.deactivate(productBarcode, materialBarcode, category);
        return R.ok();
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('sqm.binding.delete')")
    public R<Void> delete(@RequestParam String productBarcode,
                          @RequestParam String materialBarcode,
                          @RequestParam String category) {
        bindingService.delete(productBarcode, materialBarcode, category);
        return R.ok();
    }
}
