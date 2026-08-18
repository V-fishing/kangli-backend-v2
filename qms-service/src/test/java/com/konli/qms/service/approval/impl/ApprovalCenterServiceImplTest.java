package com.konli.qms.service.approval.impl;

import com.konli.qms.common.dto.PendingApprovalDTO;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaApproval;
import com.konli.qms.domain.ncm.entity.Qms8dApprovalConfig;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.entity.Qms8dStageDetail;
import com.konli.qms.domain.sqm.entity.SqmAuditApproval;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.SqmChangeApproval;
import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import com.konli.qms.domain.tlm.entity.TlmRepair;
import com.konli.qms.domain.tlm.entity.TlmScrap;
import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.service.uop.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 通用审批中心服务单元测试（M11 approval，Mockito + CompanyContext 注入）。
 * 覆盖：无登录用户返回空、FIA 指定审批人分支、NCM 8D 签批人分支(已闭环跳过)、
 * SQM 变更/审核会签(角色匹配)、TLM 报废/维修分支、时间倒序+null 置后排序、ROOT 管理员跳过 org 过滤。
 */
@ExtendWith(MockitoExtension.class)
class ApprovalCenterServiceImplTest {

    @Mock com.konli.qms.domain.fia.mapper.FiaApprovalMapper fiaApprovalMapper;
    @Mock com.konli.qms.domain.ncm.mapper.Qms8dStageDetailMapper stageDetailMapper;
    @Mock com.konli.qms.domain.ncm.mapper.Qms8dApprovalConfigMapper approvalConfigMapper;
    @Mock com.konli.qms.domain.ncm.mapper.Qms8dReportMapper reportMapper;
    @Mock com.konli.qms.domain.sqm.mapper.SqmChangeApprovalMapper changeApprovalMapper;
    @Mock com.konli.qms.domain.sqm.mapper.SqmChangeOrderMapper changeOrderMapper;
    @Mock com.konli.qms.domain.sqm.mapper.SqmAuditApprovalMapper auditApprovalMapper;
    @Mock com.konli.qms.domain.sqm.mapper.SqmAuditPlanMapper auditPlanMapper;
    @Mock com.konli.qms.domain.tlm.mapper.TlmScrapMapper tlmScrapMapper;
    @Mock com.konli.qms.domain.tlm.mapper.TlmRepairMapper tlmRepairMapper;
    @Mock UserService userService;

    @InjectMocks ApprovalCenterServiceImpl service;

    @AfterEach
    void clear() {
        CompanyContext.clear();
    }

    private void login(String userId, String orgId) {
        CompanyContext.set(new CompanyContext.CurrentUser(userId, "u" + userId, orgId, "org"));
    }

    @Test
    @DisplayName("无登录用户:返回空列表")
    void noUser_returnsEmpty() {
        assertThat(service.myPending()).isEmpty();
    }

    @Test
    @DisplayName("FIA 分支:approverId 匹配且待审批 → 映射 module=FIA/bizType/url")
    void fia_branch_mapsFields() {
        login("U1", "MZ");
        FiaApproval a = new FiaApproval();
        a.setId("fa-1");
        a.setApprovalType("豁免");
        a.setWoNo("WO-99");
        a.setApplicantId("applicant-1");
        a.setApplyAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        when(fiaApprovalMapper.selectList(any())).thenReturn(List.of(a));

        List<PendingApprovalDTO> list = service.myPending();
        assertThat(list).hasSize(1);
        PendingApprovalDTO d = list.get(0);
        assertThat(d.getModule()).isEqualTo("FIA");
        assertThat(d.getBizType()).isEqualTo("豁免");
        assertThat(d.getBizNo()).isEqualTo("WO-99");
        assertThat(d.getTitle()).isEqualTo("豁免 · WO-99");
        assertThat(d.getApplicant()).isEqualTo("applicant-1");
        assertThat(d.getUrl()).isEqualTo("/fia/approvals");
    }

    @Test
    @DisplayName("NCM 8D 分支:signer 命中且无闭环 → 映射;报告已闭环 → 跳过")
    void ncm_branch_closedSkipped() {
        login("signer-1", "MZ");
        Qms8dApprovalConfig cfg = new Qms8dApprovalConfig();
        cfg.setStageCode("D1");
        cfg.setSigner("signer-1");
        cfg.setNeedApproval(true);
        cfg.setOrgId("MZ");
        when(approvalConfigMapper.selectList(any())).thenReturn(List.of(cfg));

        Qms8dStageDetail open = new Qms8dStageDetail();
        open.setId("st-1");
        open.setD8Id("d8-1");
        open.setStageCode("D1");
        open.setApprovalStatus("待审批");
        open.setOwner("owner-1");
        open.setCreatedAt(LocalDateTime.of(2026, 2, 1, 10, 0));

        Qms8dStageDetail closed = new Qms8dStageDetail();
        closed.setId("st-2");
        closed.setD8Id("d8-2");
        closed.setStageCode("D1");
        closed.setApprovalStatus("待审批");
        when(stageDetailMapper.selectList(any())).thenReturn(List.of(open, closed));

        Qms8dReport rOpen = new Qms8dReport();
        rOpen.setId("d8-1");
        rOpen.setD8No("8D-001");
        rOpen.setStatus("进行中");
        Qms8dReport rClosed = new Qms8dReport();
        rClosed.setId("d8-2");
        rClosed.setD8No("8D-002");
        rClosed.setStatus("已闭环");
        when(reportMapper.selectList(any())).thenReturn(List.of(rOpen, rClosed));

        List<PendingApprovalDTO> list = service.myPending();
        assertThat(list).hasSize(1);
        PendingApprovalDTO d = list.get(0);
        assertThat(d.getModule()).isEqualTo("NCM");
        assertThat(d.getBizType()).isEqualTo("8D阶段审批");
        assertThat(d.getBizNo()).isEqualTo("8D-001");
        assertThat(d.getTitle()).isEqualTo("8D D1 阶段审批 · 8D-001");
        assertThat(d.getApplicant()).isEqualTo("owner-1");
        assertThat(d.getUrl()).isEqualTo("/ncm/8d-reports/d8-1");
    }

    @Test
    @DisplayName("SQM 变更会签:角色 quality 命中 → 映射 changeNo/roleLabel")
    void sqm_change_branch_roleMatch() {
        login("U1", "MZ");
        SysRole role = new SysRole();
        role.setRoleCode("sqe");
        role.setRoleName("SQE质量工程师");
        when(userService.getRoles("U1")).thenReturn(List.of(role));

        SqmChangeApproval ca = new SqmChangeApproval();
        ca.setId("ca-1");
        ca.setChangeId("co-1");
        ca.setApprovalRole("quality");
        ca.setRoleLabel("质量会签");
        when(changeApprovalMapper.selectList(any())).thenReturn(List.of(ca));

        SqmChangeOrder co = new SqmChangeOrder();
        co.setId("co-1");
        co.setChangeNo("ECN-1");
        co.setApplicant("applicant-co");
        co.setApplyDate(LocalDate.of(2026, 3, 1));
        when(changeOrderMapper.selectList(any())).thenReturn(List.of(co));

        List<PendingApprovalDTO> list = service.myPending();
        assertThat(list).hasSize(1);
        PendingApprovalDTO d = list.get(0);
        assertThat(d.getModule()).isEqualTo("SQM");
        assertThat(d.getBizType()).isEqualTo("物料变更会签");
        assertThat(d.getBizNo()).isEqualTo("ECN-1");
        assertThat(d.getTitle()).contains("质量会签").contains("ECN-1");
        assertThat(d.getApplicant()).isEqualTo("applicant-co");
        assertThat(d.getUrl()).isEqualTo("/sqm/changes");
    }

    @Test
    @DisplayName("SQM 审核会签:角色 purchase 命中 → 映射 planNo")
    void sqm_audit_branch_roleMatch() {
        login("U1", "MZ");
        SysRole role = new SysRole();
        role.setRoleCode("buyer");
        role.setRoleName("采购专员");
        when(userService.getRoles("U1")).thenReturn(List.of(role));

        SqmAuditApproval aa = new SqmAuditApproval();
        aa.setId("aa-1");
        aa.setAuditId("ap-1");
        aa.setApprovalRole("purchase");
        aa.setRoleLabel("采购会签");
        when(auditApprovalMapper.selectList(any())).thenReturn(List.of(aa));

        SqmAuditPlan ap = new SqmAuditPlan();
        ap.setId("ap-1");
        ap.setPlanNo("AUD-1");
        ap.setAuditLead("lead-1");
        ap.setPlanDate(LocalDate.of(2026, 4, 1));
        when(auditPlanMapper.selectList(any())).thenReturn(List.of(ap));

        List<PendingApprovalDTO> list = service.myPending();
        assertThat(list).hasSize(1);
        PendingApprovalDTO d = list.get(0);
        assertThat(d.getModule()).isEqualTo("SQM");
        assertThat(d.getBizType()).isEqualTo("供应商审核会签");
        assertThat(d.getBizNo()).isEqualTo("AUD-1");
        assertThat(d.getApplicant()).isEqualTo("lead-1");
        assertThat(d.getUrl()).isEqualTo("/sqm/audits");
    }

    @Test
    @DisplayName("TLM 报废/维修分支:approverId 匹配且 PENDING → 映射")
    void tlm_scrap_and_repair_branch() {
        login("U1", "MZ");
        TlmScrap scrap = new TlmScrap();
        scrap.setId("sc-1");
        scrap.setScrapNo("SCR-1");
        scrap.setStatus("PENDING");
        scrap.setCreatedBy("creator-1");
        scrap.setCreatedAt(LocalDateTime.of(2026, 5, 1, 8, 0));
        when(tlmScrapMapper.selectList(any())).thenReturn(List.of(scrap));

        TlmRepair repair = new TlmRepair();
        repair.setId("rp-1");
        repair.setRepairNo("REP-1");
        repair.setStatus("PENDING");
        repair.setCreatedBy("creator-2");
        repair.setCreatedAt(LocalDateTime.of(2026, 5, 2, 8, 0));
        when(tlmRepairMapper.selectList(any())).thenReturn(List.of(repair));

        List<PendingApprovalDTO> list = service.myPending();
        assertThat(list).hasSize(2);
        Map<String, PendingApprovalDTO> byNo = list.stream()
                .collect(java.util.stream.Collectors.toMap(PendingApprovalDTO::getBizNo, d -> d));
        assertThat(byNo.get("SCR-1").getModule()).isEqualTo("TLM");
        assertThat(byNo.get("SCR-1").getBizType()).isEqualTo("工装报废审批");
        assertThat(byNo.get("REP-1").getBizType()).isEqualTo("工装维修审批");
    }

    @Test
    @DisplayName("排序:appliedAt 倒序,null 置后")
    void sort_descByAppliedAt_nullLast() {
        login("U1", "MZ");
        FiaApproval newer = new FiaApproval();
        newer.setId("a1");
        newer.setApplyAt(LocalDateTime.of(2026, 6, 2, 0, 0));
        FiaApproval older = new FiaApproval();
        older.setId("a2");
        older.setApplyAt(LocalDateTime.of(2026, 6, 1, 0, 0));
        FiaApproval noTime = new FiaApproval();
        noTime.setId("a3");
        noTime.setApplyAt(null);
        when(fiaApprovalMapper.selectList(any())).thenReturn(List.of(older, noTime, newer));

        List<PendingApprovalDTO> list = service.myPending();
        assertThat(list).extracting(PendingApprovalDTO::getId)
                .containsExactly("a1", "a2", "a3");
    }

    @Test
    @DisplayName("ROOT 管理员:跳过 org 过滤(仍聚合全部 org 的待审批)")
    void rootAdmin_skipsOrgFilter() {
        // orgId=ROOT → hasOrg=false,查询不加 org 条件,但逻辑分支仍按 userId 匹配
        CompanyContext.set(new CompanyContext.CurrentUser("signer-1", "root", "ROOT", "all"));
        Qms8dApprovalConfig cfg = new Qms8dApprovalConfig();
        cfg.setStageCode("D1");
        cfg.setSigner("signer-1");
        cfg.setNeedApproval(true);
        cfg.setOrgId("ROOT");
        when(approvalConfigMapper.selectList(any())).thenReturn(List.of(cfg));

        Qms8dStageDetail st = new Qms8dStageDetail();
        st.setId("st-1");
        st.setD8Id("d8-1");
        st.setStageCode("D1");
        st.setApprovalStatus("待审批");
        when(stageDetailMapper.selectList(any())).thenReturn(List.of(st));
        Qms8dReport r = new Qms8dReport();
        r.setId("d8-1");
        r.setD8No("8D-ROOT");
        r.setStatus("进行中");
        when(reportMapper.selectList(any())).thenReturn(List.of(r));

        List<PendingApprovalDTO> list = service.myPending();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getBizNo()).isEqualTo("8D-ROOT");
    }
}
