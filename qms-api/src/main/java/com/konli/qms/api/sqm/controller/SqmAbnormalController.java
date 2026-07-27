package com.konli.qms.api.sqm.controller;

import com.konli.qms.domain.sqm.dto.AbnormalRectificationRequest;
import com.konli.qms.api.sqm.dto.CloseAbnormalRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;
import com.konli.qms.service.sqm.SqmAbnormalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 来料异常整改单 + FMEA 高风险项。sqm.abnormal.list / sqm.abnormal.create */
@RestController
@RequestMapping("/api/v1/sqm")
@RequiredArgsConstructor
public class SqmAbnormalController {

    private final SqmAbnormalService sqmAbnormalService;

    // ---- 来料异常 ----

    @GetMapping("/abnormals")
    @PreAuthorize("hasAuthority('sqm.abnormal.list')")
    public R<List<SqmIncomingAbnormal>> listAbnormals() {
        return R.ok(sqmAbnormalService.listAbnormals());
    }

    @PostMapping("/abnormals")
    @PreAuthorize("hasAuthority('sqm.abnormal.create')")
    public R<SqmIncomingAbnormal> create(@RequestBody SqmIncomingAbnormal abnormal) {
        return R.ok(sqmAbnormalService.create(abnormal));
    }

    @PostMapping("/abnormals/{id}/close")
    @PreAuthorize("hasAuthority('sqm.abnormal.close')")
    public R<Void> close(@PathVariable String id, @RequestBody CloseAbnormalRequest req) {
        sqmAbnormalService.close(id, req.getDisposal(), req.getDisposalRemark());
        return R.ok();
    }

    /** 保存整改进度持久化(V21):通知/措施/验证/批验/关闭。 */
    @PutMapping("/abnormals/{id}/rectification")
    @PreAuthorize("hasAuthority('sqm.abnormal.create')")
    public R<Void> saveRectification(@PathVariable String id, @RequestBody AbnormalRectificationRequest req) {
        sqmAbnormalService.saveRectification(id, req);
        return R.ok();
    }

    /** 加载整改记录(措施+批验) */
    @GetMapping("/abnormals/{id}/rectification")
    @PreAuthorize("hasAuthority('sqm.abnormal.list')")
    public R<java.util.Map<String, java.util.List<?>>> loadRectificationDetail(@PathVariable String id) {
        return R.ok(sqmAbnormalService.loadRectificationDetail(id));
    }

    /** 触发重复问题升级审核(可手动调用,也可后续接定时任务)。 */
    @PostMapping("/abnormals/check-escalation")
    @PreAuthorize("hasAuthority('sqm.abnormal.escalation-check')")
    public R<Void> checkEscalation() {
        sqmAbnormalService.checkRepeatEscalation();
        return R.ok();
    }
}
