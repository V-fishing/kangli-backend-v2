package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcCapability;
import com.konli.qms.service.spc.SpcCapabilityService;
import com.konli.qms.service.spc.dto.SpcSupplierCpkVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SPC 过程能力指数计算(Cp/Cpk/Pp/Ppk 等,按周期聚合)。 */
@RestController
@RequestMapping("/api/v1/spc/capability")
@RequiredArgsConstructor
public class SpcCapabilityController {

    private final SpcCapabilityService spcCapabilityService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.capability.list')")
    public R<List<SpcCapability>> list() {
        return R.ok(spcCapabilityService.list());
    }

    @PostMapping("/calc")
    @PreAuthorize("hasAuthority('spc.capability.list')")
    public R<SpcCapability> calc(@RequestParam String paramId,
                                 @RequestParam(required = false) String periodType,
                                 @RequestParam(required = false) String periodValue) {
        return R.ok(spcCapabilityService.calc(paramId, periodType, periodValue));
    }

    /** 能力趋势:最近 months 个周期(默认 12,按时间正序返回)。 */
    @GetMapping("/trend")
    @PreAuthorize("hasAuthority('spc.capability.list')")
    public R<List<SpcCapability>> trend(@RequestParam(required = false) String paramId,
                                        @RequestParam(defaultValue = "12") int months) {
        return R.ok(spcCapabilityService.trend(paramId, months));
    }

    /** 看板"跨参数 CPK 对比":对每个参数实时计算 CPK(无供应商维度)。 */
    @GetMapping("/supplier-cpk")
    @PreAuthorize("hasAuthority('spc.capability.list')")
    public R<List<SpcSupplierCpkVo>> supplierCpk() {
        return R.ok(spcCapabilityService.getSupplierCpk());
    }
}
