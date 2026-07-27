package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.sqm.entity.QmsFmeaRisk;
import com.konli.qms.domain.sqm.entity.QmsFmeaRiskTrack;
import com.konli.qms.domain.sqm.mapper.QmsFmeaRiskMapper;
import com.konli.qms.domain.sqm.mapper.QmsFmeaRiskTrackMapper;
import com.konli.qms.service.sqm.SqmFmeaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqmFmeaServiceImpl implements SqmFmeaService {

    private static final List<String> TYPES = Arrays.asList("PFMEA", "DFMEA", "SFMEA");

    private final QmsFmeaRiskMapper qmsFmeaRiskMapper;
    private final QmsFmeaRiskTrackMapper qmsFmeaRiskTrackMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<String> listTypes() {
        return new ArrayList<>(TYPES);
    }

    @Override
    public Map<String, Object> predict(int severity, int occurrence, int detection) {
        int rpn = severity * occurrence * detection;
        Map<String, Object> m = new HashMap<>();
        m.put("rpn", rpn);
        m.put("riskLevel", riskLevel(rpn, severity));
        m.put("highRisk", isHighRisk(rpn, severity));
        return m;
    }

    @Override
    public List<QmsFmeaRisk> list(String status) {
        LambdaQueryWrapper<QmsFmeaRisk> w = new LambdaQueryWrapper<>();
        if (!CompanyContext.isAdmin()) {
            w.eq(QmsFmeaRisk::getOrgId, currentOrgId());
        }
        if (StringUtils.hasText(status)) {
            w.eq(QmsFmeaRisk::getStatus, status);
        }
        w.orderByDesc(QmsFmeaRisk::getRpn).orderByDesc(QmsFmeaRisk::getCreatedAt);
        return qmsFmeaRiskMapper.selectList(w);
    }

    @Override
    @Transactional
    public QmsFmeaRisk create(QmsFmeaRisk risk) {
        risk.setOrgId(currentOrgId());
        risk.setRpn(Short.valueOf((short) computeRpn(risk)));
        risk.setRiskLevel(riskLevel(risk.getRpn(), toInt(risk.getSeverityS())));
        risk.setHighRiskFlag(isHighRisk(risk.getRpn(), toInt(risk.getSeverityS())));
        risk.setRiskNo("FMEA-" + System.currentTimeMillis());
        if (!StringUtils.hasText(risk.getStatus())) {
            risk.setStatus(risk.getHighRiskFlag() ? "待闭环" : "进行中");
        }
        if (risk.getIsDeleted() == null) {
            risk.setIsDeleted(false);
        }
        qmsFmeaRiskMapper.insert(risk);
        recordTrack(risk.getId(), null, risk.getStatus(),
                "创建风险项" + (risk.getHighRiskFlag() ? "(高风险)" : ""),
                "类型=" + risk.getFmeaType() + "; 工序=" + risk.getProcess());
        return risk;
    }

    @Override
    @Transactional
    public QmsFmeaRisk update(String id, QmsFmeaRisk risk) {
        QmsFmeaRisk existing = require(id);
        boolean measureChanged = false;
        if (StringUtils.hasText(risk.getAction())) {
            existing.setAction(risk.getAction());
            measureChanged = true;
        }
        if (StringUtils.hasText(risk.getOwner())) {
            existing.setOwner(risk.getOwner());
            measureChanged = true;
        }
        if (risk.getTargetDate() != null) {
            existing.setTargetDate(risk.getTargetDate());
            measureChanged = true;
        }
        if (StringUtils.hasText(risk.getStatus())) {
            existing.setStatus(risk.getStatus());
        }
        if (StringUtils.hasText(risk.getFmeaType())) {
            existing.setFmeaType(risk.getFmeaType());
        }
        if (StringUtils.hasText(risk.getProduct())) {
            existing.setProduct(risk.getProduct());
        }
        if (StringUtils.hasText(risk.getProcess())) {
            existing.setProcess(risk.getProcess());
        }
        if (StringUtils.hasText(risk.getFailureMode())) {
            existing.setFailureMode(risk.getFailureMode());
        }
        if (risk.getSeverityS() != null) {
            existing.setSeverityS(risk.getSeverityS());
        }
        if (risk.getOccurrenceO() != null) {
            existing.setOccurrenceO(risk.getOccurrenceO());
        }
        if (risk.getDetectionD() != null) {
            existing.setDetectionD(risk.getDetectionD());
        }
        existing.setRpn(Short.valueOf((short) computeRpn(existing)));
        existing.setRiskLevel(riskLevel(existing.getRpn(), toInt(existing.getSeverityS())));
        existing.setHighRiskFlag(isHighRisk(existing.getRpn(), toInt(existing.getSeverityS())));
        qmsFmeaRiskMapper.updateById(existing);
        if (measureChanged) {
            recordTrack(existing.getId(), null, existing.getStatus(),
                    "更新纠正措施/责任人/目标日期", "措施=" + existing.getAction());
        }
        return existing;
    }

    @Override
    @Transactional
    public QmsFmeaRisk close(String id, String evidence, String actionNote, boolean recurrenceVerified, String operator) {
        QmsFmeaRisk existing = require(id);
        if ("已闭环".equals(existing.getStatus())) {
            throw new BusinessException(400, "该风险项已闭环，无需重复操作");
        }
        // SR-PTL-024：闭环条件 —— 须提交措施执行证据；高风险项还须确认 3 个月无同类问题复发
        if (!StringUtils.hasText(evidence)) {
            throw new BusinessException(400, "闭环失败：须提交措施执行证据(evidence)");
        }
        if (Boolean.TRUE.equals(existing.getHighRiskFlag()) && !recurrenceVerified) {
            throw new BusinessException(400, "闭环失败：高风险项须确认『措施实施后 3 个月无同类问题复发』");
        }
        String fromStatus = existing.getStatus();
        existing.setEvidence(evidence);
        existing.setStatus("已闭环");
        existing.setCloseDate(LocalDate.now());
        qmsFmeaRiskMapper.updateById(existing);
        recordTrack(existing.getId(), fromStatus, "已闭环", "通过闭环确认",
                "证据=" + evidence + (actionNote != null ? "; 说明=" + actionNote : ""));
        return existing;
    }

    @Override
    public List<QmsFmeaRiskTrack> tracks(String id) {
        LambdaQueryWrapper<QmsFmeaRiskTrack> w = new LambdaQueryWrapper<>();
        w.eq(QmsFmeaRiskTrack::getRiskId, id).orderByAsc(QmsFmeaRiskTrack::getOperateTime);
        return qmsFmeaRiskTrackMapper.selectList(w);
    }

    // ==================== 内部工具 ====================

    private QmsFmeaRisk require(String id) {
        QmsFmeaRisk r = qmsFmeaRiskMapper.selectById(id);
        if (r == null || Boolean.TRUE.equals(r.getIsDeleted())) {
            throw new BusinessException("风险项不存在: " + id);
        }
        return r;
    }

    private int computeRpn(QmsFmeaRisk r) {
        return toInt(r.getSeverityS()) * toInt(r.getOccurrenceO()) * toInt(r.getDetectionD());
    }

    /** 风险等级：S≥9 或 RPN≥100 为高；RPN≥50 为中；否则低。 */
    private String riskLevel(int rpn, int severity) {
        if (severity >= 9 || rpn >= 100) {
            return "高";
        }
        if (rpn >= 50) {
            return "中";
        }
        return "低";
    }

    private boolean isHighRisk(int rpn, int severity) {
        return severity >= 9 || rpn >= 100;
    }

    private int toInt(Short v) {
        return v == null ? 0 : v.intValue();
    }

    private String currentOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = (u != null) ? u.orgId() : null;
        if (orgId == null || orgId.isBlank() || "ROOT".equals(orgId)) {
            return resolveDefaultOrgId();
        }
        return orgId;
    }

    private String currentUser() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return (u != null && StringUtils.hasText(u.username())) ? u.username() : "系统";
    }

    private String resolveDefaultOrgId() {
        try {
            String id = jdbcTemplate.queryForObject(
                    "SELECT id::text FROM ops.sys_org WHERE org_code='MZ' LIMIT 1", String.class);
            if (id != null) {
                return id;
            }
        } catch (Exception ignored) {
            // 忽略
        }
        try {
            return jdbcTemplate.queryForObject("SELECT id::text FROM ops.sys_org LIMIT 1", String.class);
        } catch (Exception e) {
            log.warn("resolveDefaultOrgId failed: {}", e.getMessage());
            return null;
        }
    }

    private void recordTrack(String riskId, String fromStatus, String toStatus, String actionNote, String evidence) {
        QmsFmeaRiskTrack t = new QmsFmeaRiskTrack();
        t.setId(UUID.randomUUID().toString());
        t.setOrgId(currentOrgId());
        t.setRiskId(riskId);
        t.setFromStatus(fromStatus);
        t.setToStatus(toStatus);
        t.setOperator(currentUser());
        t.setActionNote(actionNote);
        t.setEvidence(evidence);
        t.setOperateTime(LocalDateTime.now());
        qmsFmeaRiskTrackMapper.insert(t);
    }

    // ==================== 再发生重新打开 + 超期扫描 ====================

    @Override
    @Transactional
    public QmsFmeaRisk reopen(String id, String reason) {
        QmsFmeaRisk existing = require(id);
        if (!"已闭环".equals(existing.getStatus())) {
            throw new BusinessException(400, "仅已闭环的风险项可重新打开");
        }
        String fromStatus = existing.getStatus();
        existing.setStatus("进行中");
        existing.setCloseDate(null);
        qmsFmeaRiskMapper.updateById(existing);
        recordTrack(existing.getId(), fromStatus, "进行中",
                "重新打开:" + (reason != null ? reason : "验证期内再发生"), null);
        return existing;
    }

    /** SR-PTL-025:每天 8:00 扫描超期措施(已过 targetDate 且未闭环),超7天通知责任人,超14天通知质量经理。 */
    @Override
    @Transactional
    public int scanOverdue() {
        LocalDate today = LocalDate.now();
        List<QmsFmeaRisk> overdue = qmsFmeaRiskMapper.selectList(
                new LambdaQueryWrapper<QmsFmeaRisk>()
                        .lt(QmsFmeaRisk::getTargetDate, today)
                        .ne(QmsFmeaRisk::getStatus, "已闭环"));
        int count = 0;
        for (QmsFmeaRisk r : overdue) {
            if (r.getTargetDate() == null) continue;
            long days = ChronoUnit.DAYS.between(r.getTargetDate(), today);
            if (days >= 14) {
                notifyOverdue(r, "质量经理", days);
                count++;
            } else if (days >= 7) {
                notifyOverdue(r, "责任人", days);
                count++;
            }
        }
        return count;
    }

    @Scheduled(cron = "0 0 8 * * ?") // 每天 8:00
    public void scheduledScanOverdue() {
        try { scanOverdue(); } catch (Exception e) { log.warn("FMEA超期扫描异常: {}", e.getMessage()); }
    }

    private void notifyOverdue(QmsFmeaRisk r, String receiver, long days) {
        jdbcTemplate.update(
                "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) VALUES (?::uuid, 'FMEA_OVERDUE', ?, '站内', ?, ?, '告警', '已发送', now())",
                java.util.UUID.fromString("019f701f-0411-71ed-9eac-ab9440335832"),
                r.getId(), receiver,
                "FMEA风险项 " + r.getRiskNo() + " 措施超期" + days + "天,请尽快处理");
    }
}
