package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.sqm.entity.QmsFmeaRisk;
import com.konli.qms.domain.sqm.entity.QmsFmeaRiskTrack;
import com.konli.qms.service.sqm.SqmFmeaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sqm/fmea")
@RequiredArgsConstructor
@Tag(name = "SQM-FMEA风险跟踪")
public class SqmFmeaController {

    private final SqmFmeaService service;

    @GetMapping("/types")
    @PreAuthorize("hasAuthority('sqm.fmea.list')")
    @Operation(summary = "FMEA 类型列表")
    public R<List<String>> listTypes() {
        return R.ok(service.listTypes());
    }

    @GetMapping("/predict")
    @PreAuthorize("hasAuthority('sqm.fmea.list')")
    @Operation(summary = "依据 S/O/D 预测 RPN 与风险等级")
    public R<Map<String, Object>> predict(@RequestParam int severity,
                                          @RequestParam int occurrence,
                                          @RequestParam int detection) {
        return R.ok(service.predict(severity, occurrence, detection));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.fmea.list')")
    @Operation(summary = "FMEA 风险项列表")
    public R<List<QmsFmeaRisk>> list(@RequestParam(required = false) String status) {
        return R.ok(service.list(status));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.fmea.edit')")
    @Operation(summary = "新建 FMEA 风险项")
    public R<QmsFmeaRisk> create(@RequestBody QmsFmeaRisk risk) {
        return R.ok(service.create(risk));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.fmea.edit')")
    @Operation(summary = "更新 FMEA 风险项(措施分配/评分/状态)")
    public R<QmsFmeaRisk> update(@PathVariable String id, @RequestBody QmsFmeaRisk risk) {
        return R.ok(service.update(id, risk));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('sqm.fmea.close')")
    @Operation(summary = "高风险闭环(须证据+3个月无复发确认)")
    public R<QmsFmeaRisk> close(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String evidence = body.get("evidence") == null ? null : String.valueOf(body.get("evidence"));
        String note = body.get("note") == null ? null : String.valueOf(body.get("note"));
        boolean recurrenceVerified = Boolean.parseBoolean(String.valueOf(body.get("recurrenceVerified")));
        CompanyContext.CurrentUser u = CompanyContext.get();
        String operator = (u != null && u.username() != null) ? u.username() : "系统";
        return R.ok(service.close(id, evidence, note, recurrenceVerified, operator));
    }

    @GetMapping("/{id}/tracks")
    @PreAuthorize("hasAuthority('sqm.fmea.list')")
    @Operation(summary = "FMEA 风险项闭环轨迹")
    public R<List<QmsFmeaRiskTrack>> tracks(@PathVariable String id) {
        return R.ok(service.tracks(id));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAuthority('sqm.fmea.reopen')")
    @Operation(summary = "验证期内再发生->重新打开FMEA(status已闭环->进行中)")
    public R<QmsFmeaRisk> reopen(@PathVariable String id, @RequestParam(required = false) String reason) {
        return R.ok(service.reopen(id, reason));
    }

    @PostMapping("/scan-overdue")
    @PreAuthorize("hasAuthority('sqm.fmea.scan-overdue')")
    @Operation(summary = "扫描超期措施(>7天通知责任人,>14天通知质量经理)")
    public R<Integer> scanOverdue() {
        return R.ok(service.scanOverdue());
    }
}
