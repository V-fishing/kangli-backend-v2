package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcCollectTask;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.mapper.SpcCollectTaskMapper;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.service.spc.SpcCollectTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpcCollectTaskServiceImpl implements SpcCollectTaskService {

    private final SpcCollectTaskMapper spcCollectTaskMapper;
    private final SpcParamMapper spcParamMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SpcCollectTask> list() {
        return spcCollectTaskMapper.selectList(null);
    }

    @Override
    @Transactional
    public SpcCollectTask create(SpcCollectTask task) {
        if (task.getIsPlannedDowntime() == null) {
            task.setIsPlannedDowntime(false);
        }
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("待采集");
        }
        spcCollectTaskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public void markDowntime(String id, Boolean isPlannedDowntime, String reason) {
        SpcCollectTask task = spcCollectTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(400, "采集任务不存在");
        }
        task.setIsPlannedDowntime(isPlannedDowntime);
        // reason 当前表无独立字段,接口保留以便后续扩展(代码规范§3.2 备注列)。
        spcCollectTaskMapper.updateById(task);
    }

    /**
     * SR-SPC-003:标记采集缺失 -> status='缺失' + 通知班组长。
     * 计划停产(isPlannedDowntime=true)不触发告警,仅提示用户。
     */
    @Override
    @Transactional
    public void markMissing(String id, String reason) {
        SpcCollectTask task = spcCollectTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(400, "采集任务不存在");
        }
        if (Boolean.TRUE.equals(task.getIsPlannedDowntime())) {
            // 计划停产期间不告警(SR-SPC-003 异常分支)
            throw new BusinessException(400, "该采集任务已标记计划停产,停产期间不触发缺失告警");
        }
        if ("缺失".equals(task.getStatus())) {
            return; // 幂等:已标记缺失
        }
        task.setStatus("缺失");
        spcCollectTaskMapper.updateById(task);
        // 写采集缺失告警通知给班组长
        notifyMissing(task, reason);
        log.info("[SPC缺失] 采集任务 {} 标记缺失,已告警班组长(param={})", id, task.getParamId());
    }

    /**
     * SR-SPC-003:扫描到期未录入且未停产的采集任务 -> 自动标记缺失并告警。
     * 条件:nextDueAt < now AND status='待采集' AND isPlannedDowntime=false。
     */
    @Override
    @Transactional
    public int scanOverdueMissing() {
        List<SpcCollectTask> overdue = spcCollectTaskMapper.selectList(
                new LambdaQueryWrapper<SpcCollectTask>()
                        .lt(SpcCollectTask::getNextDueAt, LocalDateTime.now())
                        .eq(SpcCollectTask::getStatus, "待采集")
                        .eq(SpcCollectTask::getIsPlannedDowntime, false));
        int n = 0;
        for (SpcCollectTask t : overdue) {
            try {
                markMissing(t.getId(), "定时扫描:采集到期未录入");
                n++;
            } catch (Exception e) {
                // 单条失败不影响其它
            }
        }
        if (n > 0) {
            log.info("[SPC缺失] 定时扫描标记缺失 {} 条", n);
        }
        return n;
    }

    /** 定时扫描:每 60 秒检查一次到期未录入的采集任务(SR-SPC-003)。 */
    @Scheduled(fixedDelay = 60_000)
    public void scheduledScanOverdue() {
        try {
            scanOverdueMissing();
        } catch (Exception e) {
            log.warn("[SPC缺失] 定时扫描异常: {}", e.getMessage());
        }
    }

    /** 写采集缺失告警到 notification_log(站内通知班组长)。 */
    private void notifyMissing(SpcCollectTask task, String reason) {
        String paramName = task.getParamId();
        SpcParam param = spcParamMapper.selectById(task.getParamId());
        if (param != null) {
            paramName = param.getParamName();
        }
        String content = String.format("SPC 采集缺失告警:参数 %s,应采集时间 %s 已到期未录入%s。请班组长安排补录。",
                paramName,
                task.getNextDueAt() != null ? task.getNextDueAt().toString() : "-",
                reason != null && !reason.isBlank() ? "(" + reason + ")" : "");
        jdbcTemplate.update(
                "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) " +
                        "VALUES (?, 'SPC_COLLECT_MISSING', ?, '站内', ?, ?, '告警', '已发送', now())",
                java.util.UUID.fromString(task.getOrgId()), task.getId(),
                "班组长", content);
    }
}
