package com.konli.qms.api.fia.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.api.fia.dto.InspItemResultRequest;
import com.konli.qms.api.fia.dto.SignRequest;
import com.konli.qms.common.api.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import com.konli.qms.domain.fia.entity.FiaInspItem;
import com.konli.qms.domain.fia.entity.FiaInspPlan;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.fia.mapper.FiaInspPlanMapper;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.mapper.SqmIncomingLotMapper;
import com.konli.qms.service.fia.AqlSamplingUtil;
import com.konli.qms.service.fia.FiaTaskService;
import com.konli.qms.service.fia.dto.FiaTaskVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 供应商来料首件检验 — 独立子域。
 *
 * 与产线首件(/api/v1/fia/tasks)的区别:
 *   - 触发: 来料批次入库 (SqmIncomingLot), 非产线换模/换设备
 *   - disposition 枚举: 合格入库 / 退货 / 让步接收 / 挑选 (不允许"拦截/放行")
 *   - 不触发工单锁定(FiaWoLock)
 *   - AQL 抽样计算
 *
 * source = SUPPLIER, 自动设置, 不可变。
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/fia/incoming-checks")
@RequiredArgsConstructor
public class FiaIncomingCheckController {

    private final FiaTaskService fiaTaskService;
    private final FiaInspPlanMapper fiaInspPlanMapper;
    private final SqmIncomingLotMapper sqmIncomingLotMapper;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    // ── 来料首件 disposition 白名单 ──
    private static final List<String> VALID_DISPOSITIONS = List.of("合格入库", "退货", "让步接收", "挑选");

    /** 看板: 来料首件专属统计 (source=SUPPLIER) */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        // 按状态计数
        List<Map<String, Object>> statusCounts = jdbcTemplate.queryForList(
            "SELECT status, count(*) as cnt FROM ops.fia_task WHERE source='SUPPLIER' AND is_deleted=false GROUP BY status");
        data.put("statusCounts", statusCounts);
        // 今日汇总
        Map<String, Object> today = jdbcTemplate.queryForMap(
            "SELECT count(*) as total, "
            + "count(*) FILTER (WHERE status='已完成') as completed, "
            + "count(*) FILTER (WHERE is_overdue=true) as overdue "
            + "FROM ops.fia_task WHERE source='SUPPLIER' AND is_deleted=false AND created_at::date = CURRENT_DATE");
        data.put("today", today);
        // 来料批次覆盖率
        Long lotCoverage = jdbcTemplate.queryForObject(
            "SELECT count(DISTINCT lot_id) FROM ops.fia_task WHERE source='SUPPLIER' AND is_deleted=false", Long.class);
        data.put("lotCoverage", lotCoverage != null ? lotCoverage : 0);
        return R.ok(data);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<List<FiaTask>> list() {
        return R.ok(fiaTaskService.listBySource("SUPPLIER"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<FiaTaskVo> get(@PathVariable String id) {
        return R.ok(fiaTaskService.get(id));
    }

    /** 来料批次驱动: 按物料+供应商+工序自动匹配检验标准 */
    @GetMapping("/match-std")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<FiaInspStd> matchStd(@RequestParam String partNo,
                                  @RequestParam(required = false) String supplierId,
                                  @RequestParam(required = false) String procName) {
        return R.ok(fiaTaskService.matchStd(null, partNo, supplierId, procName));
    }

    /** 来料批次→自动拆分检验任务: 按检验计划匹配标准+AQL, 批量建单 */
    @PostMapping("/batch-by-lot")
    @PreAuthorize("hasAuthority('fia.task.create')")
    public R<Map<String, Object>> batchCreateByLot(@RequestBody Map<String, String> body) {
        String lotNo = body.get("lotNo");
        String orgId = body.get("orgId");
        if (lotNo == null || lotNo.isBlank()) return R.fail(400, "lotNo 不能为空");

        SqmIncomingLot lot = sqmIncomingLotMapper.selectOne(
                new LambdaQueryWrapper<SqmIncomingLot>().eq(SqmIncomingLot::getLotNo, lotNo));
        if (lot == null) return R.fail(404, "批次不存在: " + lotNo);

        String partNo = lot.getPartNo();
        List<FiaInspPlan> plans = fiaInspPlanMapper.selectList(
                new LambdaQueryWrapper<FiaInspPlan>()
                        .eq(FiaInspPlan::getMaterialCategory, partNo)
                        .eq(FiaInspPlan::getIsActive, true)
                        .eq(lot.getSupplierId() != null, FiaInspPlan::getSupplierId, lot.getSupplierId()));
        if (plans.isEmpty()) {
            plans = fiaInspPlanMapper.selectList(
                    new LambdaQueryWrapper<FiaInspPlan>()
                            .like(FiaInspPlan::getMaterialCategory, partNo)
                            .eq(FiaInspPlan::getIsActive, true));
        }
        if (plans.isEmpty()) {
            FiaInspPlan def = fiaInspPlanMapper.selectOne(
                    new LambdaQueryWrapper<FiaInspPlan>()
                            .eq(FiaInspPlan::getIsActive, true)
                            .eq(FiaInspPlan::getIsDefault, true)
                            .last("LIMIT 1"));
            if (def != null) plans.add(def);
        }

        List<Map<String, Object>> created = new ArrayList<>();
        int matched = 0, missing = 0;

        for (FiaInspPlan plan : plans) {
            FiaTask task = new FiaTask();
            task.setSource("SUPPLIER");
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
            task.setRemark("供应商来料首件自动生成: " + plan.getPlanName());

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
                log.warn("来料首件批量建单失败: plan={}", plan.getPlanName(), e);
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

    /** 供应商来料处置: 合格入库 / 退货 / 让步接收 / 挑选 (不允许拦截/放行) */
    @PostMapping("/{id}/disposition")
    @PreAuthorize("hasAuthority('fia.sign.disposition')")
    public R<Void> setDisposition(@PathVariable String id,
                                  @RequestParam String disposition,
                                  @RequestParam(required = false) String remark) {
        if (!VALID_DISPOSITIONS.contains(disposition)) {
            return R.fail(400, "供应商来料处置不支持: " + disposition + "，允许: " + String.join("/", VALID_DISPOSITIONS));
        }
        fiaTaskService.setDisposition(id, disposition, remark);
        return R.ok();
    }
}
