package com.konli.qms.api.fia.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaInterceptConfig;
import com.konli.qms.service.fia.FiaInterceptConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 拦截配置(每公司一行:模式/多触发/SLA/升级)。fia.std.create。 */
@RestController
@RequestMapping("/api/v1/fia/intercept-config")
@RequiredArgsConstructor
public class FiaInterceptConfigController {

    private final FiaInterceptConfigService fiaInterceptConfigService;

    @GetMapping
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<FiaInterceptConfig> get(@RequestParam(required = false) String orgId) {
        String oid = orgId != null ? orgId
                : (CompanyContext.get() == null ? null : CompanyContext.get().orgId());
        return R.ok(fiaInterceptConfigService.get(oid));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<Void> save(@RequestBody FiaInterceptConfig config) {
        if (config.getOrgId() == null && CompanyContext.get() != null) {
            config.setOrgId(CompanyContext.get().orgId());
        }
        fiaInterceptConfigService.save(config);
        return R.ok();
    }
}
