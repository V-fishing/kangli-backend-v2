package com.konli.qms.service.qmsmgmt.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.qmsmgmt.entity.QmsAdverseEvent;
import com.konli.qms.domain.qmsmgmt.mapper.QmsAdverseEventMapper;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.qmsmgmt.QmsAdverseEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QmsAdverseEventServiceImpl implements QmsAdverseEventService {

    private final QmsAdverseEventMapper mapper;
    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
        } catch (Exception e) {
            return null;
        }
    }

    private String defaultOrgId() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM ops.sys_org WHERE is_deleted = false ORDER BY created_at LIMIT 1", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String curUser() {
        try {
            return CompanyContext.get().userId();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public PageResult<QmsAdverseEvent> page(String keyword, String eventType, String status, int page, int size) {
        LambdaQueryWrapper<QmsAdverseEvent> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(QmsAdverseEvent::getEventNo, keyword)
                    .or().like(QmsAdverseEvent::getEventType, keyword)
                    .or().like(QmsAdverseEvent::getRootCause, keyword));
        }
        if (eventType != null && !eventType.isBlank()) w.eq(QmsAdverseEvent::getEventType, eventType);
        if (status != null && !status.isBlank()) w.eq(QmsAdverseEvent::getStatus, status);
        w.orderByDesc(QmsAdverseEvent::getCreatedAt);
        IPage<QmsAdverseEvent> p = mapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public QmsAdverseEvent get(String id) {
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public QmsAdverseEvent create(QmsAdverseEvent event) {
        if (event.getOrgId() == null) {
            String o = curOrg();
            event.setOrgId(o != null ? o : defaultOrgId());
        }
        if (event.getStatus() == null || event.getStatus().isBlank()) event.setStatus("PENDING");
        if (event.getSeverity() == null || event.getSeverity().isBlank()) event.setSeverity("GENERAL");
        if (event.getEventNo() == null || event.getEventNo().isBlank()) {
            event.setEventNo("AE-" + System.currentTimeMillis());
        }
        String u = curUser();
        event.setCreatedBy(u);
        event.setUpdatedBy(u);
        mapper.insert(event);
        try {
            notificationService.notify("qms-mgmt", "qms_adverse_created", "不良事件登记",
                    "不良事件 " + event.getEventNo() + "(" + event.getEventType()
                            + ") 已登记,请及时处理。", "qms_adverse_event", event.getId(), "/qms-mgmt/adverse");
        } catch (Exception e) {
            log.warn("[QMS-MGMT] 不良事件通知发送失败: {}", e.getMessage());
        }
        return event;
    }

    @Override
    @Transactional
    public QmsAdverseEvent update(QmsAdverseEvent event) {
        QmsAdverseEvent exist = mapper.selectById(event.getId());
        if (exist == null) throw new com.konli.qms.common.exception.BusinessException("不良事件不存在");
        exist.setEventType(event.getEventType());
        exist.setOccurStage(event.getOccurStage());
        exist.setSeverity(event.getSeverity());
        exist.setOccurAt(event.getOccurAt());
        exist.setReportAt(event.getReportAt());
        exist.setRootCause(event.getRootCause());
        exist.setHandleDesc(event.getHandleDesc());
        exist.setHandleTimeliness(event.getHandleTimeliness());
        exist.setOwner(event.getOwner());
        exist.setRemark(event.getRemark());
        exist.setUpdatedBy(curUser());
        mapper.updateById(exist);
        return exist;
    }

    @Override
    @Transactional
    public void delete(String id) {
        mapper.deleteById(id);
    }

    @Override
    @Transactional
    public void handle(String id, String status, String handleDesc, String owner) {
        QmsAdverseEvent exist = mapper.selectById(id);
        if (exist == null) throw new com.konli.qms.common.exception.BusinessException("不良事件不存在");
        exist.setStatus(status);
        if (handleDesc != null && !handleDesc.isBlank()) exist.setHandleDesc(handleDesc);
        if (owner != null && !owner.isBlank()) exist.setOwner(owner);
        exist.setUpdatedBy(curUser());
        mapper.updateById(exist);
    }

    @Override
    public Map<String, Object> stats() {
        String org = curOrg();
        LambdaQueryWrapper<QmsAdverseEvent> w = new LambdaQueryWrapper<>();
        if (org != null) w.eq(QmsAdverseEvent::getOrgId, org);
        List<QmsAdverseEvent> all = mapper.selectList(w);
        long pending = 0, handling = 0, done = 0;
        long general = 0, serious = 0, critical = 0;
        for (QmsAdverseEvent e : all) {
            switch (e.getStatus()) {
                case "PENDING" -> pending++;
                case "HANDLING" -> handling++;
                case "DONE" -> done++;
            }
            switch (e.getSeverity()) {
                case "GENERAL" -> general++;
                case "SERIOUS" -> serious++;
                case "CRITICAL" -> critical++;
            }
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("total", all.size());
        res.put("pending", pending);
        res.put("handling", handling);
        res.put("done", done);
        res.put("general", general);
        res.put("serious", serious);
        res.put("critical", critical);
        res.put("processRate", all.isEmpty() ? 0 : Math.round((handling + done) * 100.0 / all.size()));
        return res;
    }
}
