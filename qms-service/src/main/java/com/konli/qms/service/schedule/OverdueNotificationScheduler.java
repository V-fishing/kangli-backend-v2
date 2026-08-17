package com.konli.qms.service.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.fia.mapper.FiaTaskMapper;
import com.konli.qms.domain.ncm.entity.NcmCorrectiveAction;
import com.konli.qms.domain.ncm.mapper.NcmCorrectiveActionMapper;
import com.konli.qms.domain.patrol.entity.PatlTask;
import com.konli.qms.domain.patrol.mapper.PatlTaskMapper;
import com.konli.qms.domain.sqm.entity.SqmAuditNc;
import com.konli.qms.domain.sqm.mapper.SqmAuditNcMapper;
import com.konli.qms.service.notify.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 统一超期扫描定时任务:每天 8:15 扫描巡检/FIA/审核NC/CAPA 超期并推送通知。
 * 各模块独立的扫描(@Scheduled 8:00/8:05/8:10)仍然保留兼容,此处作为兜底统一覆盖。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OverdueNotificationScheduler {

    private final PatlTaskMapper patlTaskMapper;
    private final FiaTaskMapper fiaTaskMapper;
    private final SqmAuditNcMapper sqmAuditNcMapper;
    private final NcmCorrectiveActionMapper ncmCorrectiveActionMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 15 8 * * ?") // 每天 8:15
    public void scanAll() {
        try { scanPatrolOverdue(); } catch (Exception e) { log.warn("[超期扫描] 巡检扫描异常: {}", e.getMessage()); }
        try { scanFiaOverdue(); } catch (Exception e) { log.warn("[超期扫描] FIA扫描异常: {}", e.getMessage()); }
        try { scanAuditNcOverdue(); } catch (Exception e) { log.warn("[超期扫描] 审核NC扫描异常: {}", e.getMessage()); }
        try { scanCapaOverdue(); } catch (Exception e) { log.warn("[超期扫描] CAPA扫描异常: {}", e.getMessage()); }
    }

    /** 巡检超期:计划时间已过且未完成 */
    private void scanPatrolOverdue() {
        List<PatlTask> overdue = patlTaskMapper.selectList(
                new LambdaQueryWrapper<PatlTask>()
                        .lt(PatlTask::getPlanTime, LocalDateTime.now())
                        .ne(PatlTask::getStatus, "已完成"));
        for (PatlTask t : overdue) {
            if (t.getPlanTime() == null) continue;
            long days = ChronoUnit.DAYS.between(t.getPlanTime().toLocalDate(), LocalDate.now());
            if (days < 1) continue; // 当天不过期报警
            notificationService.notify("schedule", "patrol_overdue",
                    "巡检任务超期",
                    "巡检任务" + t.getTaskNo() + " 已超期" + days + "天未完成,请尽快处理。",
                    "patrol_overdue", t.getId(), t.getTaskNo(), "/patrol/tasks", t.getOrgId());
        }
    }

    /** FIA超期:SLA到期未完成 */
    private void scanFiaOverdue() {
        List<FiaTask> overdue = fiaTaskMapper.selectList(
                new LambdaQueryWrapper<FiaTask>()
                        .lt(FiaTask::getSlaDueAt, LocalDateTime.now())
                        .notIn(FiaTask::getStatus, "已完成", "超时", "已作废"));
        for (FiaTask t : overdue) {
            if (t.getSlaDueAt() == null) continue;
            long days = ChronoUnit.DAYS.between(t.getSlaDueAt().toLocalDate(), LocalDate.now());
            if (days < 1) continue;
            notificationService.notify("schedule", "fia_overdue",
                    "首件检验超期提醒",
                    "校验单" + t.getCode() + "(工单" + t.getWoNo() + ") 已超期" + days + "天,请及时处理。",
                    "fia_overdue", t.getId(), t.getCode(), "/fia/tasks", t.getOrgId());
        }
    }

    /** 审核NC超期:整改deadline已过未闭环 */
    private void scanAuditNcOverdue() {
        List<SqmAuditNc> overdue = sqmAuditNcMapper.selectList(
                new LambdaQueryWrapper<SqmAuditNc>()
                        .lt(SqmAuditNc::getDeadline, LocalDate.now())
                        .notIn(SqmAuditNc::getStatus, "已关闭", "已闭环"));
        for (SqmAuditNc nc : overdue) {
            if (nc.getDeadline() == null) continue;
            long days = ChronoUnit.DAYS.between(nc.getDeadline(), LocalDate.now());
            if (days < 1) continue;
            notificationService.notify("schedule", "audit_nc_overdue",
                    "审核NC整改超期",
                    "审核不符合项" + nc.getNcNo() + " 整改超期" + days + "天,请尽快推进。",
                    "audit_nc_overdue", nc.getId(), nc.getNcNo(), "/sqm/audit", nc.getOrgId());
        }
    }

    /** CAPA超期:纠正措施dueDate已过未完成/未关闭 */
    private void scanCapaOverdue() {
        List<NcmCorrectiveAction> overdue = ncmCorrectiveActionMapper.selectList(
                new LambdaQueryWrapper<NcmCorrectiveAction>()
                        .lt(NcmCorrectiveAction::getDueDate, LocalDate.now())
                        .notIn(NcmCorrectiveAction::getStatus, "已关闭", "已完成"));
        for (NcmCorrectiveAction ca : overdue) {
            if (ca.getDueDate() == null) continue;
            long days = ChronoUnit.DAYS.between(ca.getDueDate(), LocalDate.now());
            if (days < 1) continue;
            notificationService.notify("schedule", "capa_overdue",
                    "纠正措施超期",
                    "纠正措施" + ca.getCaNo() + "(" + (ca.getIssue() != null ? ca.getIssue() : "") + ") 超期" + days + "天,请尽快处理。",
                    "capa_overdue", ca.getId(), ca.getCaNo(), "/ncm/capa", ca.getOrgId());
        }
    }
}
