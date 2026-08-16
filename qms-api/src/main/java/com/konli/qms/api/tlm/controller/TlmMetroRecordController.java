package com.konli.qms.api.tlm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.tlm.entity.TlmMetroRecord;
import com.konli.qms.service.tlm.TlmMetroRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 计量数据采集记录接口(P2): 录入计量器具(GAUGE)实测值并绑定工单/批次。 */
@RestController
@RequestMapping("/api/v1/tlm/metro-record")
@RequiredArgsConstructor
@Slf4j
public class TlmMetroRecordController {

    private final TlmMetroRecordService metroRecordService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('tlm.metro.list')")
    public R<PageResult<TlmMetroRecord>> page(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String woNo,
                                              @RequestParam(required = false) String judged,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return R.ok(metroRecordService.page(keyword, woNo, judged, page, size));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('tlm.metro.calib','tlm.metro.list')")
    public R<TlmMetroRecord> create(@RequestBody TlmMetroRecord record) {
        return R.ok(metroRecordService.create(record));
    }
}
