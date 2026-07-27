package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcNotifyChannel;
import com.konli.qms.domain.spc.entity.SpcNotifyRecord;
import com.konli.qms.service.spc.SpcNotifyChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SPC 通知渠道(启用/停用)与推送记录。 */
@RestController
@RequestMapping("/api/v1/spc/notify-channels")
@RequiredArgsConstructor
public class SpcNotifyChannelController {

    private final SpcNotifyChannelService spcNotifyChannelService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<List<SpcNotifyChannel>> list() {
        return R.ok(spcNotifyChannelService.list());
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<Void> toggle(@PathVariable String id, @RequestParam boolean enabled) {
        spcNotifyChannelService.toggle(id, enabled);
        return R.ok();
    }

    @GetMapping("/records")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<List<SpcNotifyRecord>> listRecords(@RequestParam(required = false) String alarmId) {
        return R.ok(spcNotifyChannelService.listRecords(alarmId));
    }
}
