package com.konli.qms.api.spc.controller;

import com.konli.qms.api.spc.dto.CloseAlarmRequest;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.spc.entity.SpcAlarm;
import com.konli.qms.service.ncm.Ncm8dService;
import com.konli.qms.service.ncm.NcmDefectRecordService;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;
import com.konli.qms.service.spc.SpcAlarmService;
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

/** SPC 告警查询与处置(关闭需状态为待确认) + SPC→8D联动。 */
@RestController
@RequestMapping("/api/v1/spc/alarms")
@RequiredArgsConstructor
public class SpcAlarmController {

    private final SpcAlarmService spcAlarmService;
    private final Ncm8dService ncm8dService;
    private final NcmDefectRecordService ncmDefectRecordService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.alarm.list')")
    public R<List<SpcAlarm>> list() {
        return R.ok(spcAlarmService.list());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('spc.alarm.list')")
    public R<PageResult<SpcAlarm>> page(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String level,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return R.ok(spcAlarmService.listPage(keyword, status, level, page, size));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('spc.alarm.close')")
    public R<Void> close(@PathVariable String id, @RequestBody CloseAlarmRequest req) {
        spcAlarmService.close(id, req.getCloseReason(), req.getDisposition());
        return R.ok();
    }

    /**
     * SPC→8D 联动:告警一键发起 8D 整改。
     * 统一整改源头——先登记一条 source=SPC报警 的缺陷记录,再从该缺陷记录发起 8D,
     * 使 8D 的 sourceRefId 指向缺陷记录(而非告警),缺陷记录成为全平台 8D/CAPA/CA 的唯一源头,
     * 质量追溯链完整:8D → 缺陷记录 → SPC 告警。
     */
    @PostMapping("/{id}/launch-8d")
    @PreAuthorize("hasAuthority('spc.alarm.launch-8d')")
    public R<Qms8dReport> launch8d(@PathVariable String id) {
        SpcAlarm alarm = spcAlarmService.list().stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
        if (alarm == null) return R.fail(404, "告警不存在");

        // 1) 先落一条 SPC 来源的缺陷记录(issue/severity/来源单号均取自告警)
        NcmDefectRecord def = new NcmDefectRecord();
        def.setOrgId(alarm.getOrgId());
        def.setSource("SPC报警");
        // 关联 SPC 判异不良字典(全局 code=SPC,由 V235 幂等 seed),满足 defect_dict_code NOT NULL 约束
        def.setDefectDictCode("SPC");
        // 不良数量/批量为 NOT NULL: SPC 判异按"单子组判异点"计,兜底 defectCount=1、batchTotal=子组大小(无则5)
        def.setDefectCount(1);
        def.setBatchTotal(alarm.getSubgroupEndNo() != null && alarm.getSubgroupStartNo() != null
                ? (alarm.getSubgroupEndNo() - alarm.getSubgroupStartNo() + 1) : 5);
        def.setRemark("SPC" + alarm.getTriggeredRule() + "报警:" + alarm.getParamName());
        // 告警级别映射为不良严重度:报警→高 / 中→中 / 预警→低
        def.setSeverity("报警".equals(alarm.getLevel()) ? "高" : ("预警".equals(alarm.getLevel()) ? "低" : "中"));
        def.setWoNo(alarm.getWoNo() != null ? alarm.getWoNo() : "");
        def.setBatchNo(alarm.getBatchNo());
        def.setRemark("由 SPC 告警 " + (alarm.getCode() != null ? alarm.getCode() : alarm.getId()) + " 自动登记");
        def = ncmDefectRecordService.create(def);

        // 2) 再从该缺陷记录发起 8D(标准范式:launch8dFromDefect 内部回写缺陷记录 d8No 并指派通知)
        return R.ok((Qms8dReport) ncmDefectRecordService.launch8dFromDefect(def.getId(), new DefectLaunchRequest()));
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
