package com.konli.qms.api.fia.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaSignConfig;
import com.konli.qms.service.fia.SignConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** FIA 签名配置(每公司一行:方式/节点/粒度/锁定)。读任意已认证;写需 fia.std.create。 */
@RestController
@RequestMapping("/api/v1/fia/sign-config")
@RequiredArgsConstructor
public class SignConfigController {

    private final SignConfigService signConfigService;

    @GetMapping
    public R<FiaSignConfig> get(@RequestParam(required = false) String orgId) {
        String oid = orgId != null ? orgId
                : (CompanyContext.get() == null ? null : CompanyContext.get().orgId());
        return R.ok(signConfigService.get(oid));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<Void> save(@RequestBody FiaSignConfig config) {
        if (config.getOrgId() == null && CompanyContext.get() != null) {
            config.setOrgId(CompanyContext.get().orgId());
        }
        signConfigService.save(config);
        return R.ok();
    }
}
