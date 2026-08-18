package com.konli.qms.api.tlm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.tlm.entity.TlmMaintPlan;
import com.konli.qms.domain.tlm.entity.TlmMaintRecord;
import com.konli.qms.domain.tlm.entity.TlmTooling;
import com.konli.qms.domain.tlm.entity.TlmScrap;
import com.konli.qms.domain.tlm.entity.TlmRepair;
import com.konli.qms.domain.tlm.entity.TlmToolProduct;
import com.konli.qms.domain.tlm.entity.TlmToolVersion;
import com.konli.qms.domain.tlm.entity.TlmToolWoBind;
import com.konli.qms.domain.tlm.mapper.TlmToolProductMapper;
import com.konli.qms.service.tlm.TlmMaintService;
import com.konli.qms.service.tlm.TlmProductService;
import com.konli.qms.service.tlm.TlmToolVersionService;
import com.konli.qms.service.tlm.TlmToolingService;
import com.konli.qms.service.fia.FiaTaskService;
import com.konli.qms.common.security.CompanyContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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

/** 工装台账 + 保养 + 异常。tlm.tooling.* / tlm.maint.* */
@RestController
@RequestMapping("/api/v1/tlm")
@RequiredArgsConstructor
@Slf4j
public class TlmToolingController {

    private final TlmToolingService toolingService;
    private final TlmMaintService maintService;
    private final TlmToolVersionService versionService;
    private final TlmToolProductMapper toolProductMapper;
    private final TlmProductService productService;
    private final FiaTaskService fiaTaskService;

    // ===== 台账 =====
    @GetMapping("/tooling/page")
    @PreAuthorize("hasAuthority('tlm.tooling.list')")
    public R<com.konli.qms.common.api.PageResult<TlmTooling>> page(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String ownerId,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return R.ok(toolingService.page(keyword, category, status, ownerId, page, size));
    }

    @GetMapping("/tooling/{id}")
    @PreAuthorize("hasAuthority('tlm.tooling.list')")
    public R<TlmTooling> get(@PathVariable String id) {
        return R.ok(toolingService.get(id));
    }

    // ===== 版本变更履历(留痕) =====
    @GetMapping("/tooling/{id}/versions")
    @PreAuthorize("hasAuthority('tlm.tooling.list')")
    public R<List<TlmToolVersion>> versions(@PathVariable String id) {
        return R.ok(versionService.listByTool(id));
    }

    /** 工装是否存在待处理的工装首件任务(用于台账「待首件」强提醒)。 */
    @GetMapping("/tooling/{id}/pending-first")
    @PreAuthorize("hasAuthority('tlm.tooling.list')")
    public R<Map<String, Object>> pendingFirst(@PathVariable String id) {
        boolean pending = fiaTaskService.hasPendingToolingFirst(id);
        return R.ok(Map.of("pending", pending));
    }

    @PostMapping("/tooling/{id}/version")
    @PreAuthorize("hasAuthority('tlm.tooling.edit')")
    public R<TlmToolVersion> addVersion(@PathVariable String id, @RequestBody TlmToolVersion version) {
        version.setToolId(id);
        TlmToolVersion saved = versionService.create(version);
        // 版本变更后触发 FIA 首件检验任务(工装变更后)
        // 前置校验由 createFromTooling 内部完成，异常直接传播到前端
        TlmTooling tooling = toolingService.get(id);
        if (tooling != null && tooling.getProductCode() != null && !tooling.getProductCode().isBlank()
                && tooling.getProcName() != null && !tooling.getProcName().isBlank()) {
            fiaTaskService.createFromTooling(
                    tooling.getOrgId(),
                    tooling.getId(),
                    null,
                    tooling.getProductCode(),
                    tooling.getProcName(),
                    tooling.getToolName(),
                    null,
                    "工装变更后",
                    null, // batchNo 自动触发场景兜底生成
                    tooling.getSupplierId(),
                    String.format("工装 %s(%s) 版本变更(V%s)后自动触发首件检验",
                            tooling.getToolName(), tooling.getToolNo(), saved.getVersionNo())
            );
        }
        return R.ok(saved);
    }

    // ===== 关联产品(MES 真实来源) =====
    @GetMapping("/tooling/{id}/products")
    @PreAuthorize("hasAuthority('tlm.tooling.list')")
    public R<List<TlmToolProduct>> products(@PathVariable String id) {
        return R.ok(toolProductMapper.selectList(
            new LambdaQueryWrapper<TlmToolProduct>().eq(TlmToolProduct::getToolId, id).orderByDesc(TlmToolProduct::getCreatedAt)));
    }

    /** MES 候选产品(按 material_code 去重), 供关联选择。 */
    @GetMapping("/products/candidates")
    @PreAuthorize("hasAuthority('tlm.tooling.list')")
    public R<List<com.konli.qms.domain.tlm.vo.TlmProductCandidate>> candidates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String kind) {
        return R.ok(productService.candidates(keyword, kind));
    }

    /** 某产品编码在 MES 的检验记录明细。 */
    @GetMapping("/products/{code}/detail")
    @PreAuthorize("hasAuthority('tlm.tooling.list')")
    public R<List<com.konli.qms.domain.tlm.vo.TlmProductDetail>> productDetail(@PathVariable("code") String code) {
        return R.ok(productService.detail(code));
    }

    /** 关联产品(写 tlm_tool_product, 唯一键 org+tool+product_code 防重)。 */
    @PostMapping("/tooling/{id}/product")
    @PreAuthorize("hasAuthority('tlm.tooling.edit')")
    public R<TlmToolProduct> relateProduct(@PathVariable String id, @RequestBody TlmToolProduct body) {
        String orgId = (CompanyContext.get() != null) ? CompanyContext.get().orgId() : null;
        TlmToolProduct exist = toolProductMapper.selectOne(new LambdaQueryWrapper<TlmToolProduct>()
                .eq(TlmToolProduct::getToolId, id).eq(TlmToolProduct::getProductCode, body.getProductCode()).last("LIMIT 1"));
        if (exist != null) {
            return R.ok(exist);
        }
        body.setToolId(id);
        body.setOrgId(orgId);
        toolProductMapper.insert(body);
        return R.ok(body);
    }

    /** 取消关联。 */
    @DeleteMapping("/tooling/{id}/product/{pid}")
    @PreAuthorize("hasAuthority('tlm.tooling.edit')")
    public R<Void> unrelateProduct(@PathVariable String id, @PathVariable String pid) {
        toolProductMapper.delete(new LambdaQueryWrapper<TlmToolProduct>().eq(TlmToolProduct::getId, pid).eq(TlmToolProduct::getToolId, id));
        return R.ok();
    }

    @PostMapping("/tooling")
    @PreAuthorize("hasAuthority('tlm.tooling.create')")
    public R<TlmTooling> create(@RequestBody TlmTooling tooling) {
        return R.ok(toolingService.create(tooling));
    }

    @PutMapping("/tooling")
    @PreAuthorize("hasAuthority('tlm.tooling.edit')")
    public R<TlmTooling> update(@RequestBody TlmTooling tooling) {
        return R.ok(toolingService.update(tooling));
    }

    @DeleteMapping("/tooling/{id}")
    @PreAuthorize("hasAuthority('tlm.tooling.delete')")
    public R<Void> delete(@PathVariable String id) {
        toolingService.delete(id);
        return R.ok();
    }

    @PostMapping("/tooling/{id}/repair")
    @PreAuthorize("hasAnyAuthority('tlm.tooling.repair','tlm.metro.repair')")
    public R<Void> repair(@PathVariable String id,
                          @RequestParam(required = false) String faultDesc,
                          @RequestParam(required = false) String faultType,
                          @RequestParam(required = false) String approverId) {
        toolingService.repair(id, faultDesc, faultType, approverId);
        return R.ok();
    }

    @PostMapping("/tooling/{id}/scrap")
    @PreAuthorize("hasAnyAuthority('tlm.tooling.scrap','tlm.metro.scrap')")
    public R<Void> scrap(@PathVariable String id,
                         @RequestParam(required = false) String scrapMethod,
                         @RequestParam(required = false) String reason,
                         @RequestParam(required = false) String approverId) {
        toolingService.scrap(id, scrapMethod, reason, approverId);
        return R.ok();
    }

    @PostMapping("/tooling/{id}/repair-complete")
    @PreAuthorize("hasAuthority('tlm.tooling.repair')")
    public R<Void> repairComplete(@PathVariable String id) {
        toolingService.onRepairCompleted(id);
        return R.ok();
    }

    /** 维修工单填写措施: PENDING -> REPAIRING。 */
    @PostMapping("/tooling/{id}/repair-fill")
    @PreAuthorize("hasAuthority('tlm.tooling.repair')")
    public R<Void> repairFill(@PathVariable String id, @RequestParam(required = false) String measure) {
        toolingService.repairFill(id, measure);
        return R.ok();
    }

    /** 维修完成(措施已填): REPAIRING -> DONE。verifyPass=false 验证不通过自动锁定。 */
    @PostMapping("/tooling/{id}/repair-done")
    @PreAuthorize("hasAuthority('tlm.tooling.repair')")
    public R<Void> repairDone(@PathVariable String id,
                              @RequestParam(defaultValue = "true") boolean verifyPass) {
        toolingService.onRepairCompleted(id, verifyPass);
        return R.ok();
    }

    @PostMapping("/tooling/{id}/lock")
    @PreAuthorize("hasAnyAuthority('tlm.tooling.lock','tlm.metro.lock')")
    public R<Void> lock(@PathVariable String id, @RequestParam boolean locked) {
        toolingService.lock(id, locked);
        return R.ok();
    }

    @PostMapping("/tooling/{id}/bind")
    @PreAuthorize("hasAuthority('tlm.tooling.bind')")
    public R<Void> bind(@PathVariable String id, @RequestParam String woNo) {
        toolingService.bind(id, woNo);
        return R.ok();
    }

    @GetMapping("/tooling/abnormal/{type}")
    @PreAuthorize("hasAuthority('tlm.abnormal.list')")
    public R<List<TlmTooling>> abnormal(@PathVariable String type) {
        return R.ok(toolingService.abnormalList(type));
    }

    // ===== 审批中心回调(内部/审批中心调用) =====
    @PostMapping("/scrap/{scrapId}/approve")
    @PreAuthorize("hasAuthority('tlm.scrap.approve')")
    public R<Void> approveScrap(@PathVariable String scrapId) {
        toolingService.onScrapApproved(scrapId);
        return R.ok();
    }

    @PostMapping("/scrap/{scrapId}/reject")
    @PreAuthorize("hasAuthority('tlm.scrap.approve')")
    public R<Void> rejectScrap(@PathVariable String scrapId) {
        toolingService.onScrapRejected(scrapId);
        return R.ok();
    }

    // ===== 维修审批中心回调 =====
    @PostMapping("/repair/{repairId}/approve")
    @PreAuthorize("hasAuthority('tlm.repair.approve')")
    public R<Void> approveRepair(@PathVariable String repairId) {
        toolingService.onRepairApproved(repairId);
        return R.ok();
    }

    @PostMapping("/repair/{repairId}/reject")
    @PreAuthorize("hasAuthority('tlm.repair.approve')")
    public R<Void> rejectRepair(@PathVariable String repairId) {
        toolingService.onRepairRejected(repairId);
        return R.ok();
    }

    // ===== 报废单查询 =====
    @GetMapping("/scrap/page")
    @PreAuthorize("hasAuthority('tlm.tooling.scrap')")
    public R<com.konli.qms.common.api.PageResult<TlmScrap>> scrapPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String scrapNo,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(toolingService.scrapPage(keyword, scrapNo, status, page, size));
    }

    // 详情页审批渠道: 按工装精确查 PENDING 报废单(不受组织过滤)
    @GetMapping("/scrap/by-tool/{toolId}")
    @PreAuthorize("hasAuthority('tlm.tooling.scrap')")
    public R<TlmScrap> pendingScrapByTool(@PathVariable String toolId) {
        return R.ok(toolingService.pendingScrapByTool(toolId));
    }

    // ===== 维修工单查询 =====
    @GetMapping("/repair/page")
    @PreAuthorize("hasAuthority('tlm.repair.list')")
    public R<com.konli.qms.common.api.PageResult<TlmRepair>> repairPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(toolingService.repairPage(keyword, status, page, size));
    }

    // ===== 工装维修根因分析(故障类型分布/高频工装/月度趋势) =====
    @GetMapping("/repair/analysis")
    @PreAuthorize("hasAuthority('tlm.repair.list')")
    public R<java.util.Map<String, Object>> repairAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return R.ok(toolingService.repairAnalysis(startDate, endDate));
    }

    // ===== 计量看板(GAUGE 总数/合格/限用/超期) =====
    @GetMapping("/tooling/metro/dashboard")
    @PreAuthorize("hasAuthority('tlm.metro.list')")
    public R<java.util.Map<String, Object>> metroDashboard() {
        return R.ok(toolingService.metroDashboard());
    }

    // ===== 工装-工单绑定记录(含 GAUGE 校准快照, 计量追溯反查) =====
    @GetMapping("/tooling/{id}/binds")
    @PreAuthorize("hasAuthority('tlm.metro.list')")
    public R<java.util.List<TlmToolWoBind>> bindRecords(@PathVariable String id) {
        return R.ok(toolingService.bindRecords(id));
    }

    // ===== 保养 =====
    @GetMapping("/maint/plans")
    @PreAuthorize("hasAuthority('tlm.maint.list')")
    public R<com.konli.qms.common.api.PageResult<TlmMaintPlan>> planPage(@RequestParam(required = false) String toolId,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return R.ok(maintService.planPage(toolId, page, size));
    }

    @PostMapping("/maint/plan")
    @PreAuthorize("hasAuthority('tlm.maint.plan.create')")
    public R<TlmMaintPlan> createPlan(@RequestBody TlmMaintPlan plan) {
        return R.ok(maintService.createPlan(plan));
    }

    @PutMapping("/maint/plan")
    @PreAuthorize("hasAuthority('tlm.maint.plan.edit')")
    public R<TlmMaintPlan> updatePlan(@RequestBody TlmMaintPlan plan) {
        return R.ok(maintService.updatePlan(plan));
    }

    @DeleteMapping("/maint/plan/{id}")
    @PreAuthorize("hasAuthority('tlm.maint.plan.delete')")
    public R<Void> deletePlan(@PathVariable String id) {
        maintService.deletePlan(id);
        return R.ok();
    }

    @GetMapping("/maint/records")
    @PreAuthorize("hasAuthority('tlm.maint.list')")
    public R<List<TlmMaintRecord>> records(@RequestParam(required = false) String toolId) {
        return R.ok(maintService.recordList(toolId));
    }

    @PostMapping("/maint/record")
    @PreAuthorize("hasAuthority('tlm.maint.record.create')")
    public R<TlmMaintRecord> createRecord(@RequestBody TlmMaintRecord record) {
        return R.ok(maintService.createRecord(record));
    }
}
