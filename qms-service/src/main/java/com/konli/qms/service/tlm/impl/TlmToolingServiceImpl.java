package com.konli.qms.service.tlm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.tlm.entity.TlmRepair;
import com.konli.qms.domain.tlm.entity.TlmScrap;
import com.konli.qms.domain.tlm.entity.TlmTooling;
import com.konli.qms.domain.tlm.entity.TlmToolWoBind;
import com.konli.qms.domain.tlm.mapper.TlmRepairMapper;
import com.konli.qms.domain.tlm.mapper.TlmScrapMapper;
import com.konli.qms.domain.tlm.mapper.TlmToolWoBindMapper;
import com.konli.qms.domain.tlm.mapper.TlmToolingMapper;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.fia.FiaTaskService;
import com.konli.qms.service.tlm.TlmToolingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TlmToolingServiceImpl implements TlmToolingService {

    private final TlmToolingMapper toolingMapper;
    private final TlmRepairMapper repairMapper;
    private final TlmScrapMapper scrapMapper;
    private final TlmToolWoBindMapper bindMapper;
    private final NotificationService notificationService;
    private final JdbcTemplate jdbcTemplate;
    private final FiaTaskService fiaTaskService;

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
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
    public PageResult<TlmTooling> page(String keyword, String category, String status,
                                       String ownerId, int page, int size) {
        LambdaQueryWrapper<TlmTooling> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(TlmTooling::getToolNo, keyword).or().like(TlmTooling::getToolName, keyword));
        }
        if (category != null && !category.isBlank()) w.eq(TlmTooling::getToolCategory, category);
        if (status != null && !status.isBlank()) w.eq(TlmTooling::getStatus, status);
        if (ownerId != null && !ownerId.isBlank()) w.eq(TlmTooling::getOwnerId, ownerId);
        w.orderByDesc(TlmTooling::getCreatedAt);
        IPage<TlmTooling> p = toolingMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<TlmTooling>(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public TlmTooling get(String id) {
        return toolingMapper.selectById(id);
    }

    @Override
    @Transactional
    public TlmTooling create(TlmTooling t) {
        if (t.getStatus() == null) t.setStatus("IN_USE");
        if (t.getBindCount() == null) t.setBindCount(0);
        if (t.getLocked() == null) t.setLocked(false);
        toolingMapper.insert(t);
        // 工装投用(新建首次置 IN_USE)强制触发首件验证: 仅当维护产品编码+工序时自动建 TOOLING 任务
        if ("IN_USE".equals(t.getStatus()) && t.getProductCode() != null && !t.getProductCode().isBlank()
                && t.getProcName() != null && !t.getProcName().isBlank()) {
            try {
                fiaTaskService.createFromTooling(
                        t.getOrgId(), t.getId(), null, t.getProductCode(), t.getProcName(),
                        t.getToolName(), null, "工装投用后", null, t.getSupplierId(),
                        String.format("工装 %s(%s) 投用后自动触发首件检验", t.getToolName(), t.getToolNo()));
            } catch (Exception ex) {
                // 投用主流程不阻断: 标准缺失等异常仅告警,留给台账"待首件"强提醒人工补建
                log.warn("[TLM→FIA] 工装 {} 投用首件自动触发失败(可人工补建): {}", t.getToolNo(), ex.getMessage());
            }
        }
        return t;
    }

    @Override
    @Transactional
    public TlmTooling update(TlmTooling t) {
        toolingMapper.updateById(t);
        return t;
    }

    @Override
    @Transactional
    public void delete(String id) {
        toolingMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void repair(String id, String faultDesc, String approverId) {
        TlmTooling t = toolingMapper.selectById(id);
        if (t == null) throw new RuntimeException("工装不存在");
        t.setStatus("REPAIRING");
        toolingMapper.updateById(t);
        TlmRepair r = new TlmRepair();
        r.setOrgId(t.getOrgId());
        r.setToolId(id);
        r.setRepairNo("TLM-RP-" + System.currentTimeMillis());
        r.setFaultDesc(faultDesc);
        r.setApproverId(approverId);
        r.setStatus("PENDING");
        r.setCreatedBy(curUser());
        repairMapper.insert(r);
        if (approverId != null && !approverId.isBlank()) {
            notificationService.notifyUser(approverId, "工装送修待处理",
                    "工装 " + t.getToolName() + "(" + t.getToolNo() + ") 已送修,请处理维修工单",
                    "tlm_repair", r.getId(), "/tlm/tooling/" + id);
        }
    }

    @Override
    @Transactional
    public void scrap(String id, String scrapMethod, String reason, String approverId) {
        TlmTooling t = toolingMapper.selectById(id);
        if (t == null) throw new RuntimeException("工装不存在");
        TlmScrap s = new TlmScrap();
        s.setOrgId(t.getOrgId());
        s.setToolId(id);
        s.setScrapNo("TLM-SC-" + System.currentTimeMillis());
        s.setScrapMethod(scrapMethod);
        s.setReason(reason);
        s.setStatus("PENDING");
        s.setApproverId(approverId);
        s.setCreatedBy(curUser());
        scrapMapper.insert(s);
        if (approverId != null && !approverId.isBlank()) {
            notificationService.notifyUser(approverId, "工装报废待审批",
                    "工装 " + t.getToolName() + "(" + t.getToolNo() + ") 发起报废,请审批",
                    "tlm_scrap", s.getId(), "/tlm/tooling/" + id);
        }
    }

    @Override
    @Transactional
    public void onScrapApproved(String scrapId) {
        TlmScrap s = scrapMapper.selectById(scrapId);
        if (s == null) return;
        s.setStatus("APPROVED");
        scrapMapper.updateById(s);
        TlmTooling t = toolingMapper.selectById(s.getToolId());
        if (t != null) {
            t.setStatus("SCRAPPED");
            toolingMapper.updateById(t);
        }
    }

    @Override
    @Transactional
    public void onScrapRejected(String scrapId) {
        TlmScrap s = scrapMapper.selectById(scrapId);
        if (s == null) return;
        s.setStatus("REJECTED");
        scrapMapper.updateById(s);
    }

    @Override
    @Transactional
    public void lock(String id, boolean locked) {
        TlmTooling t = toolingMapper.selectById(id);
        if (t == null) throw new RuntimeException("工装不存在");
        t.setLocked(locked);
        toolingMapper.updateById(t);
    }

    @Override
    @Transactional
    public void bind(String id, String woNo) {
        TlmTooling t = toolingMapper.selectById(id);
        if (t == null) throw new RuntimeException("工装不存在");
        TlmToolWoBind b = new TlmToolWoBind();
        b.setOrgId(t.getOrgId());
        b.setToolId(id);
        b.setWoNo(woNo);
        b.setBoundAt(LocalDateTime.now());
        b.setCreatedBy(curUser());
        bindMapper.insert(b);
        int cnt = t.getBindCount() == null ? 0 : t.getBindCount();
        t.setBindCount(cnt + 1);
        boolean overLife = t.getDesignLife() != null && (cnt + 1) >= t.getDesignLife();
        if (overLife) {
            t.setLocked(true);
            String oid = (t.getOrgId() == null) ? "019f701f-0411-71ed-9eac-ab9440335832" : t.getOrgId();
            try {
                jdbcTemplate.update(
                        "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) VALUES (?::uuid, 'TLM_LIFE_OVER', ?, '站内', '工装管理员', ?, '提醒', '已发送', now())",
                        java.util.UUID.fromString(oid), t.getId(),
                        "工装 " + t.getToolName() + "(" + t.getToolNo() + ") 已达寿命上限,已自动锁定");
            } catch (Exception e) {
                log.warn("工装寿命预警写入失败: {}", e.getMessage());
            }
        }
        toolingMapper.updateById(t);
    }

    @Override
    public List<TlmTooling> abnormalList(String type) {
        LambdaQueryWrapper<TlmTooling> w = new LambdaQueryWrapper<>();
        String org = curOrg();
        if (org != null) w.eq(TlmTooling::getOrgId, org);
        switch (type) {
            case "locked":
                w.eq(TlmTooling::getLocked, true);
                break;
            case "life":
                w.isNotNull(TlmTooling::getDesignLife)
                        .apply("bind_count >= design_life");
                break;
            case "calib":
                w.isNotNull(TlmTooling::getCalibDueDate)
                        .lt(TlmTooling::getCalibDueDate, LocalDate.now());
                break;
            default:
                w.eq(TlmTooling::getLocked, true);
        }
        return toolingMapper.selectList(w);
    }

    @Override
    @Transactional
    public void onRepairCompleted(String id) {
        TlmTooling t = toolingMapper.selectById(id);
        if (t == null) throw new RuntimeException("工装不存在");
        if (!"REPAIRING".equals(t.getStatus())) {
            throw new RuntimeException("工装当前不是维修中状态，无法执行维修完成");
        }
        // 状态恢复为在用
        t.setStatus("IN_USE");
        toolingMapper.updateById(t);

        // 触发 FIA 首件检验任务(工装维修后)
        // 前置校验由 createFromTooling 内部完成(product_code/proc_name 为空或 matchStd 失败会抛异常)，
        // 异常直接传播到 Controller → 前端，让用户看到明确错误提示而不是静默失败
        fiaTaskService.createFromTooling(
                t.getOrgId(),
                t.getId(),
                null, // woNo 自动生成
                t.getProductCode(),
                t.getProcName(),
                t.getToolName(),
                null, // lineName 兜底
                "工装维修后",
                null, // batchNo 自动触发场景兜底生成
                t.getSupplierId(),
                String.format("工装 %s(%s) 维修完成后自动触发首件检验", t.getToolName(), t.getToolNo())
        );
        log.info("[TLM→FIA] 工装 {} 维修完成,已触发首件检验任务", t.getToolNo());
    }
}
