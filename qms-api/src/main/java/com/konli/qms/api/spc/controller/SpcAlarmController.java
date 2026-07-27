package com.konli.qms.api.spc.controller;

import com.konli.qms.api.spc.dto.CloseAlarmRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.spc.entity.SpcAlarm;
import com.konli.qms.service.ncm.Ncm8dService;
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

/** SPC 告警查询与处置(关闭需状态为待确认) + SPC→8D联动。 */
@RestController
@RequestMapping("/api/v1/spc/alarms")
@RequiredArgsConstructor
public class SpcAlarmController {

    private final SpcAlarmService spcAlarmService;
    private final Ncm8dService ncm8dService;

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

    /** SPC→8D 联动:报警一键发起8D整改(source=SPC报警, sourceRefId=告警ID) */
    @PostMapping("/{id}/launch-8d")
    @PreAuthorize("hasAuthority('spc.alarm.launch-8d')")
    public R<Qms8dReport> launch8d(@PathVariable String id) {
        SpcAlarm alarm = spcAlarmService.list().stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
        if (alarm == null) return R.fail(404, "告警不存在");
        Qms8dReport r = new Qms8dReport();
        r.setOrgId(alarm.getOrgId());
        r.setSource("SPC报警");
        r.setSourceRefId(alarm.getCode() != null ? alarm.getCode() : alarm.getId().replace("-", ""));
        r.setIssue("SPC" + alarm.getTriggeredRule() + "报警:" + alarm.getParamName());
        r.setSeverity("报警".equals(alarm.getLevel()) ? "高" : "中");
        r.setTeam("质量团队");
        return R.ok(ncm8dService.create(r));
    }

    /** 查询该 SPC 告警已关联的 8D 报告(未发起则返回 null),用于详情弹窗跳转。 */
    @GetMapping("/{id}/8d")
    @PreAuthorize("hasAuthority('spc.alarm.list')")
    public R<Qms8dReport> linked8d(@PathVariable String id) {
        SpcAlarm alarm = spcAlarmService.list().stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
        if (alarm == null) return R.fail(404, "告警不存在");
        Qms8dReport linked = ncm8dService.findBySourceRef("SPC报警",
            alarm.getCode() != null ? alarm.getCode() : alarm.getId().replace("-", ""));
        return R.ok(linked);
    }
}
