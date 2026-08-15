package com.konli.qms.api.qmsmgmt.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.service.qmsmgmt.QmsComplianceBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 体系合规监控看板(跨模块聚合分析)。qms-mgmt.dashboard.* */
@RestController
@RequestMapping("/api/v1/qms-mgmt/board")
@RequiredArgsConstructor
@Slf4j
public class QmsComplianceBoardController {

    private final QmsComplianceBoardService service;

    @GetMapping
    @PreAuthorize("hasAuthority('qms-mgmt.dashboard.list')")
    public R<Map<String, Object>> board() {
        return R.ok(service.board());
    }
}
