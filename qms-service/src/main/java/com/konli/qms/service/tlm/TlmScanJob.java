package com.konli.qms.service.tlm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.tlm.entity.TlmMaintPlan;
import com.konli.qms.domain.tlm.entity.TlmTooling;
import com.konli.qms.domain.tlm.mapper.TlmMaintPlanMapper;
import com.konli.qms.domain.tlm.mapper.TlmToolingMapper;
import com.konli.qms.service.notify.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 工装计量/保养/寿命预警扫描。
 * 每天 8:00 扫描, 对校准到期/保养到期/寿命超限的工装,
 * 经 NotificationService.notify(module=tlm, eventCode=...) 按 notify_config 配置推送(强约束 C2)。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TlmScanJob {

    private final TlmToolingMapper toolingMapper;
    private final TlmMaintPlanMapper maintPlanMapper;
    private final NotificationService notificationService;

    /** 每天 8:00 扫描工装预警。 */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scanToolingMaintenance() {
        try {
            LocalDate today = LocalDate.now();
            // 1) 校准到期(30/60/90 天前预警)
            for (int days : new int[]{30, 60, 90}) {
                LocalDate target = today.plusDays(days);
                List<TlmTooling> list = toolingMapper.selectList(
                        new LambdaQueryWrapper<TlmTooling>()
                                .eq(TlmTooling::getToolCategory, "GAUGE")
                                .eq(TlmTooling::getCalibDueDate, target)
                                .ne(TlmTooling::getStatus, "SCRAPPED"));
                for (TlmTooling t : list) {
                    insertLog(t, "tlm_calib_expiry",
                            "工装(测量设备) " + t.getToolName() + "(" + t.getToolNo() + ") 将于" + days + "天后校准到期");
                }
            }
            // 2) 保养到期(7/30 天前预警)
            for (int days : new int[]{7, 30}) {
                LocalDate target = today.plusDays(days);
                List<TlmTooling> list = toolingMapper.selectList(
                        new LambdaQueryWrapper<TlmTooling>()
                                .eq(TlmTooling::getNextMaintDate, target)
                                .ne(TlmTooling::getStatus, "SCRAPPED"));
                for (TlmTooling t : list) {
                    insertLog(t, "tlm_maint_due",
                            "工装 " + t.getToolName() + "(" + t.getToolNo() + ") 将于" + days + "天后保养到期");
                }
            }
            // 3) 寿命超限(已锁定的工装)
            List<TlmTooling> overLife = toolingMapper.selectList(
                    new LambdaQueryWrapper<TlmTooling>()
                            .isNotNull(TlmTooling::getDesignLife)
                            .apply("bind_count >= design_life")
                            .eq(TlmTooling::getLocked, true));
            for (TlmTooling t : overLife) {
                insertLog(t, "tlm_life_over",
                        "工装 " + t.getToolName() + "(" + t.getToolNo() + ") 已达寿命上限,已锁定");
            }
            // 4) 保养计划滚动: 到期(next_date<=today)的计划自动生成下一期, 避免人工重复建
            rollMaintPlans();
        } catch (Exception e) {
            log.warn("工装预警扫描异常: {}", e.getMessage());
        }
    }

    /** 保养计划到期自动滚动: 对 next_date<=today 的计划生成下一期(按 cycle_type 推进), 原计划保留。 */
    private void rollMaintPlans() {
        try {
            List<TlmMaintPlan> due = maintPlanMapper.selectList(
                    new LambdaQueryWrapper<TlmMaintPlan>().le(TlmMaintPlan::getNextDate, LocalDate.now()));
            for (TlmMaintPlan p : due) {
                if (p.getCycleType() == null) continue;
                LocalDate next = p.getNextDate();
                if (next == null) continue;
                LocalDate newNext = switch (p.getCycleType()) {
                    case "WEEK" -> next.plusWeeks(1);
                    case "MONTH" -> next.plusMonths(1);
                    case "YEAR" -> next.plusYears(1);
                    default -> null;
                };
                if (newNext == null) continue;
                // 若该 tool 已存在 next_date==newNext 的计划(防重复滚动), 跳过
                Long exist = maintPlanMapper.selectCount(new LambdaQueryWrapper<TlmMaintPlan>()
                        .eq(TlmMaintPlan::getToolId, p.getToolId())
                        .eq(TlmMaintPlan::getNextDate, newNext));
                if (exist != null && exist > 0) continue;
                TlmMaintPlan np = new TlmMaintPlan();
                np.setOrgId(p.getOrgId());
                np.setToolId(p.getToolId());
                np.setPlanNo("TLM-MP-" + System.currentTimeMillis());
                np.setCycleType(p.getCycleType());
                np.setNextDate(newNext);
                np.setResponsibleId(p.getResponsibleId());
                np.setRemark(p.getRemark());
                maintPlanMapper.insert(np);
                log.info("[TLM] 保养计划 {} 已自动滚动至 {}", p.getPlanNo(), newNext);
            }
        } catch (Exception e) {
            log.warn("保养计划滚动异常: {}", e.getMessage());
        }
    }

    private void insertLog(TlmTooling t, String bizType, String content) {
        try {
            // 统一走通知配置(module=tlm, eventCode=bizType)解析接收人/渠道(强约束 C2),
            // 取代直接写 notification_log 裸表; 配置缺失时静默不发, 不影响扫描主流程。
            notificationService.notify("tlm", bizType, "工装预警", content, bizType, t.getId(), "/tlm/tooling/" + t.getId());
        } catch (Exception e) {
            log.warn("工装预警通知失败: {}", e.getMessage());
        }
    }
}
