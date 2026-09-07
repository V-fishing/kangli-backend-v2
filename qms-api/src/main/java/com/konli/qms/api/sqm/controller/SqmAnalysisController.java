package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.service.sqm.SqmAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** SQM 分析报表:来料多维/异常多维/来料看板/供应商绩效排名。sqm.supplier.list */
@RestController
@RequestMapping("/api/v1/sqm")
@RequiredArgsConstructor
public class SqmAnalysisController {

    private final SqmAnalysisService sqmAnalysisService;

    /** 来料多维分析:按 dim(supplierId/partNo/inspectResult) 分组,可选时间范围 */
    @GetMapping("/analysis/incoming")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> incomingAnalysis(
            @RequestParam String dim,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return R.ok(sqmAnalysisService.incomingAnalysis(dim, startTime, endTime));
    }

    /** 来料异常多维分析:按 dim(supplierId/partNo/level) 分组,可选时间范围 */
    @GetMapping("/analysis/abnormal")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> abnormalAnalysis(
            @RequestParam String dim,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return R.ok(sqmAnalysisService.abnormalAnalysis(dim, startTime, endTime));
    }

    /** 来料看板:今日批次/合格率/待处理异常/Top5 不良供应商/7 日趋势 */
    @GetMapping("/dashboard/incoming")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<Map<String, Object>> dashboard() {
        return R.ok(sqmAnalysisService.dashboard());
    }

    /** 供应商绩效排名:period=YYYY-MM,按 score 降序 */
    @GetMapping("/performance/ranking")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> ranking(@RequestParam(required = false) String period) {
        return R.ok(sqmAnalysisService.ranking(period));
    }

    // ==================== 供应商看板:五聚合 ====================

    /** 供应商合格率分布:五档分桶,可选 level/keyword/月份范围 */
    @GetMapping("/supplier/pass-rate-dist")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> passRateDist(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startYm,
            @RequestParam(required = false) String endYm) {
        return R.ok(sqmAnalysisService.passRateDist(level, keyword, startYm, endYm));
    }

    /** 重点供应商合格率趋势:observeOnly 仅重点观察,supplierIds 追加对比 */
    @GetMapping("/supplier/pass-rate-trend")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> passRateTrend(
            @RequestParam(required = false, defaultValue = "true") boolean observeOnly,
            @RequestParam(required = false) List<String> supplierIds,
            @RequestParam(required = false) String startYm,
            @RequestParam(required = false) String endYm) {
        return R.ok(sqmAnalysisService.passRateTrend(observeOnly, supplierIds, startYm, endYm));
    }

    /** 质量异常热力图:供应商×月份,指定 year 与 topN */
    @GetMapping("/supplier/abnormal-heat")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> abnormalHeat(
            @RequestParam(required = false) String year,
            @RequestParam(required = false, defaultValue = "15") int topN) {
        return R.ok(sqmAnalysisService.abnormalHeat(year, topN));
    }

    /** 供应商等级占比:A/B/C/D 分布 */
    @GetMapping("/supplier/level-ratio")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> levelRatio(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword) {
        return R.ok(sqmAnalysisService.levelRatio(level, keyword));
    }

    /** 检验结论分布:合格/不合格/待检 等 */
    @GetMapping("/supplier/inspect-result")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> inspectResult(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword) {
        return R.ok(sqmAnalysisService.inspectResult(level, keyword));
    }

    /** 交付率×合格率散点 */
    @GetMapping("/supplier/delivery-vs-pass")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> deliveryVsPass(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword) {
        return R.ok(sqmAnalysisService.deliveryVsPass(level, keyword));
    }

    // ==================== 物料看板:三聚合 ====================

    /** 物料合格率分布:五档分桶,keyOnly=true 仅关键物料 */
    @GetMapping("/material/pass-rate-dist")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> materialPassRateDist(
            @RequestParam(required = false, defaultValue = "true") boolean keyOnly,
            @RequestParam(required = false) String startYm,
            @RequestParam(required = false) String endYm) {
        return R.ok(sqmAnalysisService.materialPassRateDist(keyOnly, startYm, endYm));
    }

    /** 重点物料合格率趋势:keyOnly 仅关键物料,partNos 追加对比 */
    @GetMapping("/material/pass-rate-trend")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> materialPassRateTrend(
            @RequestParam(required = false, defaultValue = "true") boolean keyOnly,
            @RequestParam(required = false) List<String> partNos,
            @RequestParam(required = false) String startYm,
            @RequestParam(required = false) String endYm) {
        return R.ok(sqmAnalysisService.materialPassRateTrend(keyOnly, partNos, startYm, endYm));
    }

    /** 物料搜索:按 partNo/partName 模糊匹配去重,供看板追加对比下拉远程搜索 */
    @GetMapping("/material/search")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> materialSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        return R.ok(sqmAnalysisService.materialSearch(keyword, limit));
    }

    /** 物料劣化预警:跌破95%警戒线或环比降幅>=2pp 预警、>=5pp 严重;分页返回 */
    @GetMapping("/material/deterioration")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<PageResult<Map<String, Object>>> materialDeterioration(
            @RequestParam(required = false, defaultValue = "true") boolean keyOnly,
            @RequestParam(required = false) String startYm,
            @RequestParam(required = false) String endYm,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return R.ok(sqmAnalysisService.materialDeterioration(keyOnly, startYm, endYm, page, size));
    }
}
