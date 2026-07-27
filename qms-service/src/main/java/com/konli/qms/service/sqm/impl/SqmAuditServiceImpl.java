package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;

import java.util.Objects;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.domain.sqm.entity.SqmAuditApproval;
import com.konli.qms.domain.sqm.entity.SqmAuditNc;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.SqmAuditRecord;
import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import com.konli.qms.domain.sqm.entity.SqmSupplier;
import com.konli.qms.domain.sqm.mapper.SqmAuditApprovalMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditNcMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditPlanMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditRecordMapper;
import com.konli.qms.domain.sqm.mapper.SqmChangeOrderMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierMapper;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.DataScopeGuard;
import com.konli.qms.service.sqm.SqmAuditApprovalCfgService;
import com.konli.qms.service.sqm.SqmAuditReportArchiveService;
import com.konli.qms.service.sqm.SqmAuditService;
import com.konli.qms.service.sqm.dto.AuditorDef;
import com.konli.qms.service.support.CjkFontUtil;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqmAuditServiceImpl implements SqmAuditService {

    private final SqmAuditPlanMapper sqmAuditPlanMapper;
    private final SqmAuditRecordMapper sqmAuditRecordMapper;
    private final SqmAuditNcMapper sqmAuditNcMapper;
    private final SqmChangeOrderMapper sqmChangeOrderMapper;
    private final JdbcTemplate jdbcTemplate;
    private final NcmCapaService ncmCapaService;
    private final SqmSupplierMapper sqmSupplierMapper;
    private final SqmAuditApprovalMapper sqmAuditApprovalMapper;
    private final SqmAuditApprovalCfgService approvalCfgService;
    private final SqmAuditReportArchiveService reportArchiveService;

    @Override
    public List<SqmAuditPlan> listPlans() {
        return sqmAuditPlanMapper.selectList(null);
    }

    @Override
    public List<SqmAuditPlan> listByChangeId(String changeId) {
        if (changeId == null || changeId.isBlank()) {
            return List.of();
        }
        // 兼容 UUID 有无连字符两种格式, 避免历史数据格式不一致导致双向追溯失败
        String stripped = changeId.replace("-", "");
        log.info("[listByChangeId] 收到请求, changeId={}, stripped={}", changeId, stripped);
        List<SqmAuditPlan> result = sqmAuditPlanMapper.selectList(
                new LambdaQueryWrapper<SqmAuditPlan>()
                        .and(w -> w.eq(SqmAuditPlan::getChangeId, changeId)
                                  .or()
                                  .eq(SqmAuditPlan::getChangeId, stripped)));
        log.info("[listByChangeId] 查询结果数量={}, 结果={}", result.size(), result);
        return result;
    }

    @Override
    public List<SqmAuditRecord> listRecords() {
        return sqmAuditRecordMapper.selectList(null);
    }

    @Override
    public List<SqmAuditNc> listNcs() {
        return sqmAuditNcMapper.selectList(null);
    }

    /**
     * 解析当前写入 orgId:优先取登录上下文的 orgId(排除 ROOT 哨兵),否则用 sys_org 首个组织兜底。
     * 解决 AuthServiceImpl 对未分配组织的账号在 JWT 中写入 "ROOT" 哨兵,导致 UUID 列写入失败的问题。
     */
    private String resolveOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u != null && u.orgId() != null && !"ROOT".equals(u.orgId())) {
            return u.orgId();
        }
        try {
            return jdbcTemplate.queryForObject(
                "SELECT id::text FROM ops.sys_org ORDER BY created_at LIMIT 1", String.class);
        } catch (Exception ex) {
            log.warn("resolveOrgId: 无法从 sys_org 取首个组织,orgId 仍为 null", ex);
            return null;
        }
    }

    private String currentUser() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return (u != null && u.username() != null && !u.username().isEmpty()) ? u.username() : "系统";
    }

    @Override
    @Transactional
    public SqmAuditPlan createPlan(SqmAuditPlan plan) {
        plan.setPlanNo("AP-" + System.currentTimeMillis());
        if (plan.getStatus() == null) {
            plan.setStatus("待执行");  // 直接进入待执行,去掉无实际业务动作的"计划中→确认"步骤
        }
        // org_id NOT NULL:优先登录上下文(非 ROOT 哨兵),否则取首个 sys_org 兜底
        if (plan.getOrgId() == null) plan.setOrgId(resolveOrgId());
        sqmAuditPlanMapper.insert(plan);
        // 创建时即惰性生成会签链并回写审核组,避免列表「审核组」栏为空(如年度复审/临时审核未填审核组)
        seedDefaultApprovals(plan);
        return plan;
    }

    /** 独立事务创建审核计划:联动场景(如变更提交联动建审核计划)失败时不回滚调用方主事务。 */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SqmAuditPlan createPlanInNewTx(SqmAuditPlan plan) {
        return createPlan(plan);
    }

    /** @deprecated 新计划创建即"待执行",不再需要确认步骤;旧数据兼容仍可调用 */
    @Override
    @Transactional
    public void confirmPlan(String id) {
        SqmAuditPlan p = sqmAuditPlanMapper.selectById(id);
        if (p == null) throw new BusinessException(404, "审核计划不存在");
        if ("待执行".equals(p.getStatus()) || "进行中".equals(p.getStatus()) || "已完成".equals(p.getStatus()))
            throw new BusinessException(400, "该计划无需确认,已处于" + p.getStatus() + "状态");
        if (!"计划中".equals(p.getStatus()))
            throw new BusinessException(400, "仅计划中状态可确认排期");
        p.setStatus("待执行");
        sqmAuditPlanMapper.updateById(p);
    }

    @Override
    @Transactional
    public SqmAuditPlan startPlan(String id) {
        SqmAuditPlan plan = sqmAuditPlanMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(404, "审核计划不存在");
        }
        DataScopeGuard.ensureOwner(plan.getOrgId());

        // 变更联动审核:变更已通过采购→研发→质量三方审批,审核会签视为已完成,无需再签
        boolean changeLinked = plan.getChangeId() != null && !plan.getChangeId().isBlank();
        if (changeLinked) {
            SqmChangeOrder co = sqmChangeOrderMapper.selectById(plan.getChangeId());
            // 兼容历史数据中无连字符格式, 尝试 stripped 版本
            if (co == null) {
                co = sqmChangeOrderMapper.selectById(plan.getChangeId().replace("-", ""));
            }
            if (co == null)
                throw new BusinessException(404, "关联变更单不存在,无法开始审核");
            if (!"已批准".equals(co.getStatus()))
                throw new BusinessException(409, "关联变更单尚未审批通过(当前状态:" + co.getStatus() + "),变更批准后审核即可开始");
        }

        if (!changeLinked) {
            // 独立审核计划:需审核组全部会签通过后方可开始
            List<SqmAuditApproval> apps = sqmAuditApprovalMapper.selectList(
                new LambdaQueryWrapper<SqmAuditApproval>().eq(SqmAuditApproval::getAuditId, id));
            if (apps.isEmpty()) apps = seedDefaultApprovals(plan);
            boolean allApproved = apps.stream().allMatch(a -> "done".equals(a.getStatus()));
            if (!allApproved) {
                String group = plan.getAuditorTeam() == null || plan.getAuditorTeam().isBlank()
                    ? "质量,采购" : plan.getAuditorTeam();
                throw new BusinessException(409, "需审核组(" + group + ")全部会签通过后方可开始执行");
            }
        }
        // 用已加载 plan 更新(带 @Version 乐观锁)
        plan.setStatus("进行中");
        if (sqmAuditPlanMapper.updateById(plan) == 0) {
            throw new BusinessException(409, "审核计划已被他人修改,请刷新后重试");
        }
        return plan;
    }

    @Override
    public SqmAuditPlan getPlan(String id) {
        SqmAuditPlan plan = sqmAuditPlanMapper.selectById(id);
        if (plan == null) throw new BusinessException(404, "审核计划不存在");
        return plan;
    }

    @Override
    public SqmAuditRecord getRecord(String id) {
        SqmAuditRecord record = sqmAuditRecordMapper.selectById(id);
        if (record == null) throw new BusinessException(404, "审核记录不存在");
        return record;
    }

    @Override
    public List<SqmAuditApproval> listApprovals(String auditId) {
        SqmAuditPlan plan = sqmAuditPlanMapper.selectById(auditId);
        if (plan == null) throw new BusinessException(404, "审核计划不存在");
        LambdaQueryWrapper<SqmAuditApproval> qw = new LambdaQueryWrapper<>();
        qw.eq(SqmAuditApproval::getAuditId, auditId);
        qw.orderByAsc(SqmAuditApproval::getSeqOrder);
        List<SqmAuditApproval> list = sqmAuditApprovalMapper.selectList(qw);
        // 无会签节点,或与当前审核组不一致(历史数据/回填后),且尚无任何人签字时,按当前审核组重建,保证详情与列表一致
        boolean signed = list.stream().anyMatch(a -> "done".equals(a.getStatus()) || "rejected".equals(a.getStatus()));
        if (list.isEmpty() || (!signed && !approvalTeamMatches(list, plan.getAuditorTeam()))) {
            list = seedDefaultApprovals(plan);
        }
        return list;
    }

    @Override
    @Transactional
    public void approve(String auditId, String approvalRole, boolean approved, String opinion) {
        LambdaQueryWrapper<SqmAuditApproval> qw = new LambdaQueryWrapper<>();
        qw.eq(SqmAuditApproval::getAuditId, auditId);
        qw.eq(SqmAuditApproval::getApprovalRole, approvalRole);
        SqmAuditApproval a = sqmAuditApprovalMapper.selectOne(qw);
        if (a == null) throw new BusinessException(404, "审核会签节点不存在: " + approvalRole);
        if ("done".equals(a.getStatus()) || "rejected".equals(a.getStatus())) {
            throw new BusinessException(409, "该节点已会签,不可重复操作");
        }
        a.setStatus(approved ? "done" : "rejected");
        a.setOperator(currentUser());
        a.setOperateDate(LocalDateTime.now());
        a.setOpinion(opinion);
        sqmAuditApprovalMapper.updateById(a);
        // 同步「实际参与审核人」到计划,使审核组栏始终反映真实签字人(与计划时配置的审核组区分)
        syncActualAuditors(auditId);
    }

    /** 把已签字(done/rejected)会签节点的执行人汇总为实际参与人,写入计划 actual_auditors。 */
    private void syncActualAuditors(String auditId) {
        List<SqmAuditApproval> all = sqmAuditApprovalMapper.selectList(
            new LambdaQueryWrapper<SqmAuditApproval>().eq(SqmAuditApproval::getAuditId, auditId));
        String actual = all.stream()
            .filter(x -> x.getOperator() != null && !x.getOperator().isBlank()
                && ("done".equals(x.getStatus()) || "rejected".equals(x.getStatus())))
            .map(SqmAuditApproval::getOperator)
            .distinct()
            .collect(Collectors.joining(","));
        SqmAuditPlan plan = sqmAuditPlanMapper.selectById(auditId);
        if (plan != null) {
            plan.setActualAuditors(actual.isBlank() ? null : actual);
            sqmAuditPlanMapper.updateById(plan);
        }
    }

    /**
     * 惰性初始化会签链:优先使用「审核会签配置」(按审核类型配置的人员与否决权),
     * 找不到配置时兜底解析计划的 auditorTeam。配置中标记 veto 的节点具一票否决权。
     * 仅「物料变更审核」的质量主管默认带否决权,由配置驱动而非写死。
     */
    private List<SqmAuditApproval> seedDefaultApprovals(SqmAuditPlan plan) {
        String orgId = plan.getOrgId() != null ? plan.getOrgId() : resolveOrgId();
        List<AuditorDef> auditors = null;
        try {
            auditors = approvalCfgService.resolve(plan.getAuditType());
        } catch (Exception ignore) {
            auditors = null;
        }
        List<SqmAuditApproval> seeds = new ArrayList<>();
        List<String> teamLabels = new ArrayList<>();
        int seq = 0;
        if (auditors != null && !auditors.isEmpty()) {
            for (AuditorDef m : auditors) {
                // approvalRole 必须是 ASCII 稳定标识(中文在 WHERE 参数比对中会编码失配),
                // roleLabel 保留中文用于展示。
                String role = (m.getRole() == null || m.getRole().isBlank())
                        ? roleCode(m.getLabel(), seq) : m.getRole();
                seeds.add(buildApproval(orgId, plan.getId(), role, m.getLabel(), m.isVeto(), seq));
                teamLabels.add(m.getLabel());
                seq++;
            }
        } else {
            // 兜底:解析 auditorTeam(无否决权)
            List<String> group = parseAuditGroup(plan.getAuditorTeam());
            for (String member : group) {
                seeds.add(buildApproval(orgId, plan.getId(), roleCode(member, seq), member, false, seq));
                teamLabels.add(member);
                seq++;
            }
            if (seeds.isEmpty()) {
                seeds.add(buildApproval(orgId, plan.getId(), "quality", "质量经理", false, 0));
                seeds.add(buildApproval(orgId, plan.getId(), "purchase", "采购主管", false, 1));
                teamLabels.add("质量经理");
                teamLabels.add("采购主管");
            }
        }
        // 先清掉旧节点(重建幂等),保证与当前审核组一致
        sqmAuditApprovalMapper.delete(new LambdaQueryWrapper<SqmAuditApproval>()
                .eq(SqmAuditApproval::getAuditId, plan.getId()));
        seeds.forEach(sqmAuditApprovalMapper::insert);
        // 始终回写审核组,保证列表「审核组」栏不为空且与详情会签链一致(含 auditorTeam 为空的兜底场景)
        String team = teamLabels.stream().collect(Collectors.joining(","));
        if (!team.isBlank() && !team.equals(plan.getAuditorTeam())) {
            plan.setAuditorTeam(team);
            sqmAuditPlanMapper.updateById(plan);
        }
        return seeds;
    }

    /** 现有会签节点标签是否与当前审核组(auditorTeam)一致。 */
    private boolean approvalTeamMatches(List<SqmAuditApproval> list, String auditorTeam) {
        List<String> team = parseAuditGroup(auditorTeam);
        if (team.isEmpty()) return list.isEmpty();
        List<String> labels = list.stream().map(SqmAuditApproval::getRoleLabel)
                .filter(Objects::nonNull).collect(Collectors.toList());
        return labels.size() == team.size() && labels.containsAll(team) && team.containsAll(labels);
    }

    /**
     * 配置变更后,按最新「审核人员配置」重建该审核类型下计划的会签链并回写审核组。
     * 仅对「待执行」且尚无任何签字记录(未冻结)的计划生效;已签字/已执行的计划会签链保持冻结,不改动。
     */
    @Override
    public void resetApprovalsForType(String auditType) {
        List<SqmAuditPlan> plans = sqmAuditPlanMapper.selectList(
                new LambdaQueryWrapper<SqmAuditPlan>().eq(SqmAuditPlan::getAuditType, auditType));
        for (SqmAuditPlan p : plans) {
            if (!"待执行".equals(p.getStatus())) continue;
            long signed = sqmAuditApprovalMapper.selectCount(new LambdaQueryWrapper<SqmAuditApproval>()
                    .eq(SqmAuditApproval::getAuditId, p.getId())
                    .in(SqmAuditApproval::getStatus, "done", "rejected"));
            if (signed > 0) continue;
            sqmAuditApprovalMapper.delete(new LambdaQueryWrapper<SqmAuditApproval>()
                    .eq(SqmAuditApproval::getAuditId, p.getId()));
            seedDefaultApprovals(p);
        }
    }

    /** 将 "质量,采购,研发" 等审核组字符串解析为成员列表。 */
    private List<String> parseAuditGroup(String team) {
        List<String> out = new ArrayList<>();
        if (team == null || team.isBlank()) return out;
        for (String part : team.split("[,，、/]")) {
            String t = part.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    /** 生成 ASCII 稳定的会签角色码:纯英文成员直接小写,含中文则用 r+序号兜底。 */
    private String roleCode(String member, int index) {
        String ascii = member.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        return ascii.isEmpty() ? ("r" + index) : ascii;
    }

    private SqmAuditApproval buildApproval(String orgId, String auditId, String role,
                                           String label, boolean veto, int seq) {
        SqmAuditApproval a = new SqmAuditApproval();
        a.setOrgId(orgId);
        a.setAuditId(auditId);
        a.setApprovalRole(role);
        a.setRoleLabel(label);
        a.setStatus("pending");
        a.setHasVeto(veto);
        a.setSeqOrder(seq);
        return a;
    }

    @Override
    @Transactional
    public SqmAuditRecord createRecord(SqmAuditRecord record) {
        record.setRecordNo("AR-" + System.currentTimeMillis());
        if (record.getStatus() == null) {
            record.setStatus("已完成");
        }
        if (record.getNcCount() == null) {
            record.setNcCount(0);
        }
        // NOT NULL 列兜底:从计划继承 supplier_id/audit_type/audit_date/audit_lead/auditor_team
        if (record.getOrgId() == null) record.setOrgId(resolveOrgId());
        if (record.getPlanId() != null) {
            SqmAuditPlan plan = sqmAuditPlanMapper.selectById(record.getPlanId());
            if (plan != null) {
                if (record.getSupplierId() == null && plan.getSupplierId() != null)
                    record.setSupplierId(plan.getSupplierId());
                if (record.getAuditType() == null && plan.getAuditType() != null)
                    record.setAuditType(plan.getAuditType());
                if (record.getAuditDate() == null && plan.getActualDate() != null)
                    record.setAuditDate(plan.getActualDate());
                if (record.getAuditDate() == null)
                    record.setAuditDate(java.time.LocalDate.now());
                if (record.getAuditLead() == null && plan.getAuditLead() != null)
                    record.setAuditLead(plan.getAuditLead());
                if (record.getAuditorTeam() == null && plan.getActualAuditors() != null)
                    record.setAuditorTeam(plan.getActualAuditors());
            }
        }
        if (record.getResult() == null) {
            String c = record.getConclusion();
            if (c == null) record.setResult("通过");
            else if ("推荐通过".equals(c)) record.setResult("通过");
            else record.setResult(c);
        }
        sqmAuditRecordMapper.insert(record);
        // 供应商审核→等级联动:审核得分自动更新供应商等级(A/B/C/D)
        if (record.getSupplierId() != null && record.getScore() != null) {
            try {
                SqmSupplier sup = sqmSupplierMapper.selectById(record.getSupplierId());
                if (sup != null) {
                    // 用已加载 sup 更新(带 @Version 乐观锁);联动失败不阻断审核记录创建
                    BigDecimal s = record.getScore();
                    if (s.compareTo(BigDecimal.valueOf(90)) >= 0) sup.setLevel("A");
                    else if (s.compareTo(BigDecimal.valueOf(75)) >= 0) sup.setLevel("B");
                    else if (s.compareTo(BigDecimal.valueOf(60)) >= 0) sup.setLevel("C");
                    else sup.setLevel("D");
                    sup.setScore(s);
                    sup.setLastAuditDate(LocalDate.now());
                    sqmSupplierMapper.updateById(sup);
                }
            } catch (Exception ignored) {}
        }
        // 提交审核报告后,把对应计划状态推进为「已完成」(状态机收尾)
        if (record.getPlanId() != null) {
            SqmAuditPlan plan = sqmAuditPlanMapper.selectById(record.getPlanId());
            if (plan != null && !"已完成".equals(plan.getStatus())) {
                plan.setStatus("已完成");
                sqmAuditPlanMapper.updateById(plan);
            }
        }
        // 流程走完(记录已提交、计划已完成)后自动归档,无需手动点击「生成归档」
        final String rid = record.getId();
        if (rid != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        reportArchiveService.generatePdf(rid);
                    } catch (Exception e) {
                        log.warn("SQM 审核记录自动归档失败, recordId={}: {}", rid, e.getMessage());
                    }
                }
            });
        }
        return record;
    }

    @Override
    @Transactional
    public SqmAuditNc createNc(SqmAuditNc nc) {
        nc.setNcNo("NC-" + System.currentTimeMillis());
        if (nc.getStatus() == null) {
            nc.setStatus("待整改");
        }
        // org_id NOT NULL:优先登录上下文(非 ROOT 哨兵),否则取首个 sys_org 兜底
        if (nc.getOrgId() == null) nc.setOrgId(resolveOrgId());
        sqmAuditNcMapper.insert(nc);
        // 审计NC→CAPA联动:严重不符合项自动创建CAPA
        if ("严重".equals(nc.getLevel())) {
            try {
                QmsCapa capa = new QmsCapa();
                capa.setOrgId(nc.getOrgId());
                capa.setIssue("审核严重不符合项:" + nc.getNcNo() + " " + (nc.getDescription() != null ? nc.getDescription().substring(0, Math.min(30, nc.getDescription().length())) : ""));
                capa.setTriggerType("内审");
                capa.setSourceRefId(nc.getId());
                capa.setSourceType("审核不符合项");
                capa.setCapaType("纠正措施");
                capa.setOwner("质量经理");
                capa.setDueDate(LocalDate.now().plusDays(30));
                ncmCapaService.createInNewTx(capa);
            } catch (Exception ignored) {}
        }
        return nc;
    }

    @Override
    @Transactional
    public void closeNc(String ncId, String verifyResult, String verifyComment) {
        SqmAuditNc nc = sqmAuditNcMapper.selectById(ncId);
        if (nc == null) {
            throw new BusinessException(404, "审核不符合项不存在");
        }
        DataScopeGuard.ensureOwner(nc.getOrgId());
        // 用已加载 nc 更新(带 @Version 乐观锁)
        nc.setVerifyResult(verifyResult);
        nc.setVerifyComment(verifyComment);
        nc.setVerifyDate(LocalDateTime.now());
        nc.setStatus("已闭环");
        nc.setCloseDate(LocalDateTime.now());
        if (sqmAuditNcMapper.updateById(nc) == 0) {
            throw new BusinessException(409, "审核不符合项已被他人修改,请刷新后重试");
        }
    }

    /**
     * 生成审核报告 PDF(openhtmltopdf)。
     * 查 sqm_audit_record + sqm_audit_nc(by recordId),构建 HTML 渲染为 PDF 返回 byte[]。
     * 渲染失败抛 BusinessException,由 GlobalExceptionHandler 转 R<T>。
     */
    @Override
    public byte[] generateReport(String recordId) {
        SqmAuditRecord record = sqmAuditRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "审核记录不存在");
        }
        List<SqmAuditNc> ncs = sqmAuditNcMapper.selectList(
                new LambdaQueryWrapper<SqmAuditNc>()
                        .eq(SqmAuditNc::getRecordId, recordId)
                        .orderByAsc(SqmAuditNc::getNcNo));
        try {
            String html = buildReportHtml(record, ncs);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // 注册中文字体,避免报告中文显示为方块/乱码
            CjkFontUtil.register(builder);
            builder.withHtmlContent(html, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            log.warn("SQM 审核报告 PDF 生成失败, recordId={}: {}", recordId, e.getMessage(), e);
            throw new BusinessException(500, "审核报告 PDF 生成失败: " + e.getMessage());
        }
    }

    /** 构建审核报告 HTML(内联样式,不依赖外部 CSS;openhtmltopdf 渲染)。 */
    private String buildReportHtml(SqmAuditRecord record, List<SqmAuditNc> ncs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><style>")
          .append("body{font-family:'SimHei',sans-serif;font-size:12px;color:#000;margin:24px;}")
          .append("h1{text-align:center;font-size:20px;margin:8px 0 16px;}")
          .append("h2{font-size:14px;margin:12px 0 4px;border-left:4px solid #333;padding-left:6px;}")
          .append("table{border-collapse:collapse;width:100%;margin:4px 0;}")
          .append("th,td{border:1px solid #333;padding:4px 6px;text-align:left;font-size:11px;}")
          .append("th{background:#eee;}")
          .append(".info{margin:2px 0;}")
          .append(".label{display:inline-block;width:110px;font-weight:bold;}")
          .append("</style></head><body>");
        // 标题
        sb.append("<h1>供应商审核报告</h1>");
        // 审核记录信息
        sb.append("<h2>审核记录信息</h2>");
        sb.append("<table>");
        appendRow(sb, "记录编号", str(record.getRecordNo()), "审核类型", str(record.getAuditType()));
        appendRow(sb, "审核日期", str(record.getAuditDate()), "审核组长", str(record.getAuditLead()));
        appendRow(sb, "审核组", str(record.getAuditorTeam()), "审核结果", str(record.getResult()));
        appendRow(sb, "审核得分", str(record.getScore()), "NC 数量", str(record.getNcCount()));
        appendRow(sb, "审核结论", str(record.getConclusion()), "状态", str(record.getStatus()));
        sb.append("</table>");
        // NC 列表
        sb.append("<h2>不符合项列表</h2>");
        if (ncs == null || ncs.isEmpty()) {
            sb.append("<p>无不符合项</p>");
        } else {
            sb.append("<table><tr><th>序号</th><th>NC 编号</th><th>条款</th><th>描述</th><th>级别</th><th>状态</th><th>责任人</th><th>整改措施</th><th>验证结论</th></tr>");
            int seq = 1;
            for (SqmAuditNc nc : ncs) {
                sb.append("<tr>")
                  .append("<td>").append(seq++).append("</td>")
                  .append("<td>").append(str(nc.getNcNo())).append("</td>")
                  .append("<td>").append(str(nc.getClause())).append("</td>")
                  .append("<td>").append(str(nc.getDescription())).append("</td>")
                  .append("<td>").append(str(nc.getLevel())).append("</td>")
                  .append("<td>").append(str(nc.getStatus())).append("</td>")
                  .append("<td>").append(str(nc.getResponsible())).append("</td>")
                  .append("<td>").append(str(nc.getRectifyMeasure())).append("</td>")
                  .append("<td>").append(str(nc.getVerifyResult())).append("</td>")
                  .append("</tr>");
            }
            sb.append("</table>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, String l1, String v1, String l2, String v2) {
        sb.append("<tr><td><b>").append(l1).append("</b></td><td>").append(v1).append("</td>")
          .append("<td><b>").append(l2).append("</b></td><td>").append(v2).append("</td></tr>");
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    /** SR-SQA:每天8:00扫描审核NC超期(deadline已过未闭环)->升级通知采购+质量经理 */
    @Scheduled(cron = "0 10 8 * * ?")
    public void scanNcOverdue() {
        try {
            List<SqmAuditNc> ncs = sqmAuditNcMapper.selectList(
                    new LambdaQueryWrapper<SqmAuditNc>()
                            .lt(SqmAuditNc::getDeadline, LocalDate.now())
                            .ne(SqmAuditNc::getStatus, "已关闭"));
            for (SqmAuditNc nc : ncs) {
                if (nc.getDeadline() == null) continue;
                long days = ChronoUnit.DAYS.between(nc.getDeadline(), LocalDate.now());
                jdbcTemplate.update(
                        "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) VALUES (?::uuid, 'AUDIT_NC_OVERDUE', ?, '站内', '采购,质量经理', ?, '告警', '已发送', now())",
                        java.util.UUID.fromString("019f701f-0411-71ed-9eac-ab9440335832"),
                        nc.getId(),
                        "审核不符合项 " + nc.getNcNo() + " 整改超期" + days + "天,已升级通知");
            }
        } catch (Exception e) { log.warn("NC超期扫描异常: {}", e.getMessage()); }
    }
}
