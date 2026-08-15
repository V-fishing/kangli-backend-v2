package com.konli.qms.service.qmsmgmt.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.qmsmgmt.entity.QmsAuditNc;
import com.konli.qms.domain.qmsmgmt.entity.QmsInternalAudit;
import com.konli.qms.domain.qmsmgmt.mapper.QmsAuditNcMapper;
import com.konli.qms.domain.qmsmgmt.mapper.QmsInternalAuditMapper;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.qmsmgmt.QmsInternalAuditService;
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
public class QmsInternalAuditServiceImpl implements QmsInternalAuditService {

    private final QmsInternalAuditMapper mapper;
    private final QmsAuditNcMapper ncMapper;
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
    public PageResult<QmsInternalAudit> page(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<QmsInternalAudit> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(QmsInternalAudit::getAuditNo, keyword)
                    .or().like(QmsInternalAudit::getAuditName, keyword)
                    .or().like(QmsInternalAudit::getAuditor, keyword));
        }
        if (status != null && !status.isBlank()) w.eq(QmsInternalAudit::getStatus, status);
        w.orderByDesc(QmsInternalAudit::getCreatedAt);
        IPage<QmsInternalAudit> p = mapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public QmsInternalAudit get(String id) {
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public QmsInternalAudit create(QmsInternalAudit audit) {
        if (audit.getOrgId() == null) {
            String o = curOrg();
            audit.setOrgId(o != null ? o : defaultOrgId());
        }
        if (audit.getStatus() == null || audit.getStatus().isBlank()) audit.setStatus("PLANNED");
        if (audit.getAuditNo() == null || audit.getAuditNo().isBlank()) {
            audit.setAuditNo("IA-" + System.currentTimeMillis());
        }
        String u = curUser();
        audit.setCreatedBy(u);
        audit.setUpdatedBy(u);
        mapper.insert(audit);
        return audit;
    }

    @Override
    @Transactional
    public QmsInternalAudit update(QmsInternalAudit audit) {
        QmsInternalAudit exist = mapper.selectById(audit.getId());
        if (exist == null) throw new com.konli.qms.common.exception.BusinessException("内审不存在");
        exist.setAuditName(audit.getAuditName());
        exist.setAuditScope(audit.getAuditScope());
        exist.setPlanDate(audit.getPlanDate());
        exist.setAuditor(audit.getAuditor());
        exist.setStatus(audit.getStatus());
        exist.setRemark(audit.getRemark());
        exist.setUpdatedBy(curUser());
        mapper.updateById(exist);
        return exist;
    }

    @Override
    @Transactional
    public void delete(String id) {
        mapper.deleteById(id);
        // 级联软删不符合项
        ncMapper.delete(new LambdaQueryWrapper<QmsAuditNc>().eq(QmsAuditNc::getAuditId, id));
    }

    @Override
    @Transactional
    public void advance(String id, String status) {
        QmsInternalAudit exist = mapper.selectById(id);
        if (exist == null) throw new com.konli.qms.common.exception.BusinessException("内审不存在");
        String cur = exist.getStatus();
        if (!isAllowedTransition(cur, status)) {
            throw new com.konli.qms.common.exception.BusinessException(
                    "非法的状态流转: " + cur + " → " + status);
        }
        exist.setStatus(status);
        exist.setUpdatedBy(curUser());
        mapper.updateById(exist);
    }

    /** 内审状态机(单向): PLANNED→ONGOING→DONE→CLOSED。允许停留原状态(幂等推进)。 */
    private boolean isAllowedTransition(String from, String to) {
        if (from == null) from = "PLANNED";
        if (from.equals(to)) return true;
        return switch (from) {
            case "PLANNED"  -> "ONGOING".equals(to);
            case "ONGOING"  -> "DONE".equals(to);
            case "DONE"     -> "CLOSED".equals(to);
            case "CLOSED"   -> false;
            default         -> false;
        };
    }

    @Override
    public PageResult<QmsAuditNc> ncPage(String auditId, String status, int page, int size) {
        LambdaQueryWrapper<QmsAuditNc> w = new LambdaQueryWrapper<>();
        if (auditId != null && !auditId.isBlank()) w.eq(QmsAuditNc::getAuditId, auditId);
        if (status != null && !status.isBlank()) w.eq(QmsAuditNc::getStatus, status);
        w.orderByDesc(QmsAuditNc::getCreatedAt);
        IPage<QmsAuditNc> p = ncMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public QmsAuditNc getNc(String id) {
        return ncMapper.selectById(id);
    }

    @Override
    @Transactional
    public QmsAuditNc saveNc(QmsAuditNc nc) {
        if (nc.getOrgId() == null) {
            String o = curOrg();
            nc.setOrgId(o != null ? o : defaultOrgId());
        }
        if (nc.getStatus() == null || nc.getStatus().isBlank()) nc.setStatus("OPEN");
        if (nc.getSeverity() == null || nc.getSeverity().isBlank()) nc.setSeverity("MINOR");
        if (nc.getNcNo() == null || nc.getNcNo().isBlank()) {
            nc.setNcNo("NC-" + System.currentTimeMillis());
        }
        String u = curUser();
        boolean isNew = nc.getId() == null;
        if (isNew) {
            nc.setCreatedBy(u);
            ncMapper.insert(nc);
            try {
                notificationService.notify("qms-mgmt", "qms_audit_nc_created", "内审不符合项新增",
                        "内审 " + nc.getNcNo() + " 新增不符合项,请责任部门及时整改。",
                        "qms_audit_nc", nc.getId(), "/qms-mgmt/audit");
            } catch (Exception e) {
                log.warn("[QMS-MGMT] 不符合项通知发送失败: {}", e.getMessage());
            }
        } else {
            QmsAuditNc exist = ncMapper.selectById(nc.getId());
            if (exist == null) throw new com.konli.qms.common.exception.BusinessException("不符合项不存在");
            exist.setNcDesc(nc.getNcDesc());
            exist.setClause(nc.getClause());
            exist.setSeverity(nc.getSeverity());
            exist.setStatus(nc.getStatus());
            exist.setOwner(nc.getOwner());
            exist.setDueDate(nc.getDueDate());
            exist.setCorrective(nc.getCorrective());
            exist.setVerifyResult(nc.getVerifyResult());
            if ("CLOSED".equals(nc.getStatus()) && exist.getClosedAt() == null) {
                exist.setClosedAt(LocalDateTime.now());
            }
            exist.setUpdatedBy(u);
            ncMapper.updateById(exist);
        }
        return nc;
    }

    @Override
    @Transactional
    public void deleteNc(String id) {
        ncMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> stats() {
        String org = curOrg();
        LambdaQueryWrapper<QmsInternalAudit> w = new LambdaQueryWrapper<>();
        if (org != null) w.eq(QmsInternalAudit::getOrgId, org);
        List<QmsInternalAudit> audits = mapper.selectList(w);
        LambdaQueryWrapper<QmsAuditNc> nw = new LambdaQueryWrapper<>();
        if (org != null) nw.eq(QmsAuditNc::getOrgId, org);
        List<QmsAuditNc> ncs = ncMapper.selectList(nw);

        long planned = 0, ongoing = 0, done = 0, closed = 0;
        for (QmsInternalAudit a : audits) {
            switch (a.getStatus()) {
                case "PLANNED" -> planned++;
                case "ONGOING" -> ongoing++;
                case "DONE" -> done++;
                case "CLOSED" -> closed++;
            }
        }
        long ncOpen = 0, ncInProgress = 0, ncClosed = 0;
        for (QmsAuditNc n : ncs) {
            switch (n.getStatus()) {
                case "OPEN" -> ncOpen++;
                case "IN_PROGRESS" -> ncInProgress++;
                case "CLOSED" -> ncClosed++;
            }
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("auditTotal", audits.size());
        res.put("planned", planned);
        res.put("ongoing", ongoing);
        res.put("done", done);
        res.put("closed", closed);
        long ncTotal = ncs.size();
        res.put("ncTotal", ncTotal);
        res.put("ncOpen", ncOpen);
        res.put("ncInProgress", ncInProgress);
        res.put("ncClosed", ncClosed);
        res.put("ncCloseRate", ncTotal == 0 ? 0 : Math.round(ncClosed * 100.0 / ncTotal));
        return res;
    }
}
