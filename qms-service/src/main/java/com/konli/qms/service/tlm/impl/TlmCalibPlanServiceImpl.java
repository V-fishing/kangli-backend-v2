package com.konli.qms.service.tlm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.tlm.entity.TlmCalibPlan;
import com.konli.qms.domain.tlm.entity.TlmTooling;
import com.konli.qms.domain.tlm.mapper.TlmCalibPlanMapper;
import com.konli.qms.domain.tlm.mapper.TlmToolingMapper;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.tlm.TlmCalibPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** 计量校准计划单服务实现(P1)。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TlmCalibPlanServiceImpl implements TlmCalibPlanService {

    private final TlmCalibPlanMapper calibPlanMapper;
    private final TlmToolingMapper toolingMapper;
    private final NotificationService notificationService;

    @Override
    public PageResult<TlmCalibPlan> page(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<TlmCalibPlan> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(TlmCalibPlan::getToolNo, keyword).or().like(TlmCalibPlan::getToolName, keyword));
        }
        if (status != null && !status.isBlank()) {
            w.eq(TlmCalibPlan::getStatus, status);
        }
        w.orderByDesc(TlmCalibPlan::getPlanDueDate);
        IPage<TlmCalibPlan> ip = calibPlanMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), page, size);
    }

    @Override
    public void autoGenerate(int leadDays) {
        if (leadDays <= 0) leadDays = 30;
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(leadDays);
        // 取校准到期落在 [from, to] 且未报废的 GAUGE
        List<TlmTooling> due = toolingMapper.selectList(
                new LambdaQueryWrapper<TlmTooling>()
                        .eq(TlmTooling::getToolCategory, "GAUGE")
                        .between(TlmTooling::getCalibDueDate, from, to)
                        .ne(TlmTooling::getStatus, "SCRAPPED"));
        for (TlmTooling t : due) {
            // 去重: 已存在覆盖该器具且 plan_due_date 相同的待执行计划则跳过
            Long exist = calibPlanMapper.selectCount(new LambdaQueryWrapper<TlmCalibPlan>()
                    .eq(TlmCalibPlan::getToolId, t.getId())
                    .eq(TlmCalibPlan::getPlanDueDate, t.getCalibDueDate())
                    .eq(TlmCalibPlan::getStatus, "PENDING"));
            if (exist != null && exist > 0) continue;

            TlmCalibPlan plan = new TlmCalibPlan();
            plan.setOrgId(t.getOrgId());
            plan.setToolId(t.getId());
            plan.setToolNo(t.getToolNo());
            plan.setToolName(t.getToolName());
            plan.setPlanCycle(t.getCalibCycle());
            plan.setPlanDueDate(t.getCalibDueDate());
            plan.setStatus("PENDING");
            plan.setOwnerId(t.getAdminId());
            plan.setSource("AUTO");
            calibPlanMapper.insert(plan);
            log.info("[TLM] 校准计划单已自动生成 器具 {} 到期 {}", t.getToolNo(), t.getCalibDueDate());

            try {
                String content = "计量器具 " + t.getToolName() + "(" + t.getToolNo() + ") 将于 "
                        + t.getCalibDueDate() + " 校准到期, 请安排校准。";
                notificationService.notify("tlm", "tlm_calib_plan_created", "计量校准计划", content,
                        "tlm_calib_plan_created", plan.getId(), "/tlm/metro/" + t.getId());
            } catch (Exception e) {
                log.warn("校准计划通知失败: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordResult(String planId, LocalDate calibDate, LocalDate calibDueDate,
                             Integer calibCycle, String upperLimit, String result, String remark, String certNo) {
        TlmCalibPlan plan = calibPlanMapper.selectById(planId);
        if (plan == null) {
            throw new com.konli.qms.common.exception.BusinessException("校准计划单不存在");
        }
        plan.setStatus("DONE");
        // 校准结果明细回填计划单, 长期留存计量履历(供追溯反查)
        plan.setCalibNo(plan.getToolNo() + "-C" + System.currentTimeMillis());
        plan.setCalibDate(calibDate);
        plan.setCalibDueDate(calibDueDate);
        plan.setCalibCycle(calibCycle);
        plan.setUpperLimit(upperLimit);
        plan.setResult(result);
        plan.setRemark(remark);
        plan.setCertNo(certNo);
        calibPlanMapper.updateById(plan);

        // 回写器具校准日期/到期: 已传 calibDueDate 优先, 否则按 calibDate + 周期推算
        TlmTooling t = toolingMapper.selectById(plan.getToolId());
        if (t != null) {
            LocalDate start = calibDate != null ? calibDate : LocalDate.now();
            LocalDate due = calibDueDate;
            if (due == null && plan.getPlanCycle() != null) {
                due = start.plusMonths(plan.getPlanCycle());
            }
            t.setCalibDate(start);
            t.setCalibDueDate(due);
            if (calibCycle != null) t.setCalibCycle(calibCycle);
            t.setLocked(Boolean.FALSE); // 校准合格解锁
            toolingMapper.updateById(t);
        }
    }

    @Override
    public TlmCalibPlan createManual(String toolId, Integer planCycle, LocalDate planDueDate) {
        TlmTooling t = toolingMapper.selectById(toolId);
        if (t == null) throw new com.konli.qms.common.exception.BusinessException("计量器具不存在");
        if (!"GAUGE".equals(t.getToolCategory())) {
            throw new com.konli.qms.common.exception.BusinessException("仅计量器具(GAUGE)可建校准计划");
        }
        TlmCalibPlan plan = new TlmCalibPlan();
        plan.setOrgId(t.getOrgId());
        plan.setToolId(t.getId());
        plan.setToolNo(t.getToolNo());
        plan.setToolName(t.getToolName());
        plan.setPlanCycle(planCycle != null ? planCycle : t.getCalibCycle());
        plan.setPlanDueDate(planDueDate);
        plan.setStatus("PENDING");
        plan.setOwnerId(t.getAdminId());
        plan.setSource("MANUAL");
        calibPlanMapper.insert(plan);
        return plan;
    }
}
