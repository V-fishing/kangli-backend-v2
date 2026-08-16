package com.konli.qms.service.tlm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
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
import com.konli.qms.service.sqm.SqmAuditApprovalCfgService;
import com.konli.qms.service.sqm.dto.AuditorDef;
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
    private static final String MZ_ORG = "019f701f-0411-71ed-9eac-ab9440335832";

    private final NotificationService notificationService;
    private final JdbcTemplate jdbcTemplate;
    private final FiaTaskService fiaTaskService;
    private final SqmAuditApprovalCfgService approvalCfgService;

    /** 工装报废审批在「系统管理 › 审核配置」登记的 auditType,须与前端 AuditApprovalConfig.vue 的 TLM_AUDIT_TYPE 一致。 */
    private static final String TLM_SCRAP_AUDIT_TYPE = "工装报废审核";
    private static final String TLM_REPAIR_AUDIT_TYPE = "工装维修审核";

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
        } catch (Exception e) {
            return null;
        }
    }

    /** ROOT/未切换组织时,兜底取 sys_org 中第一个有效组织,确保 tlm_tooling.org_id 外键成立。 */
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
        if (t.getOrgId() == null) {
            String o = curOrg();
            t.setOrgId(o != null ? o : defaultOrgId());
        }
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
    public void repair(String id, String faultDesc, String faultType, String approverId) {
        TlmTooling t = toolingMapper.selectById(id);
        if (t == null) throw new RuntimeException("工装不存在");
        // 配置即权威:未显式指定审批人时,从「系统管理 › 审核配置」的「工装维修审核」节点读取默认审批人
        if (approverId == null || approverId.isBlank()) {
            approverId = resolveRepairApprovers();
        }
        TlmRepair r = new TlmRepair();
        r.setOrgId(t.getOrgId());
        r.setToolId(id);
        r.setRepairNo("TLM-RP-" + System.currentTimeMillis());
        r.setFaultDesc(faultDesc);
        r.setFaultType(faultType != null && !faultType.isBlank() ? faultType : "其他");
        r.setApproverId(approverId);
        r.setStatus("PENDING");
        r.setCreatedBy(curUser());
        repairMapper.insert(r);
        // 工装保持原状态, 待审批中心通过后(repair-approved)才置 REPAIRING
        if (approverId != null && !approverId.isBlank()) {
            notificationService.notifyUser(approverId, "工装送修待审批",
                    "工装 " + t.getToolName() + "(" + t.getToolNo() + ") 发起送修,请审批",
                    "tlm_repair", r.getId(), "/tlm/tooling/" + id);
        } else {
            // 无审批人配置: 直接置维修中(退化流程, 不阻断主流程)
            t.setStatus("REPAIRING");
            toolingMapper.updateById(t);
            log.warn("[TLM] 工装 {} 发起送修但未配置审批人(审核配置中无「工装维修审核」节点),流程待人工介入", t.getToolNo());
        }
    }

    /**
     * 从审核配置读取「工装维修审核」节点的默认审批人(userIds 逗号串)。
     * 与 resolveScrapApprovers 复用同一 SqmAuditApprovalCfgService.resolve 范式。
     */
    private String resolveRepairApprovers() {
        try {
            var auditors = approvalCfgService.resolve(TLM_REPAIR_AUDIT_TYPE);
            if (auditors == null || auditors.isEmpty()) return null;
            StringBuilder sb = new StringBuilder();
            for (AuditorDef a : auditors) {
                if (a.getUserIds() != null && !a.getUserIds().isEmpty()) {
                    for (String uid : a.getUserIds()) {
                        if (uid != null && !uid.isBlank()) {
                            if (sb.length() > 0) sb.append(",");
                            sb.append(uid.trim());
                        }
                    }
                } else if (a.getUserId() != null && !a.getUserId().isBlank()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(a.getUserId().trim());
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception e) {
            log.warn("[TLM] 读取工装维修审批配置失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public void onRepairApproved(String repairId) {
        TlmRepair r = repairMapper.selectById(repairId);
        if (r == null) return;
        assertRepairApprover(r);
        r.setStatus("REPAIRING");
        repairMapper.updateById(r);
        TlmTooling t = toolingMapper.selectById(r.getToolId());
        if (t != null && !"REPAIRING".equals(t.getStatus())) {
            t.setStatus("REPAIRING");
            toolingMapper.updateById(t);
        }
    }

    @Override
    @Transactional
    public void onRepairRejected(String repairId) {
        TlmRepair r = repairMapper.selectById(repairId);
        if (r == null) return;
        assertRepairApprover(r);
        r.setStatus("REJECTED");
        repairMapper.updateById(r);
    }

    /**
     * 维修审批回调身份校验:当前登录用户必须命中维修单指定审批人(OR 语义),否则抛 403。
     */
    private void assertRepairApprover(TlmRepair r) {
        String approverId = r.getApproverId();
        if (approverId == null || approverId.isBlank()) return;
        String cur = curUser();
        if (cur == null) throw new BusinessException(403, "当前登录用户非该维修单指定审批人");
        boolean allowed = java.util.Arrays.stream(approverId.split(","))
                .map(String::trim).anyMatch(id -> id.equals(cur));
        if (!allowed) {
            throw new BusinessException(403, "当前登录用户非该维修单指定审批人");
        }
    }

    @Override
    @Transactional
    public void scrap(String id, String scrapMethod, String reason, String approverId) {
        TlmTooling t = toolingMapper.selectById(id);
        if (t == null) throw new RuntimeException("工装不存在");
        // 计量器具(GAUGE)报废前校验(需求3-3): 维修后强制重校准方可解锁, 锁定待校准状态的器具禁止报废,
        // 避免不合格计量器具未经校准复核即退出生命周期, 造成追溯断点。
        if ("GAUGE".equals(t.getToolCategory()) && Boolean.TRUE.equals(t.getLocked())) {
            throw new BusinessException("计量器具 " + t.getToolNo() + " 处于锁定待校准状态，请先完成校准录入并解锁后再发起报废");
        }
        // 配置即权威:未显式指定审批人时,从「系统管理 › 审核配置」的「工装报废审核」节点读取默认审批人
        if (approverId == null || approverId.isBlank()) {
            approverId = resolveScrapApprovers();
        }
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
        } else {
            log.warn("[TLM] 工装 {} 发起报废但未配置审批人(审核配置中无「工装报废审核」节点),流程待人工介入", t.getToolNo());
        }
    }

    /**
     * 从审核配置读取「工装报废审核」节点的默认审批人(userIds 逗号串)。
     * 复用 SqmAuditApprovalCfgService.resolve 范式(与 SQM 会签一致);无配置返回 null(不阻断主流程)。
     */
    private String resolveScrapApprovers() {
        try {
            var auditors = approvalCfgService.resolve(TLM_SCRAP_AUDIT_TYPE);
            if (auditors == null || auditors.isEmpty()) return null;
            StringBuilder sb = new StringBuilder();
            for (AuditorDef a : auditors) {
                if (a.getUserIds() != null && !a.getUserIds().isEmpty()) {
                    for (String uid : a.getUserIds()) {
                        if (uid != null && !uid.isBlank()) {
                            if (sb.length() > 0) sb.append(",");
                            sb.append(uid.trim());
                        }
                    }
                } else if (a.getUserId() != null && !a.getUserId().isBlank()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(a.getUserId().trim());
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception e) {
            log.warn("[TLM] 读取工装报废审批配置失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public void onScrapApproved(String scrapId) {
        TlmScrap s = scrapMapper.selectById(scrapId);
        if (s == null) return;
        assertScrapApprover(s);
        s.setStatus("APPROVED");
        scrapMapper.updateById(s);
        TlmTooling t = toolingMapper.selectById(s.getToolId());
        if (t != null) {
            t.setStatus("SCRAPPED");
            toolingMapper.updateById(t);
        }
        // 报废审批通过即归档(留存全生命周期): 写 tlm_scrap_archive, 供归档中心查询
        try {
            jdbcTemplate.update(
                "INSERT INTO ops.tlm_scrap_archive (id, org_id, archive_no, scrap_id, tool_id, scrap_no, tool_no, tool_name, scrap_method, reason, "
                + "last_calib_date, calib_due_date, tool_category, retention_until, report_hash, pdf_ref, status, created_by) "
                + "VALUES (ops.gen_uuid_v7(), ?::uuid, ?, ?, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, (now() + interval '10 year')::date, ?, 'placeholder://tlm-scrap', '已归档', ?) "
                + "ON CONFLICT (scrap_id) DO NOTHING",
                (t != null && t.getOrgId() != null) ? t.getOrgId() : (s.getOrgId() != null ? s.getOrgId() : MZ_ORG),
                "TLM-SCA-" + System.currentTimeMillis(),
                scrapId,
                s.getToolId(),
                s.getScrapNo(),
                t != null ? t.getToolNo() : null,
                t != null ? t.getToolName() : null,
                s.getScrapMethod(),
                s.getReason(),
                t != null ? t.getCalibDate() : null,
                t != null ? t.getCalibDueDate() : null,
                t != null ? t.getToolCategory() : null,
                ("sha256:" + scrapId),
                curUser());
        } catch (Exception e) {
            log.warn("工装报废归档写入失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void onScrapRejected(String scrapId) {
        TlmScrap s = scrapMapper.selectById(scrapId);
        if (s == null) return;
        assertScrapApprover(s);
        s.setStatus("REJECTED");
        scrapMapper.updateById(s);
    }

    /**
     * 报废审批回调身份校验:当前登录用户必须命中报废单指定审批人(OR 语义,approver_id 为逗号串),
     * 否则抛 403 防止越权签字。与 SQM 会签 approve 校验范式一致;approver_id 为空(未配置)时退化为不校验。
     */
    private void assertScrapApprover(TlmScrap s) {
        String approverId = s.getApproverId();
        if (approverId == null || approverId.isBlank()) return;
        String cur = curUser();
        if (cur == null) throw new BusinessException(403, "当前登录用户非该报废单指定审批人");
        boolean allowed = java.util.Arrays.stream(approverId.split(","))
                .map(String::trim).anyMatch(id -> id.equals(cur));
        if (!allowed) {
            throw new BusinessException(403, "当前登录用户非该报废单指定审批人");
        }
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
        // 工装首件质量门禁(quality gate): 投用/维修/变更后若仍存在未完成的工装首件任务,
        // 且工装档案完整(产品编码+工序均维护,标准可匹配、首件任务本应已生成),则禁止派工,
        // 强制先完成首件检验。档案不完整(标准无法匹配、首件任务本就未生成)属数据问题,豁免门禁。
        boolean archComplete = t.getProductCode() != null && !t.getProductCode().isBlank()
                && t.getProcName() != null && !t.getProcName().isBlank();
        if (archComplete && fiaTaskService.hasPendingToolingFirst(id)) {
            throw new BusinessException("工装存在未完成的首件检验任务，请先完成工装首件检验后再派工");
        }
        // 计量器具(GAUGE)校准状态门禁(强约束, 需求2-1): 超期/失效/未校准的器具禁止绑定工序,
        // 防止不合格计量器具被使用。calib_due_date 为空视为未校准, 一并拦截。
        if ("GAUGE".equals(t.getToolCategory())) {
            if (t.getCalibDueDate() == null || t.getCalibDueDate().isBefore(LocalDate.now())) {
                throw new BusinessException("计量器具 " + t.getToolNo() + " 校准已超期或未校准，无法绑定工单，请先完成校准");
            }
        }
        TlmToolWoBind b = new TlmToolWoBind();
        b.setOrgId(t.getOrgId());
        b.setToolId(id);
        b.setWoNo(woNo);
        b.setBoundAt(LocalDateTime.now());
        b.setCreatedBy(curUser());
        // 计量器具(GAUGE)绑定即记录校准状态快照, 供产品批次/检验记录反查该件产品当时使用的计量器具
        // 是否在合格有效期内(强约束, 需求2-2 计量数据绑定追溯)。
        if ("GAUGE".equals(t.getToolCategory())) {
            b.setCalibNo(t.getToolNo());
            b.setCalibDate(t.getCalibDate());
            b.setCalibDueDate(t.getCalibDueDate());
        }
        bindMapper.insert(b);
        int cnt = t.getBindCount() == null ? 0 : t.getBindCount();
        t.setBindCount(cnt + 1);
        boolean overLife = t.getDesignLife() != null && (cnt + 1) >= t.getDesignLife();
        if (overLife) {
            t.setLocked(true);
            try {
                // 统一走通知配置(module=tlm, event_code=tlm_life_over)解析接收人/渠道(强约束 C2)
                notificationService.notify("tlm", "tlm_life_over", "工装寿命超限预警",
                        "工装 " + t.getToolName() + "(" + t.getToolNo() + ") 已达寿命上限,已自动锁定",
                        "tlm_life_over", t.getId(), "/tlm/tooling/" + t.getId());
            } catch (Exception e) {
                log.warn("工装寿命预警通知失败: {}", e.getMessage());
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
            case "maint":
                w.isNotNull(TlmTooling::getNextMaintDate)
                        .lt(TlmTooling::getNextMaintDate, LocalDate.now());
                break;
            default:
                w.eq(TlmTooling::getLocked, true);
        }
        return toolingMapper.selectList(w);
    }

    @Override
    @Transactional
    public void repairFill(String id, String measure) {
        TlmRepair r = repairMapper.selectOne(new LambdaQueryWrapper<TlmRepair>()
                .eq(TlmRepair::getToolId, id).orderByDesc(TlmRepair::getCreatedAt).last("LIMIT 1"));
        if (r == null) throw new RuntimeException("未找到该工装的维修工单");
        // 已结束(已完成/已验证/已驳回)不可再填措施;待处理与维修中均允许回填/补充措施
        if ("DONE".equals(r.getStatus()) || "VERIFIED".equals(r.getStatus()) || "REJECTED".equals(r.getStatus())) {
            throw new BusinessException("维修工单已结束，无法填写措施");
        }
        r.setMeasure(measure);
        r.setStatus("REPAIRING");
        repairMapper.updateById(r);
    }

    @Override
    @Transactional
    public void repairDone(String id) {
        TlmRepair r = repairMapper.selectOne(new LambdaQueryWrapper<TlmRepair>()
                .eq(TlmRepair::getToolId, id).orderByDesc(TlmRepair::getCreatedAt).last("LIMIT 1"));
        if (r == null) throw new RuntimeException("未找到该工装的维修工单");
        if (!"REPAIRING".equals(r.getStatus())) {
            throw new BusinessException("维修工单当前不是维修中状态，无法标记完成");
        }
        r.setStatus("DONE");
        repairMapper.updateById(r);
        // 工装仍保持 REPAIRING, 等待首件验证通过后才恢复在用
    }

    @Override
    @Transactional
    public void onRepairCompleted(String id) {
        onRepairCompleted(id, true);
    }

    /**
     * 维修完成验证(需求 2.5.3.3 深度闭环): verifyPass=true 验证通过恢复在用并触发首件;
     * verifyPass=false 验证不通过 → 自动锁定工装(locked=true)并通知, 禁止派工使用。
     */
    @Override
    @Transactional
    public void onRepairCompleted(String id, boolean verifyPass) {
        TlmTooling t = toolingMapper.selectById(id);
        if (t == null) throw new RuntimeException("工装不存在");
        if (!"REPAIRING".equals(t.getStatus()) && !"DONE".equals(t.getStatus())) {
            throw new RuntimeException("工装当前不是维修中/已完成维修状态，无法执行验证");
        }
        TlmRepair r = repairMapper.selectOne(new LambdaQueryWrapper<TlmRepair>()
                .eq(TlmRepair::getToolId, id).orderByDesc(TlmRepair::getCreatedAt).last("LIMIT 1"));
        if (r != null && !"VERIFIED".equals(r.getStatus())) {
            r.setStatus("VERIFIED");
            repairMapper.updateById(r);
        }
        if (!verifyPass) {
            // 验证不通过: 自动锁定, 禁止派工
            t.setLocked(Boolean.TRUE);
            t.setStatus("REPAIRING");
            toolingMapper.updateById(t);
            try {
                notificationService.notify("tlm", "tlm_repair_verify_fail", "工装验证不通过已锁定",
                        "工装 " + t.getToolName() + "(" + t.getToolNo() + ") 维修后验证不通过,已自动锁定禁止派工,请重新安排维修。",
                        "tlm_tooling", t.getId(), "/tlm/tooling/" + id);
            } catch (Exception e) {
                log.warn("[TLM] 工装验证不通过锁定通知失败: {}", e.getMessage());
            }
            log.info("[TLM] 工装 {} 维修验证不通过,已自动锁定", t.getToolNo());
            return;
        }
        // 状态恢复为在用
        t.setStatus("IN_USE");
        // 计量器具(GAUGE)维修后强制重校准(需求3-2): 锁定器具, 待校准录入合格后方可解锁(recordResult 置 locked=false)。
        // 校准日期/到期由校准录入回写, 此处不预填, 避免在未校准状态下显示"合格"。
        if ("GAUGE".equals(t.getToolCategory())) {
            t.setLocked(Boolean.TRUE);
            log.info("[TLM] 计量器具 {} 维修完成,已锁定待重校准", t.getToolNo());
            // 计量器具维修完成待校准提醒(强约束 C2): 经 notify_config(tlm, tlm_repair_done) 推送计量管理员。
            try {
                notificationService.notify("tlm", "tlm_repair_done", "计量器具维修完成待校准",
                        "计量器具 " + t.getToolName() + "(" + t.getToolNo() + ") 维修已完成，请安排重新校准并录入后方可解锁使用",
                        "tlm_repair_done", t.getId(), "/tlm/metro");
            } catch (Exception e) {
                log.warn("计量器具维修完成待校准通知失败: {}", e.getMessage());
            }
        }
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

    /**
     * 报废单分页查询。支持按工装编号/名称关键词、报废单号、状态筛选。
     * 仅返回当前组织数据（curOrg 为空时按 DataScope 全局可见）。
     */
    public PageResult<TlmScrap> scrapPage(String keyword, String scrapNo, String status,
                                          int page, int size) {
        LambdaQueryWrapper<TlmScrap> w = new LambdaQueryWrapper<>();
        String org = curOrg();
        if (org != null) w.eq(TlmScrap::getOrgId, org);
        if (scrapNo != null && !scrapNo.isBlank()) w.like(TlmScrap::getScrapNo, scrapNo);
        if (status != null && !status.isBlank()) w.eq(TlmScrap::getStatus, status);
        w.orderByDesc(TlmScrap::getCreatedAt);
        IPage<TlmScrap> p = scrapMapper.selectPage(new Page<>(page, size), w);
        // 回填工装编号/名称,便于报废单列表直接展示(避免前端逐行查工装)
        for (TlmScrap s : p.getRecords()) {
            if (s.getToolId() != null) {
                TlmTooling t = toolingMapper.selectById(s.getToolId());
                if (t != null) {
                    s.setToolNo(t.getToolNo());
                    s.setToolName(t.getToolName());
                }
            }
        }
        return new PageResult<TlmScrap>(p.getRecords(), p.getTotal(), page, size);
    }

    /**
     * 维修工单分页查询。支持按工装编号/名称关键词、状态筛选。
     * 仅返回当前组织数据（curOrg 为空时按 DataScope 全局可见）；回填工装编号/名称。
     */
    @Override
    public PageResult<TlmRepair> repairPage(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<TlmRepair> w = new LambdaQueryWrapper<>();
        String org = curOrg();
        if (org != null) w.eq(TlmRepair::getOrgId, org);
        if (status != null && !status.isBlank()) w.eq(TlmRepair::getStatus, status);
        w.orderByDesc(TlmRepair::getCreatedAt);
        IPage<TlmRepair> p = repairMapper.selectPage(new Page<>(page, size), w);
        // 关键词(工装编号/名称)在内存侧过滤: 先回填工装信息再匹配, 避免对 repair 表做 JOIN
        if (keyword != null && !keyword.isBlank()) {
            List<TlmRepair> matched = new java.util.ArrayList<>();
            for (TlmRepair r : p.getRecords()) {
                TlmTooling t = r.getToolId() != null ? toolingMapper.selectById(r.getToolId()) : null;
                if (t != null) {
                    r.setToolNo(t.getToolNo());
                    r.setToolName(t.getToolName());
                    if (t.getToolNo().contains(keyword) || (t.getToolName() != null && t.getToolName().contains(keyword))) {
                        matched.add(r);
                    }
                }
            }
            long total0 = p.getTotal();
            return new PageResult<TlmRepair>(matched, total0, page, size);
        }
        for (TlmRepair r : p.getRecords()) {
            if (r.getToolId() != null) {
                TlmTooling t = toolingMapper.selectById(r.getToolId());
                if (t != null) {
                    r.setToolNo(t.getToolNo());
                    r.setToolName(t.getToolName());
                }
            }
        }
        return new PageResult<TlmRepair>(p.getRecords(), p.getTotal(), page, size);
    }

    /**
     * 计量看板: 统计 GAUGE 器具总数、合格(在期内)、限用预警(到期前30天)、超期。
     * 由后端直接计算, 替代前端按当前页派生(前端派生仅覆盖已加载页, 不准确)。
     */
    @Override
    public java.util.Map<String, Object> metroDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate limitLine = today.plusDays(30);
        List<TlmTooling> gauges = toolingMapper.selectList(
                new LambdaQueryWrapper<TlmTooling>().eq(TlmTooling::getToolCategory, "GAUGE"));
        int total = gauges.size();
        int qualified = 0, limited = 0, overdue = 0;
        for (TlmTooling t : gauges) {
            LocalDate due = t.getCalibDueDate();
            if (due == null) {
                overdue++;            // 未校准视为超期口径
            } else if (due.isBefore(today)) {
                overdue++;
            } else if (!due.isAfter(limitLine)) {
                limited++;            // 30 天内到期 → 限用预警
            } else {
                qualified++;
            }
        }
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("total", total);
        m.put("qualified", qualified);
        m.put("limited", limited);
        m.put("overdue", overdue);
        return m;
    }

    /** 工装-工单绑定记录(含 GAUGE 校准状态快照), 供计量追溯反查。 */
    @Override
    public java.util.List<com.konli.qms.domain.tlm.entity.TlmToolWoBind> bindRecords(String toolId) {
        return bindMapper.selectList(new LambdaQueryWrapper<com.konli.qms.domain.tlm.entity.TlmToolWoBind>()
                .eq(com.konli.qms.domain.tlm.entity.TlmToolWoBind::getToolId, toolId)
                .orderByDesc(com.konli.qms.domain.tlm.entity.TlmToolWoBind::getBoundAt));
    }

    /**
     * 工装维修根因分析聚合(需求 2.5.2.6): 基于 ops.tlm_repair 做三类聚合, 全部带组织隔离。
     * ① faultTypeDist: 各故障类型维修单数 + 占比(柏拉图数据源);
     * ② topTools: 维修频次最高的工装 TOP10(编号/名称/次数);
     * ③ monthlyTrend: 近 12 个月每月维修单数(折线数据源)。
     */
    @Override
    public java.util.Map<String, Object> repairAnalysis(String startDate, String endDate) {
        String org = curOrg();
        String orgCond = (org != null) ? " AND r.org_id = ?" : "";
        java.util.List<Object> args = new java.util.ArrayList<>();
        if (org != null) args.add(org);
        String dateCond = "";
        if (startDate != null && !startDate.isBlank()) {
            dateCond += " AND r.created_at >= ?";
            args.add(startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isBlank()) {
            dateCond += " AND r.created_at <= ?";
            args.add(endDate + " 23:59:59");
        }

        // ① 故障类型分布
        String distSql = "SELECT COALESCE(r.fault_type, '其他') AS fault_type, COUNT(*) AS cnt "
                + "FROM ops.tlm_repair r WHERE 1=1" + orgCond + dateCond
                + " GROUP BY COALESCE(r.fault_type, '其他') ORDER BY cnt DESC";
        java.util.List<java.util.Map<String, Object>> dist = jdbcTemplate.queryForList(distSql, args.toArray());
        long distTotal = 0;
        for (java.util.Map<String, Object> d : dist) {
            Object c = d.get("cnt");
            long cv = (c instanceof Number) ? ((Number) c).longValue() : 0L;
            distTotal += cv;
        }
        for (java.util.Map<String, Object> d : dist) {
            Object c = d.get("cnt");
            long cv = (c instanceof Number) ? ((Number) c).longValue() : 0L;
            d.put("ratio", distTotal > 0 ? Math.round(cv * 1000.0 / distTotal) / 10.0 : 0.0);
        }

        // ② TOP 高频工装(维修次数)
        String topSql = "SELECT t.tool_no AS tool_no, t.tool_name AS tool_name, COUNT(*) AS cnt "
                + "FROM ops.tlm_repair r LEFT JOIN ops.tlm_tooling t ON t.id = r.tool_id::uuid "
                + "WHERE 1=1" + orgCond + dateCond
                + " GROUP BY t.tool_no, t.tool_name ORDER BY cnt DESC LIMIT 10";
        java.util.List<java.util.Map<String, Object>> topTools = jdbcTemplate.queryForList(topSql, args.toArray());

        // ③ 月度趋势(近 12 个月)
        String trendSql = "SELECT TO_CHAR(r.created_at, 'YYYY-MM') AS ym, COUNT(*) AS cnt "
                + "FROM ops.tlm_repair r WHERE 1=1" + orgCond + dateCond
                + " GROUP BY TO_CHAR(r.created_at, 'YYYY-MM') ORDER BY ym";
        java.util.List<java.util.Map<String, Object>> trend = jdbcTemplate.queryForList(trendSql, args.toArray());

        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("total", distTotal);
        m.put("faultTypeDist", dist);
        m.put("topTools", topTools);
        m.put("monthlyTrend", trend);
        return m;
    }
}
