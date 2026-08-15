package com.konli.qms.api.cs.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.cs.entity.CsFeedback;
import com.konli.qms.service.cs.CsFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@RequestMapping("/api/v1/cs/feedbacks")
@RequiredArgsConstructor
@Slf4j
public class CsFeedbackController {

    private final CsFeedbackService service;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('cs.feedback.list')")
    public R<PageResult<CsFeedback>> page(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String fbType,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return R.ok(service.page(keyword, fbType, status, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cs.feedback.list')")
    public R<CsFeedback> get(@PathVariable String id) {
        return R.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cs.feedback.create')")
    public R<CsFeedback> create(@RequestBody CsFeedback fb) {
        return R.ok(service.create(fb));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('cs.feedback.edit')")
    public R<CsFeedback> update(@RequestBody CsFeedback fb) {
        return R.ok(service.update(fb));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cs.feedback.delete')")
    public R<Void> delete(@PathVariable String id) {
        service.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAuthority('cs.feedback.handle')")
    public R<Void> handle(@PathVariable String id,
                          @RequestParam(required = false) String handleDetail,
                          @RequestParam(required = false) String ownerName) {
        service.handle(id, handleDetail, ownerName);
        return R.ok();
    }

    @PostMapping("/{id}/handling")
    @PreAuthorize("hasAuthority('cs.feedback.handle')")
    public R<Void> markHandling(@PathVariable String id,
                                @RequestParam(required = false) String ownerName) {
        service.markHandling(id, ownerName);
        return R.ok();
    }
}
