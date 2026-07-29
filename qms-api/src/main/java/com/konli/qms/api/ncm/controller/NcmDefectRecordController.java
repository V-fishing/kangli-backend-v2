package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.service.ncm.NcmDefectRecordService;
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
import java.util.Map;

/** 不良记录查询/录入 + 分析报表(多维/趋势/环比同比/实时看板)。ncm.record.list / ncm.record.create */
@RestController
@RequestMapping("/api/v1/ncm")
@RequiredArgsConstructor
public class NcmDefectRecordController {

    private final NcmDefectRecordService ncmDefectRecordService;

    // ---------------- 不良记录 CRUD ----------------

    @GetMapping("/defect-records")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<List<NcmDefectRecord>> list() {
        return R.ok(ncmDefectRecordService.list());
    }

    @GetMapping("/defect-records/{id}")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<NcmDefectRecord> get(@PathVariable String id) {
        return R.ok(ncmDefectRecordService.get(id));
    }

    @GetMapping("/defect-records/by-number/{defectNo}")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<NcmDefectRecord> getByDefectNo(@PathVariable String defectNo) {
        return R.ok(ncmDefectRecordService.getByDefectNo(defectNo));
    }

    @PostMapping("/defect-records")
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<NcmDefectRecord> create(@RequestBody NcmDefectRecord record) {
        return R.ok(ncmDefectRecordService.create(record));
    }

    // ---------------- 分析报表 ----------------

    /** 多维分析:按 dim(processCode/defectDictCode/deviceCode/batchNo) 分组,可选时间范围 */
    @GetMapping("/analysis/multi-dim")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<List<Map<String, Object>>> multiDimAnalysis(
            @RequestParam String dim,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return R.ok(ncmDefectRecordService.multiDimAnalysis(dim, startTime, endTime));
    }

    /** 趋势报表:按 granularity(day/week/month) 聚合,可选时间范围 */
    @GetMapping("/analysis/trend")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<List<Map<String, Object>>> trendAnalysis(
            @RequestParam(required = false, defaultValue = "day") String granularity,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return R.ok(ncmDefectRecordService.trendAnalysis(granularity, startTime, endTime));
    }

    /** 环比同比:period=YYYY-MM,type∈{week,month,year,mtd};返回不良率% */
    @GetMapping("/analysis/compare")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<Map<String, Object>> compareAnalysis(@RequestParam String period,
                                                  @RequestParam(required = false, defaultValue = "month") String type) {
        return R.ok(ncmDefectRecordService.compareAnalysis(period, type));
    }

    /** 实时看板:今日不良数/当前班次不良率/PPM/Top5 不良类型/工序热力图/数据新鲜度 */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<Map<String, Object>> dashboard() {
        return R.ok(ncmDefectRecordService.dashboard());
    }

    /** 趋势异常检测(SR-NCM-017):连续5天defectRate上升->标红预警+通知 */
    @PostMapping("/analysis/check-anomaly")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<Map<String, Object>> checkTrendAnomaly() {
        return R.ok(ncmDefectRecordService.checkTrendAnomaly());
    }

    /** 不良记录一键发起8D(跨模块:defect->8D) */
    @PostMapping("/defect-records/{id}/launch-8d")
    @PreAuthorize("hasAuthority('ncm.8d.create')")
    public R<Object> launch8dFromDefect(@PathVariable String id) {
        return R.ok(ncmDefectRecordService.launch8dFromDefect(id));
    }

    /** 不良记录一键发起CAPA(跨模块:defect->CAPA) */
    @PostMapping("/defect-records/{id}/launch-capa")
    @PreAuthorize("hasAuthority('ncm.capa.create')")
    public R<Object> launchCapaFromDefect(@PathVariable String id) {
        return R.ok(ncmDefectRecordService.launchCapaFromDefect(id));
    }

    /** 不良记录一键发起CA(跨模块:defect->CA) */
    @PostMapping("/defect-records/{id}/launch-ca")
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<Object> launchCaFromDefect(@PathVariable String id) {
        return R.ok(ncmDefectRecordService.launchCaFromDefect(id));
    }
}
