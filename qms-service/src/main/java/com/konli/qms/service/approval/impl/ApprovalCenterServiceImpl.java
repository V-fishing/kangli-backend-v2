package com.konli.qms.service.approval.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.dto.PendingApprovalDTO;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaApproval;
import com.konli.qms.domain.fia.mapper.FiaApprovalMapper;
import com.konli.qms.domain.ncm.entity.Qms8dApprovalConfig;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.entity.Qms8dStageDetail;
import com.konli.qms.domain.ncm.mapper.Qms8dApprovalConfigMapper;
import com.konli.qms.domain.ncm.mapper.Qms8dReportMapper;
import com.konli.qms.domain.ncm.mapper.Qms8dStageDetailMapper;
import com.konli.qms.domain.sqm.entity.SqmAuditApproval;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.SqmChangeApproval;
import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import com.konli.qms.domain.sqm.mapper.SqmAuditApprovalMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditPlanMapper;
import com.konli.qms.domain.sqm.mapper.SqmChangeApprovalMapper;
import com.konli.qms.domain.sqm.mapper.SqmChangeOrderMapper;
import com.konli.qms.domain.tlm.entity.TlmScrap;
import com.konli.qms.domain.tlm.entity.TlmRepair;
import com.konli.qms.domain.tlm.mapper.TlmScrapMapper;
import com.konli.qms.domain.tlm.mapper.TlmRepairMapper;
import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.service.approval.ApprovalCenterService;
import com.konli.qms.service.uop.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通用审批中心实现:跨模块聚合当前用户的待审批事项。
 *
 * <p>匹配规则:
 * <ul>
 *   <li>FIA 首件审批: 指定审批人(approver_id) == 当前用户 且 status=待审批。</li>
 *   <li>NCM 8D 阶段审批: 阶段明细 approval_status=待审批,且阶段签批配置(signer) == 当前用户。</li>
 *   <li>SQM 物料变更/供应商审核会签: status=pending,且 approval_role 命中当前用户角色(quality/purchase/rd/trial)。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ApprovalCenterServiceImpl implements ApprovalCenterService {

    private final FiaApprovalMapper fiaApprovalMapper;
    private final Qms8dStageDetailMapper stageDetailMapper;
    private final Qms8dApprovalConfigMapper approvalConfigMapper;
    private final Qms8dReportMapper reportMapper;
    private final SqmChangeApprovalMapper changeApprovalMapper;
    private final SqmChangeOrderMapper changeOrderMapper;
    private final SqmAuditApprovalMapper auditApprovalMapper;
    private final SqmAuditPlanMapper auditPlanMapper;
    private final TlmScrapMapper tlmScrapMapper;
    private final TlmRepairMapper tlmRepairMapper;
    private final UserService userService;

    @Override
    public List<PendingApprovalDTO> myPending() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null) {
            return List.of();
        }
        String userId = u.userId();
        String orgId = u.orgId();
        // 跨公司管理员 JWT orgId="ROOT"(非 uuid),不能用于 uuid 列过滤,此时跳过组织过滤
        boolean hasOrg = orgId != null && !orgId.isBlank() && !"ROOT".equals(orgId);
        List<PendingApprovalDTO> list = new ArrayList<>();

        // 1) FIA 首件审批(指定审批人)
        List<FiaApproval> fiaList = fiaApprovalMapper.selectList(new LambdaQueryWrapper<FiaApproval>()
                .eq(hasOrg, FiaApproval::getOrgId, orgId)
                .eq(FiaApproval::getApproverId, userId)
                .eq(FiaApproval::getStatus, "待审批"));
        for (FiaApproval a : fiaList) {
            PendingApprovalDTO d = new PendingApprovalDTO();
            d.setId(a.getId());
            d.setModule("FIA");
            d.setBizType(a.getApprovalType());
            d.setBizNo(a.getWoNo());
            d.setTitle((a.getApprovalType() == null ? "首件审批" : a.getApprovalType())
                    + " · " + (a.getWoNo() == null ? "" : a.getWoNo()));
            d.setApplicant(a.getApplicantId());
            d.setAppliedAt(a.getApplyAt());
            d.setUrl("/fia/approvals");
            list.add(d);
        }

        // 2) NCM 8D 阶段审批(按签批人配置)
        List<Qms8dApprovalConfig> configs = approvalConfigMapper.selectList(
                new LambdaQueryWrapper<Qms8dApprovalConfig>()
                        .and(hasOrg, w -> w.eq(Qms8dApprovalConfig::getOrgId, orgId)
                                .or().eq(Qms8dApprovalConfig::getOrgId, "ROOT"))
                        .eq(Qms8dApprovalConfig::getNeedApproval, true));
        Set<String> myStages = configs.stream()
                .filter(c -> isAmongSigners(c.getSigner(), userId, u.username()))
                .map(Qms8dApprovalConfig::getStageCode)
                .collect(Collectors.toSet());
        if (!myStages.isEmpty()) {
            List<Qms8dStageDetail> stages = stageDetailMapper.selectList(
                    new LambdaQueryWrapper<Qms8dStageDetail>()
                            .eq(hasOrg, Qms8dStageDetail::getOrgId, orgId)
                            .eq(Qms8dStageDetail::getApprovalStatus, "待审批")
                            .in(Qms8dStageDetail::getStageCode, myStages));
            Set<String> d8Ids = stages.stream().map(Qms8dStageDetail::getD8Id)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            Map<String, Qms8dReport> reportMap = d8Ids.isEmpty() ? Map.of() :
                    reportMapper.selectList(new LambdaQueryWrapper<Qms8dReport>().in(Qms8dReport::getId, d8Ids))
                            .stream().collect(Collectors.toMap(Qms8dReport::getId, r -> r));
            for (Qms8dStageDetail s : stages) {
                Qms8dReport r = reportMap.get(s.getD8Id());
                // 8D 报告已闭环的不再作为待审批展示
                if (r != null && "已闭环".equals(r.getStatus())) {
                    continue;
                }
                PendingApprovalDTO d = new PendingApprovalDTO();
                d.setId(s.getId());
                d.setModule("NCM");
                d.setBizType("8D阶段审批");
                d.setBizNo(r == null ? s.getD8Id() : r.getD8No());
                d.setTitle("8D " + s.getStageCode() + " 阶段审批 · " + (r == null ? "" : r.getD8No()));
                d.setApplicant(s.getOwner());
                d.setAppliedAt(s.getCreatedAt());
                d.setUrl("/ncm/8d-reports/" + s.getD8Id());
                list.add(d);
            }
        }

        // 3)+4) SQM 物料变更会签 / 供应商审核会签(按角色匹配)
        Set<String> myRoles = resolveApprovalRoles(userId);
        if (!myRoles.isEmpty()) {
            // 物料变更会签
            List<SqmChangeApproval> cas = changeApprovalMapper.selectList(
                    new LambdaQueryWrapper<SqmChangeApproval>()
                            .eq(hasOrg, SqmChangeApproval::getOrgId, orgId)
                            .eq(SqmChangeApproval::getStatus, "pending")
                            .in(SqmChangeApproval::getApprovalRole, myRoles));
            Set<String> changeIds = cas.stream().map(SqmChangeApproval::getChangeId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            Map<String, SqmChangeOrder> coMap = changeIds.isEmpty() ? Map.of() :
                    changeOrderMapper.selectList(new LambdaQueryWrapper<SqmChangeOrder>().in(SqmChangeOrder::getId, changeIds))
                            .stream().collect(Collectors.toMap(SqmChangeOrder::getId, c -> c));
            for (SqmChangeApproval ca : cas) {
                SqmChangeOrder co = coMap.get(ca.getChangeId());
                PendingApprovalDTO d = new PendingApprovalDTO();
                d.setId(ca.getId());
                d.setModule("SQM");
                d.setBizType("物料变更会签");
                d.setBizNo(co == null ? ca.getChangeId() : co.getChangeNo());
                d.setTitle("物料变更会签(" + roleLabel(ca.getRoleLabel(), ca.getApprovalRole()) + ") · "
                        + (co == null ? "" : co.getChangeNo()));
                d.setApplicant(co == null ? null : co.getApplicant());
                d.setAppliedAt(co == null || co.getApplyDate() == null ? null : co.getApplyDate().atStartOfDay());
                d.setUrl("/sqm/changes");
                list.add(d);
            }

            // 供应商审核会签
            List<SqmAuditApproval> aas = auditApprovalMapper.selectList(
                    new LambdaQueryWrapper<SqmAuditApproval>()
                            .eq(hasOrg, SqmAuditApproval::getOrgId, orgId)
                            .eq(SqmAuditApproval::getStatus, "pending")
                            .in(SqmAuditApproval::getApprovalRole, myRoles));
            Set<String> auditIds = aas.stream().map(SqmAuditApproval::getAuditId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            Map<String, SqmAuditPlan> apMap = auditIds.isEmpty() ? Map.of() :
                    auditPlanMapper.selectList(new LambdaQueryWrapper<SqmAuditPlan>().in(SqmAuditPlan::getId, auditIds))
                            .stream().collect(Collectors.toMap(SqmAuditPlan::getId, p -> p));
            for (SqmAuditApproval aa : aas) {
                SqmAuditPlan ap = apMap.get(aa.getAuditId());
                PendingApprovalDTO d = new PendingApprovalDTO();
                d.setId(aa.getId());
                d.setModule("SQM");
                d.setBizType("供应商审核会签");
                d.setBizNo(ap == null ? aa.getAuditId() : ap.getPlanNo());
                d.setTitle("供应商审核会签(" + roleLabel(aa.getRoleLabel(), aa.getApprovalRole()) + ") · "
                        + (ap == null ? "" : ap.getPlanNo()));
                d.setApplicant(ap == null ? null : ap.getAuditLead());
                d.setAppliedAt(ap == null || ap.getPlanDate() == null ? null : ap.getPlanDate().atStartOfDay());
                d.setUrl("/sqm/audits");
                list.add(d);
            }
        }

        // 5) TLM 工装报废审批(指定审批人 approver_id == 当前用户 且 status=PENDING)
        List<TlmScrap> scraps = tlmScrapMapper.selectList(new LambdaQueryWrapper<TlmScrap>()
                .eq(hasOrg, TlmScrap::getOrgId, orgId)
                .eq(TlmScrap::getApproverId, userId)
                .eq(TlmScrap::getStatus, "PENDING"));
        for (TlmScrap s : scraps) {
            PendingApprovalDTO d = new PendingApprovalDTO();
            d.setId(s.getId());
            d.setModule("TLM");
            d.setBizType("工装报废审批");
            d.setBizNo(s.getScrapNo());
            d.setTitle("工装报废审批 · " + (s.getScrapNo() == null ? "" : s.getScrapNo()));
            d.setApplicant(s.getCreatedBy());
            d.setAppliedAt(s.getCreatedAt());
            d.setUrl("/tlm/tooling/" + s.getToolId());
            list.add(d);
        }

        // 6) TLM 工装维修审批(指定审批人 approver_id == 当前用户 且 status=PENDING)
        List<TlmRepair> repairs = tlmRepairMapper.selectList(new LambdaQueryWrapper<TlmRepair>()
                .eq(hasOrg, TlmRepair::getOrgId, orgId)
                .eq(TlmRepair::getApproverId, userId)
                .eq(TlmRepair::getStatus, "PENDING"));
        for (TlmRepair r : repairs) {
            PendingApprovalDTO d = new PendingApprovalDTO();
            d.setId(r.getId());
            d.setModule("TLM");
            d.setBizType("工装维修审批");
            d.setBizNo(r.getRepairNo());
            d.setTitle("工装维修审批 · " + (r.getRepairNo() == null ? "" : r.getRepairNo()));
            d.setApplicant(r.getCreatedBy());
            d.setAppliedAt(r.getCreatedAt());
            d.setUrl("/tlm/tooling/" + r.getToolId());
            list.add(d);
        }

        list.sort((x, y) -> {
            LocalDateTime xa = x.getAppliedAt();
            LocalDateTime ya = y.getAppliedAt();
            if (xa == null) return 1;
            if (ya == null) return -1;
            return ya.compareTo(xa);
        });
        return list;
    }

    private String roleLabel(String roleLabel, String approvalRole) {
        if (roleLabel != null && !roleLabel.isBlank()) {
            return roleLabel;
        }
        return approvalRole == null ? "" : approvalRole;
    }

    /** 根据当前用户角色解析其可审批的 SQM 会签角色(quality/purchase/rd/trial)。 */
    private Set<String> resolveApprovalRoles(String userId) {
        Set<String> roles = new HashSet<>();
        try {
            List<SysRole> userRoles = userService.getRoles(userId);
            for (SysRole r : userRoles) {
                String s = ((r.getRoleCode() == null ? "" : r.getRoleCode()) + " "
                        + (r.getRoleName() == null ? "" : r.getRoleName())).toLowerCase();
                if (s.contains("quality") || s.contains("质量")) roles.add("quality");
                if (s.contains("purchase") || s.contains("采购")) roles.add("purchase");
                if (s.contains("rd") || s.contains("研发")) roles.add("rd");
                if (s.contains("trial") || s.contains("试产")) roles.add("trial");
            }
        } catch (Exception ignored) {
            // 角色解析失败不阻断审批中心
        }
        return roles;
    }

    /**
     * 判断当前用户是否在被指定的签批人列表中(OR 语义:任一命中即可签)。
     * signer 字段支持逗号分隔的多人(userId 或 username 均可,兼容历史单 username 数据)。
     */
    private boolean isAmongSigners(String signer, String userId, String username) {
        if (signer == null || signer.isBlank()) return false;
        String uId = userId == null ? "" : userId.trim();
        String uName = username == null ? "" : username.trim();
        for (String s : signer.split(",")) {
            String t = s.trim();
            if (t.isEmpty()) continue;
            if (!uId.isEmpty() && t.equalsIgnoreCase(uId)) return true;
            if (!uName.isEmpty() && t.equalsIgnoreCase(uName)) return true;
        }
        return false;
    }
}
