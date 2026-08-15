package com.konli.qms.api.fia.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.api.fia.dto.CreateFiaTaskRequest;
import com.konli.qms.api.fia.dto.CreateFromToolingRequest;
import com.konli.qms.api.fia.dto.InspItemResultRequest;
import com.konli.qms.api.fia.dto.SignRequest;
import com.konli.qms.service.fia.dto.ProductSearchResult;
import com.konli.qms.service.fia.dto.ProductTreeNode;
import com.konli.qms.service.fia.dto.TaskStdItemVo;
import com.konli.qms.domain.fia.dto.StdTraceResult;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaArchivedReport;
import com.konli.qms.domain.fia.entity.FiaInspItem;
import com.konli.qms.domain.fia.entity.FiaInspPlan;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.fia.mapper.FiaInspPlanMapper;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.mapper.SqmIncomingLotMapper;
import com.konli.qms.domain.tlm.entity.TlmTooling;
import com.konli.qms.domain.tlm.mapper.TlmToolingMapper;
import com.konli.qms.service.fia.AqlSamplingUtil;
import com.konli.qms.service.fia.FiaDashboardService;
import com.konli.qms.service.fia.FiaTaskService;
import com.konli.qms.service.fia.dto.FiaTaskVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 首件检验任务:create -> 录入 -> 检验签名 -> 复核签名(两级完成/三级待批准)-> 批准签名(三级完成)。
 * 签名带密码(SignRequest),经配置的 sign_methods 校验 + 锁定。
 */
@RestController
@RequestMapping("/api/v1/fia/tasks")
@RequiredArgsConstructor
public class FiaTaskController {

    private final FiaTaskService fiaTaskService;
    private final FiaDashboardService fiaDashboardService;
    private final FiaInspPlanMapper fiaInspPlanMapper;
    private final SqmIncomingLotMapper sqmIncomingLotMapper;
    private final TlmToolingMapper tlmToolingMapper;

    /** FIA 看板:今日任务/完成数、合格率、超时数、状态分布、近7天趋势。 */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<Map<String, Object>> dashboard() {
        return R.ok(fiaDashboardService.dashboard());
    }

    /**
     * 按 setup(工单+物料+工序)查询最新一条首件任务结论,供量产监控前置校验:
     * 首件未合格放行时,对应 setup 的量产 SPC 采集不允许提交。
     */
    @GetMapping("/by-setup")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<Map<String, Object>> bySetup(@RequestParam(required = false) String woNo,
                                          @RequestParam(required = false) String partNo,
                                          @RequestParam(required = false) String procName) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        FiaTask latest = fiaTaskService.findLatestBySetup(orgId, woNo, partNo, procName);
        Map<String, Object> result = new LinkedHashMap<>();
        if (latest == null) {
            result.put("exists", false);
            result.put("released", false);
            result.put("message", "该 setup 尚无首件检验记录,量产监控未启动");
            return R.ok(result);
        }
        // 放行判定:状态=已完成 且 综合判定=合格(或审批放行)
        boolean released = "已完成".equals(latest.getStatus())
                && latest.getOverallJudge() != null
                && ("合格".equals(latest.getOverallJudge()) || "警告".equals(latest.getOverallJudge()));
        result.put("exists", true);
        result.put("released", released);
        result.put("taskId", latest.getId());
        result.put("code", latest.getCode());
        result.put("status", latest.getStatus());
        result.put("overallJudge", latest.getOverallJudge());
        result.put("woNo", latest.getWoNo());
        result.put("partNo", latest.getPartNo());
        result.put("procName", latest.getProcName());
        result.put("message", released ? "首件已合格放行,可启动量产监控" : "首件未放行,量产监控未启动");
        return R.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('fia.task.list')")
        public R<List<FiaTask>> list(@RequestParam(required = false) String status,
                                 @RequestParam(required = false) String woNo,
                                 @RequestParam(required = false) String productName,
                                 @RequestParam(required = false) String partNo,
                                 @RequestParam(required = false) String procName,
                                 @RequestParam(required = false) String triggerType) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        return R.ok(fiaTaskService.list(orgId, status, woNo, productName, partNo, procName, triggerType));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<PageResult<FiaTask>> page(@RequestParam(required = false) String status,
                                       @RequestParam(required = false) String woNo,
                                       @RequestParam(required = false) String productName,
                                       @RequestParam(required = false) String partNo,
                                       @RequestParam(required = false) String procName,
                                       @RequestParam(required = false) String triggerType,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        return R.ok(fiaTaskService.listPage(orgId, status, woNo, productName, partNo, procName, triggerType, page, size));
    }

    /** 产品→工序 二级树(去重汇总),供列表/筛选构建树 */
    @GetMapping("/products")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<List<ProductTreeNode>> productTree() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        return R.ok(fiaTaskService.listProductTree(orgId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<FiaTaskVo> get(@PathVariable String id) {
        return R.ok(fiaTaskService.get(id));
    }

    /** 来料批次驱动:按 物料编码 + 供应商 + 工序 从标准库自动匹配检验标准 */
    @GetMapping("/match-std")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<FiaInspStd> matchStd(@RequestParam String orgId,
                                  @RequestParam String partNo,
                                  @RequestParam(required = false) String supplierId,
                                  @RequestParam(required = false) String procName) {
        return R.ok(fiaTaskService.matchStd(orgId, partNo, supplierId, procName));
    }

    /** 产品料号模糊搜索:在所有FIA任务/标准库中检索产品,标注"新"/"旧" */
    @GetMapping("/search-product")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<List<ProductSearchResult>> searchProduct(@RequestParam String orgId,
                                                       @RequestParam String keyword,
                                                       @RequestParam(required = false) String category) {
        return R.ok(fiaTaskService.searchProduct(orgId, keyword, category));
    }

    /** 按FIA任务获取关联的检验标准项(供SPC采集页加载参数列表) */
    @GetMapping("/{id}/std-items")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<List<TaskStdItemVo>> getTaskStdItems(@PathVariable String id) {
        return R.ok(fiaTaskService.getTaskStdItems(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('fia.task.create')")
    @com.konli.qms.common.audit.Auditable(module = "FIA", action = "CREATE", recordExpr = "#result.data.id", detailExpr = "'首件任务:' + #req.woNo")
    public R<FiaTask> create(@Valid @RequestBody CreateFiaTaskRequest req) {
        // 工单号为空时由后端自动生成
        String woNo = (req.getWoNo() == null || req.getWoNo().isBlank())
                ? fiaTaskService.generateWoNo(req.getOrgId())
                : req.getWoNo();

        FiaTask task = new FiaTask();
        task.setOrgId(req.getOrgId());
        task.setWoNo(woNo);
        task.setLineName(req.getLineName()); // lineName 可选(前端已删除产线字段)
        task.setProductName(req.getProductName());
        task.setProcName(req.getProcName());
        task.setTriggerType(req.getTriggerType());
        task.setStdId(req.getStdId());
        task.setPartNo(req.getPartNo());
        task.setSupplierId(req.getSupplierId());
        task.setLotId(req.getLotId());
        task.setBatchNo(req.getBatchNo());
        task.setStdItemIds(req.getStdItemIds()); // 选中的标准项(空=全量)
        task.setIsUrgent(req.getIsUrgent());
        task.setRemark(req.getRemark());
        task.setCategory(req.getCategory());
        return R.ok(fiaTaskService.create(task));
    }

    /**
     * 工装首件检验任务创建(人工入口)。
     * 由前端「新建检验任务 / 工装首件」分支或工装台账「创建首件」按钮调用。
     * 按 toolId 取工装档案的 product_code + proc_name 自动匹配 FIA 标准，批次号必填(与生产批次绑定)。
     */
    @PostMapping("/from-tooling")
    @PreAuthorize("hasAuthority('fia.task.create')")
    public R<FiaTask> createFromTooling(@Valid @RequestBody CreateFromToolingRequest req) {
        TlmTooling tooling = tlmToolingMapper.selectById(req.getToolId());
        if (tooling == null) {
            throw new com.konli.qms.common.exception.BusinessException(400, "工装不存在");
        }
        FiaTask task = fiaTaskService.createFromTooling(
                req.getOrgId(),
                tooling.getId(),
                null, // woNo 自动生成
                tooling.getProductCode(),
                tooling.getProcName(),
                tooling.getToolName(),
                null, // lineName 兜底
                req.getTriggerType(),
                req.getBatchNo(),
                tooling.getSupplierId(),
                req.getRemark() != null && !req.getRemark().isBlank()
                        ? req.getRemark()
                        : String.format("工装 %s(%s) 人工发起首件检验", tooling.getToolName(), tooling.getToolNo()));
        return R.ok(task);
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('fia.task.create')")
    public R<Void> enterResults(@PathVariable String id, @RequestBody InspItemResultRequest req) {
        List<FiaInspItem> items = new ArrayList<>();
        if (req.getItems() != null) {
            for (InspItemResultRequest.Item ir : req.getItems()) {
                FiaInspItem it = new FiaInspItem();
                it.setId(ir.getId());
                it.setMeasuredValue(ir.getMeasuredValue());
                it.setJudge(ir.getJudge());
                items.add(it);
            }
        }
        fiaTaskService.enterResults(id, items);
        return R.ok();
    }

    /** 检验结果试算:依据标准规则预判合格/不合格,不可匹配返回 null(人工兜底)。 */
    @PostMapping("/{id}/items/preview")
    @PreAuthorize("hasAuthority('fia.task.create')")
    public R<List<com.konli.qms.domain.fia.dto.PreviewJudgeResult>> previewItems(
            @PathVariable String id, @RequestBody com.konli.qms.domain.fia.dto.PreviewJudgeRequest req) {
        return R.ok(fiaTaskService.previewJudge(id, req));
    }

    /** 标准引用追溯:列出引用该标准(可精确到项)的首件任务及命中检验项。 */
    @GetMapping("/trace")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<StdTraceResult> traceByStd(@RequestParam String stdId,
                                       @RequestParam(required = false) String itemId) {
        return R.ok(fiaTaskService.traceStd(stdId, itemId));
    }

    @PostMapping("/{id}/sign-inspector")
    @PreAuthorize("hasAuthority('fia.sign.inspector')")
    public R<Void> signInspector(@PathVariable String id, @RequestBody SignRequest req) {
        fiaTaskService.signInspector(id, req.getPassword(), req.getItemId());
        return R.ok();
    }

    @PostMapping("/{id}/sign-reviewer")
    @PreAuthorize("hasAuthority('fia.sign.reviewer')")
    public R<Void> signReviewer(@PathVariable String id, @RequestBody SignRequest req) {
        fiaTaskService.signReviewer(id, req.getPassword(), req.getItemId());
        return R.ok();
    }

    @PostMapping("/{id}/sign-approver")
    @PreAuthorize("hasAuthority('fia.sign.approver')")
    public R<Void> signApprover(@PathVariable String id, @RequestBody SignRequest req) {
        fiaTaskService.signApprover(id, req.getPassword());
        return R.ok();
    }

    /** 不合格处理路径:退货/返工/让步接收;让步接收自动发起审批单 */
    @PostMapping("/{id}/disposition")
    @PreAuthorize("hasAuthority('fia.task.disposition')")
    public R<Void> setDisposition(@PathVariable String id,
                                  @RequestParam String disposition,
                                  @RequestParam(required = false) String remark) {
        fiaTaskService.setDisposition(id, disposition, remark);
        return R.ok();
    }

    @GetMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<FiaArchivedReport> getArchive(@PathVariable String id) {
        return R.ok(fiaTaskService.getArchive(id));
    }

    @GetMapping("/archives")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<List<Map<String, Object>>> listArchives() {
        return R.ok(fiaTaskService.listArchives());
    }

    @GetMapping("/{id}/log")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<List<Map<String, Object>>> getTaskLog(@PathVariable String id) {
        return R.ok(fiaTaskService.getTaskLog(id));
    }

    /** 来料批次→自动拆分检验任务:按检验计划匹配标准+AQL,批量建单 */
    @PostMapping("/batch-by-lot")
    @PreAuthorize("hasAuthority('fia.task.create')")
    public R<Map<String, Object>> batchCreateByLot(@RequestBody Map<String, String> body) {
        String lotNo = body.get("lotNo");
        String orgId = body.get("orgId");
        if (lotNo == null || lotNo.isBlank()) return R.fail(400, "lotNo 不能为空");

        SqmIncomingLot lot = sqmIncomingLotMapper.selectOne(
                new LambdaQueryWrapper<SqmIncomingLot>().eq(SqmIncomingLot::getLotNo, lotNo));
        if (lot == null) return R.fail(404, "批次不存在: " + lotNo);

        // 按物料分类查检验计划(优先 物料编码 + 供应商,回退仅物料编码)
        String partNo = lot.getPartNo();
        List<FiaInspPlan> plans = fiaInspPlanMapper.selectList(
                new LambdaQueryWrapper<FiaInspPlan>()
                        .eq(FiaInspPlan::getMaterialCategory, partNo)
                        .eq(FiaInspPlan::getIsActive, true)
                        .eq(lot.getSupplierId() != null, FiaInspPlan::getSupplierId, lot.getSupplierId()));
        if (plans.isEmpty()) {
            // 没匹配到→用 partNo 作为分类再试(忽略供应商)
            plans = fiaInspPlanMapper.selectList(
                    new LambdaQueryWrapper<FiaInspPlan>()
                            .like(FiaInspPlan::getMaterialCategory, partNo)
                            .eq(FiaInspPlan::getIsActive, true));
        }
        if (plans.isEmpty()) {
            // 兜底:通用默认检验计划(保证任何来料都能建单,实现全量覆盖)
            FiaInspPlan def = fiaInspPlanMapper.selectOne(
                    new LambdaQueryWrapper<FiaInspPlan>()
                            .eq(FiaInspPlan::getIsActive, true)
                            .eq(FiaInspPlan::getIsDefault, true)
                            .last("LIMIT 1"));
            if (def != null) {
                plans.add(def);
            }
        }

        List<Map<String, Object>> created = new ArrayList<>();
        int matched = 0, missing = 0;

        for (FiaInspPlan plan : plans) {
            FiaTask task = new FiaTask();
            task.setOrgId(orgId != null ? orgId : lot.getOrgId());
            task.setWoNo(lot.getLotNo());
            task.setLineName("来料检验");
            task.setProductName(lot.getPartName() != null ? lot.getPartName() : lot.getPartNo());
            task.setProcName(plan.getProcName());
            task.setTriggerType("来料入库");
            task.setStdId(plan.getStdId());
            task.setPartNo(lot.getPartNo());
            task.setSupplierId(lot.getSupplierId());
            task.setLotId(lot.getId());
            task.setBatchNo(lot.getLotNo());
            task.setRemark("IQC自动生成: " + plan.getPlanName());

            // AQL 抽样
            if (lot.getQty() != null && plan.getAql() != null) {
                AqlSamplingUtil.SamplePlan sp = AqlSamplingUtil.calc(lot.getQty().intValue(), plan.getAql());
                task.setAql(plan.getAql().toPlainString());
                task.setSampleSize(sp.sampleSize);
            }

            try {
                FiaTask createdTask = fiaTaskService.create(task);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("taskId", createdTask.getId());
                m.put("code", createdTask.getCode());
                m.put("procName", plan.getProcName());
                m.put("stdId", plan.getStdId());
                m.put("sampleSize", task.getSampleSize());
                created.add(m);
                matched++;
            } catch (Exception e) {
                missing++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lotNo", lotNo);
        result.put("partNo", partNo);
        result.put("plansFound", plans.size());
        result.put("tasksCreated", matched);
        result.put("tasksFailed", missing);
        result.put("tasks", created);
        return R.ok(result);
    }
}
