package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmSqeVerification;
import com.konli.qms.service.sqm.SqmSqeVerificationService;
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

/** SQE 验证:查询/新增。sqm.abnormal.create */
@RestController
@RequestMapping("/api/v1/sqm/verifications")
@RequiredArgsConstructor
public class SqmSqeVerificationController {

    private final SqmSqeVerificationService sqmSqeVerificationService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.abnormal.create')")
    public R<List<SqmSqeVerification>> list(@RequestParam(required = false) String abnormalId) {
        return R.ok(sqmSqeVerificationService.list(abnormalId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.abnormal.create')")
    public R<SqmSqeVerification> get(@PathVariable String id) {
        return R.ok(sqmSqeVerificationService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.abnormal.create')")
    public R<SqmSqeVerification> create(@RequestBody SqmSqeVerification verification) {
        return R.ok(sqmSqeVerificationService.create(verification));
    }
}
