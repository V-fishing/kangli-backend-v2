package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcControlLimit;
import com.konli.qms.service.spc.SpcControlLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SPC 控制限(前25子组建基线,Xbar-R 图 CL/UCL/LCL)。 */
@RestController
@RequestMapping("/api/v1/spc/control-limits")
@RequiredArgsConstructor
public class SpcControlLimitController {

    private final SpcControlLimitService spcControlLimitService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<List<SpcControlLimit>> list(@RequestParam(required = false) String paramId) {
        return R.ok(spcControlLimitService.list(paramId));
    }

    @PostMapping("/calc")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<SpcControlLimit> calc(@RequestParam String paramId) {
        return R.ok(spcControlLimitService.calc(paramId));
    }
}
