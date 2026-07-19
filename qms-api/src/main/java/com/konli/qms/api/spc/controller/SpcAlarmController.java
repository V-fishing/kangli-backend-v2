package com.konli.qms.api.spc.controller;

import com.konli.qms.api.spc.dto.CloseAlarmRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcAlarm;
import com.konli.qms.service.spc.SpcAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SPC 告警查询与处置(关闭需状态为待确认)。 */
@RestController
@RequestMapping("/api/v1/spc/alarms")
@RequiredArgsConstructor
public class SpcAlarmController {

    private final SpcAlarmService spcAlarmService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.alarm.list')")
    public R<List<SpcAlarm>> list() {
        return R.ok(spcAlarmService.list());
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('spc.alarm.close')")
    public R<Void> close(@PathVariable String id, @RequestBody CloseAlarmRequest req) {
        spcAlarmService.close(id, req.getCloseReason(), req.getDisposition());
        return R.ok();
    }
}
