package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SPC 采集任务服务实现。
 * 负责采集任务 CRUD、缺失告警,以及"子组录入 -> 回写采集任务"的自动闭环与三类通知。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpcCollectTaskServiceImpl implements SpcCollectTaskService {

    private final SpcCollectTaskMapper spcCollectTaskMapper;
    private final SpcParamMapper spcParamMapper;
    private final JdbcTemplate jdbcTemplate;

    /** 临期提醒提前量(分钟)。 */
    private static final int DUE_SOON_WINDOW_MIN = 30;

    @Override
    public List<SpcCollectTask> list() {
        return spcCollectTaskMapper.selectList(
                Wrappers.lambdaQuery(SpcCollectTask.class)
                        .orderByDesc(SpcCollectTask::getNextDueAt));
    }

    @Override
    @Transactional
    public SpcCollectTask create(SpcCollectTask task) {
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("待采集");
        }
        if (task.getIsPlannedDowntime() == null) {
            task.setIsPlannedDowntime(false);
        }
        if (task.getDueReminded() == null) {
            task.setDueReminded(false);
        }
        // 若未给下次到期,默认按频率从当前顺延一天,避免 NULL 导致扫描异常
        if (task.getNextDueAt() == null) {
            task.setNextDueAt(computeNextDue(LocalDateTime.now(), task.getCollectFreq()));
        }
        spcCollectTaskMapper.insert(task);
        try {
            notifyAssigned(task);
        } catch (Exception e) {
            log.warn("[SPC采集] 任务下发通知失败(忽略): {}", e.getMessage());
        }
        return task;
    }

    @Override
    @Transactional
    public void update(SpcCollectTask task) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("更新需指定 id");
        }
        SpcCollectTask existing = spcCollectTaskMapper.selectById(task.getId());
        if (existing == null) {
            throw new IllegalArgumentException("采集任务不存在: " + task.getId());
        }
        if (task.getParamId() != null) {
            existing.setParamId(task.getParamId());
        }
        if (task.getOrgId() != null) {
            existing.setOrgId(task.getOrgId());
        }
        if (task.getCollectFreq() != null) {
            existing.setCollectFreq(task.getCollectFreq());
        }
        if (task.getCollector() != null) {
            existing.setCollector(task.getCollector());
        }
        if (task.getCollectMode() != null) {
            existing.setCollectMode(task.getCollectMode());
        }
        if (task.getNextDueAt() != null) {
            existing.setNextDueAt(task.getNextDueAt());
            existing.setDueReminded(false);
        }
        if (task.getStatus() != null) {
            existing.setStatus(task.getStatus());
        }
        if (task.getIsPlannedDowntime() != null) {
            existing.setIsPlannedDowntime(task.getIsPlannedDowntime());
        }
        spcCollectTaskMapper.updateById(existing);
    }

    @Override
    public void delete(String id) {
        spcCollectTaskMapper.deleteById(id); // BaseEntity @TableLogic -> 软删
    }

    @Override
    @Transactional
    public void markDowntime(String id, Boolean isPlannedDowntime, String reason) {
        SpcCollectTask task = spcCollectTaskMapper.selectById(id);
        if (task == null) {
            throw new IllegalArgumentException("采集任务不存在: " + id);
        }
        task.setIsPlannedDowntime(isPlannedDowntime);
        spcCollectTaskMapper.updateById(task);
    }

    @Override
    @Transactional
    public void markMissing(String id, String reason) {
        SpcCollectTask task = spcCollectTaskMapper.selectById(id);
        if (task == null) {
            throw new IllegalArgumentException("采集任务不存在: " + id);
        }
        if (task.getIsPlannedDowntime() != null && task.getIsPlannedDowntime()) {
            throw new IllegalStateException("计划停产期间不标记缺失");
        }
        if ("缺失".equals(task.getStatus())) {
            return; // 幂等:已缺失不再重复告警
        }
        task.setStatus("缺失");
        spcCollectTaskMapper.updateById(task);
        notifyMissing(task, reason);
    }

    @Override
    @Transactional
    public int scanOverdueMissing() {
        LocalDateTime now = LocalDateTime.now();
        // 覆盖"待采集"与"已采集(进入新周期)"两类过期任务
        List<SpcCollectTask> overdue = spcCollectTaskMapper.selectList(
                Wrappers.lambdaQuery(SpcCollectTask.class)
                        .lt(SpcCollectTask::getNextDueAt, now)
                        .in(SpcCollectTask::getStatus, "待采集", "已采集")
                        .eq(SpcCollectTask::getIsPlannedDowntime, false));
        int n = 0;
        for (SpcCollectTask t : overdue) {
            try {
                if ("已采集".equals(t.getStatus())) {
                    // 进入新周期:先复位为待采集,再标记缺失
                    t.setStatus("待采集");
                    t.setDueReminded(false);
                    spcCollectTaskMapper.updateById(t);
                }
                markMissing(t.getId(), "定时扫描:采集到期未录入");
                n++;
            } catch (Exception e) {
                log.warn("[SPC采集] 标记缺失失败 taskId={}: {}", t.getId(), e.getMessage());
            }
        }
        if (n > 0) {
            log.info("[SPC采集] 逾期缺失扫描处理 {} 条", n);
        }
        return n;
    }

    @Override
    @Transactional
    public void recordCollected(String paramId, String orgId, BigDecimal lastValue, LocalDateTime lastAt) {
        if (paramId == null || orgId == null || lastAt == null) {
            return;
        }
        List<SpcCollectTask> tasks = spcCollectTaskMapper.selectList(
                Wrappers.lambdaQuery(SpcCollectTask.class)
                        .eq(SpcCollectTask::getParamId, paramId)
                        .eq(SpcCollectTask::getOrgId, orgId));
        for (SpcCollectTask t : tasks) {
            t.setLastValue(lastValue);
            t.setLastAt(lastAt);
            t.setStatus("已采集");
            t.setNextDueAt(computeNextDue(lastAt, t.getCollectFreq()));
            t.setDueReminded(false);
            spcCollectTaskMapper.updateById(t);
            try {
                notifyCollected(t);
            } catch (Exception e) {
                log.warn("[SPC采集] 录入回执通知失败 taskId={}: {}", t.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public int scanDueSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime window = now.plusMinutes(DUE_SOON_WINDOW_MIN);
        List<SpcCollectTask> due = spcCollectTaskMapper.selectList(
                Wrappers.lambdaQuery(SpcCollectTask.class)
                        .eq(SpcCollectTask::getStatus, "待采集")
                        .eq(SpcCollectTask::getIsPlannedDowntime, false)
                        .eq(SpcCollectTask::getDueReminded, false)
                        .le(SpcCollectTask::getNextDueAt, window)
                        .ge(SpcCollectTask::getNextDueAt, now));
        int n = 0;
        for (SpcCollectTask t : due) {
            try {
                t.setDueReminded(true);
                spcCollectTaskMapper.updateById(t);
                notifyDueSoon(t);
                n++;
            } catch (Exception e) {
                log.warn("[SPC采集] 临期提醒失败 taskId={}: {}", t.getId(), e.getMessage());
            }
        }
        if (n > 0) {
            log.info("[SPC采集] 临期提醒 {} 条", n);
        }
        return n;
    }

    // ====================== 定时扫描(每 60s) ======================

    @Scheduled(fixedDelay = 60000)
    public void scheduledScanOverdue() {
        try {
            scanOverdueMissing();
        } catch (Exception e) {
            log.warn("[SPC采集] 逾期扫描异常: {}", e.getMessage());
        }
        try {
            scanDueSoon();
        } catch (Exception e) {
            log.warn("[SPC采集] 临期扫描异常: {}", e.getMessage());
        }
    }

    // ====================== 通知 ======================

    private void notifyMissing(SpcCollectTask task, String reason) {
        String paramName = resolveParamName(task.getParamId());
        String content = String.format(
                "SPC 采集缺失告警:参数 %s,应采集时间 %s 已到期未录入(%s)。请班组长安排补录。",
                paramName, task.getNextDueAt(), reason);
        jdbcTemplate.update(
                "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) "
                        + "VALUES (?, 'SPC_COLLECT_MISSING', ?, '站内', ?, ?, '告警', '已发送', now())",
                java.util.UUID.fromString(task.getOrgId()),
                task.getId(),
                resolveReceiver(task),
                content);
    }

    private void notifyAssigned(SpcCollectTask task) {
        String paramName = resolveParamName(task.getParamId());
        String content = String.format(
                "SPC 采集任务已下发:参数 %s,采集频率 %s。请按时采集并在到期前录入。",
                paramName, task.getCollectFreq() != null ? task.getCollectFreq() : "-");
        jdbcTemplate.update(
                "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) "
                        + "VALUES (?, 'SPC_COLLECT_ASSIGNED', ?, '站内', ?, ?, '提示', '已发送', now())",
                java.util.UUID.fromString(task.getOrgId()),
                task.getId(),
                resolveReceiver(task),
                content);
    }

    private void notifyDueSoon(SpcCollectTask task) {
        String paramName = resolveParamName(task.getParamId());
        String content = String.format(
                "SPC 采集临期提醒:参数 %s 将于 %s 到期,请尽快录入。",
                paramName, task.getNextDueAt() != null ? task.getNextDueAt().toString() : "-");
        jdbcTemplate.update(
                "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) "
                        + "VALUES (?, 'SPC_COLLECT_DUE_SOON', ?, '站内', ?, ?, '告警', '已发送', now())",
                java.util.UUID.fromString(task.getOrgId()),
                task.getId(),
                resolveReceiver(task),
                content);
    }

    private void notifyCollected(SpcCollectTask task) {
        String paramName = resolveParamName(task.getParamId());
        String content = String.format(
                "SPC 采集完成回执:参数 %s 已于 %s 录入(值 %s),下次到期 %s。",
                paramName,
                task.getLastAt() != null ? task.getLastAt().toString() : "-",
                task.getLastValue() != null ? task.getLastValue().toString() : "-",
                task.getNextDueAt() != null ? task.getNextDueAt().toString() : "-");
        jdbcTemplate.update(
                "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) "
                        + "VALUES (?, 'SPC_COLLECT_DONE', ?, '站内', ?, ?, '提示', '已发送', now())",
                java.util.UUID.fromString(task.getOrgId()),
                task.getId(),
                resolveReceiver(task),
                content);
    }

    private String resolveReceiver(SpcCollectTask task) {
        if (task.getCollector() != null && !task.getCollector().isBlank()) {
            return task.getCollector();
        }
        return "班组长";
    }

    private String resolveParamName(String paramId) {
        if (paramId == null) {
            return "-";
        }
        SpcParam p = spcParamMapper.selectById(paramId);
        return (p != null && p.getParamName() != null) ? p.getParamName() : paramId;
    }

    // ====================== 频率解析 ======================

    /**
     * 根据自由文本采集频率,基于基准时间计算下次到期时间。
     * 支持:每日/每天/每N天、每班次(+8h)、每N小时、每N分钟、1次/30min 等;解析不到默认 +1 天。
     */
    private LocalDateTime computeNextDue(LocalDateTime base, String freq) {
        if (base == null) {
            base = LocalDateTime.now();
        }
        if (freq == null || freq.isBlank()) {
            return base.plusDays(1);
        }
        String f = freq.trim();
        String lower = f.toLowerCase();
        if (f.contains("班次") || f.contains("班")) {
            return base.plusHours(8);
        }
        if (f.contains("周") || lower.contains("week")) {
            return base.plusDays(7);
        }
        if (f.contains("分钟") || lower.contains("min")) {
            return base.plusMinutes(parseLeadingNum(f, 30));
        }
        if (f.contains("小时") || lower.contains("hour")) {
            return base.plusHours(parseLeadingNum(f, 1));
        }
        if (f.contains("天") || f.contains("日") || lower.contains("day")) {
            return base.plusDays(parseLeadingNum(f, 1));
        }
        return base.plusDays(1);
    }

    private int parseLeadingNum(String s, int def) {
        Matcher m = Pattern.compile("(\\d+)").matcher(s);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                return def;
            }
        }
        return def;
    }
}
