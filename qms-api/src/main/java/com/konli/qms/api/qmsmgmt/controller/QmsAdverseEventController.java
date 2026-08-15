package com.konli.qms.api.qmsmgmt.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.qmsmgmt.entity.QmsAdverseEvent;
import com.konli.qms.service.qmsmgmt.QmsAdverseEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 不良事件管理。qms-mgmt.adverse.* */
@RestController
@RequestMapping("/api/v1/qms-mgmt/adverse")
@RequiredArgsConstructor
@Slf4j
@Validated
public class QmsAdverseEventController {

    private final QmsAdverseEventService service;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('qms-mgmt.adverse.list')")
    public R<PageResult<QmsAdverseEvent>> page(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String eventType,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(defaultValue = "1") @Min(1) int page,
                                               @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return R.ok(service.page(keyword, eventType, status, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('qms-mgmt.adverse.list')")
    public R<QmsAdverseEvent> get(@PathVariable String id) {
        return R.ok(service.get(id));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('qms-mgmt.adverse.list')")
    public R<Map<String, Object>> stats() {
        return R.ok(service.stats());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('qms-mgmt.adverse.create')")
    public R<QmsAdverseEvent> create(@RequestBody QmsAdverseEvent event) {
        return R.ok(service.create(event));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('qms-mgmt.adverse.edit')")
    public R<QmsAdverseEvent> update(@RequestBody QmsAdverseEvent event) {
        return R.ok(service.update(event));
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAuthority('qms-mgmt.adverse.edit')")
    public R<Void> handle(@PathVariable String id,
                          @RequestParam String status,
                          @RequestParam(required = false) String handleDesc,
                          @RequestParam(required = false) String owner) {
        service.handle(id, status, handleDesc, owner);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('qms-mgmt.adverse.delete')")
    public R<Void> delete(@PathVariable String id) {
        service.delete(id);
        return R.ok();
    }
}
