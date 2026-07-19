package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcGlobalConfig;
import com.konli.qms.service.spc.SpcGlobalConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** SPC 全局配置(按公司一行,无则返回默认)。 */
@RestController
@RequestMapping("/api/v1/spc/global-config")
@RequiredArgsConstructor
public class SpcGlobalConfigController {

    private final SpcGlobalConfigService spcGlobalConfigService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<SpcGlobalConfig> get(@RequestParam(required = false) String orgId) {
        return R.ok(spcGlobalConfigService.get(orgId));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<Void> save(@RequestBody SpcGlobalConfig config) {
        spcGlobalConfigService.save(config);
        return R.ok();
    }
}
