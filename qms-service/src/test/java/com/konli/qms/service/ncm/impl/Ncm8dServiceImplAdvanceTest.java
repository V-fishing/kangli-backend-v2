package com.konli.qms.service.ncm.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.entity.Qms8dStageDetail;
import com.konli.qms.domain.ncm.mapper.Qms8dReportMapper;
import com.konli.qms.domain.ncm.mapper.Qms8dStageDetailMapper;
import com.konli.qms.domain.ncm.mapper.QmsAssignRecordMapper;
import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;
import com.konli.qms.domain.sqm.mapper.SqmIncomingAbnormalMapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.assign.AssignReassignService;
import com.konli.qms.service.ncm.Ncm8dArchiveService;
import com.konli.qms.service.ncm.Ncm8dApprovalConfigService;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.ncm.Qms8dFishboneService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 8D 报告阶段推进/签批 Service 测试（M5 ncm，Mockito 纯单元）。
 * 覆盖 advanceStage 顺序校验/已闭环防护/无需审批推进/需审批停留 + approveStage 口令校验/阶段匹配/通过/驳回。
 * 避开 D4 根因校验与 D8 闭环分支(依赖 triggerCapa/archive/closeAbnormal 等附加链路),
 * 聚焦核心状态机与签批权限守卫。
 */
@ExtendWith(MockitoExtension.class)
class Ncm8dServiceImplAdvanceTest {

    @Mock Qms8dReportMapper qms8dReportMapper;
    @Mock Qms8dStageDetailMapper qms8dStageDetailMapper;
    @Mock SqmIncomingAbnormalMapper abnormalMapper;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock NcmCapaService ncmCapaService;
    @Mock Ncm8dApprovalConfigService approvalConfigService;
    @Mock SysUserMapper sysUserMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock NotificationService notificationService;
    @Mock Qms8dFishboneService fishboneService;
    @Mock Ncm8dArchiveService ncm8dArchiveService;
    @Mock QmsAssignRecordMapper assignRecordMapper;
    @Mock AssignReassignService assignReassignService;

    @InjectMocks Ncm8dServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(qms8dReportMapper.updateById(any(Qms8dReport.class))).thenReturn(1);
        lenient().when(qms8dStageDetailMapper.updateById(any(Qms8dStageDetail.class))).thenReturn(1);
        lenient().when(qms8dStageDetailMapper.insert(any(Qms8dStageDetail.class))).thenReturn(1);
        // 默认无需审批 + 无指定签批人(聚焦状态机)
        lenient().when(approvalConfigService.needApproval(anyString(), anyString())).thenReturn(false);
        lenient().when(approvalConfigService.signerOf(anyString(), anyString())).thenReturn(null);
        // 默认口令正确
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        CompanyContext.set(new CompanyContext.CurrentUser("u-1", "qmanager", "ORG-MZ", "MZ"));
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    private Qms8dReport report(String status, String currentStage) {
        Qms8dReport r = new Qms8dReport();
        r.setId("D8-1");
        r.setOrgId("ORG-MZ");
        r.setD8No("8D-TEST");
        r.setIssue("测试问题");
        r.setStatus(status);
        r.setCurrentStage(currentStage);
        return r;
    }

    // ---------- advanceStage ----------
    @Test
    @DisplayName("advanceStage:阶段顺序错误抛 BusinessException")
    void advance_wrongOrder() {
        when(qms8dReportMapper.selectById("D8-1")).thenReturn(report("进行中", "D2"));
        assertThrows(BusinessException.class,
                () -> service.advanceStage("D8-1", "D1", "内容", "owner", null));
    }

    @Test
    @DisplayName("advanceStage:已闭环报告抛 BusinessException")
    void advance_closedLoop() {
        when(qms8dReportMapper.selectById("D8-1")).thenReturn(report("已闭环", "D8"));
        assertThrows(BusinessException.class,
                () -> service.advanceStage("D8-1", "D8", "内容", "owner", null));
    }

    @Test
    @DisplayName("advanceStage:无需审批阶段(D2)->推进到 D3")
    void advance_noApproval_advances() {
        when(qms8dReportMapper.selectById("D8-1")).thenReturn(report("进行中", "D2"));
        when(qms8dStageDetailMapper.selectOne(any())).thenReturn(null);
        service.advanceStage("D8-1", "D2", "根因分析", "owner", null);
        verify(qms8dReportMapper, org.mockito.Mockito.atLeastOnce()).updateById(org.mockito.ArgumentMatchers.<Qms8dReport>argThat(
                (Qms8dReport r) -> "D3".equals(r.getCurrentStage())));
    }

    @Test
    @DisplayName("advanceStage:需审批阶段(D1)->停留当前阶段并置待审批")
    void advance_needsApproval_stays() {
        when(qms8dReportMapper.selectById("D8-1")).thenReturn(report("进行中", "D1"));
        when(qms8dStageDetailMapper.selectOne(any())).thenReturn(null);
        when(approvalConfigService.needApproval("ORG-MZ", "D1")).thenReturn(true);
        service.advanceStage("D8-1", "D1", "团队组建", "owner", "u-a,u-b");
        verify(qms8dStageDetailMapper).insert(org.mockito.ArgumentMatchers.<Qms8dStageDetail>argThat(
                (Qms8dStageDetail d) -> "待审批".equals(d.getApprovalStatus())
                        && "u-a,u-b".equals(d.getTeamMembers())));
        // 主表阶段停留 D1(未推进)
        verify(qms8dReportMapper, org.mockito.Mockito.atLeastOnce()).updateById(org.mockito.ArgumentMatchers.<Qms8dReport>argThat(
                (Qms8dReport r) -> "D1".equals(r.getCurrentStage())));
    }

    // ---------- approveStage ----------
    @Test
    @DisplayName("approveStage:当前阶段不匹配抛 BusinessException")
    void approve_stageMismatch() {
        when(qms8dReportMapper.selectById("D8-1")).thenReturn(report("进行中", "D2"));
        assertThrows(BusinessException.class,
                () -> service.approveStage("D8-1", "D1", true, "ok", "123456"));
    }

    @Test
    @DisplayName("approveStage:阶段未处于待审批状态抛 BusinessException")
    void approve_notPending() {
        Qms8dReport r = report("进行中", "D1");
        when(qms8dReportMapper.selectById("D8-1")).thenReturn(r);
        Qms8dStageDetail d = new Qms8dStageDetail();
        d.setApprovalStatus("无需审批");
        when(qms8dStageDetailMapper.selectOne(any())).thenReturn(d);
        assertThrows(BusinessException.class,
                () -> service.approveStage("D8-1", "D1", true, "ok", "123456"));
    }

    @Test
    @DisplayName("approveStage:口令错误抛 BusinessException")
    void approve_wrongPassword() {
        Qms8dReport r = report("进行中", "D1");
        when(qms8dReportMapper.selectById("D8-1")).thenReturn(r);
        Qms8dStageDetail d = new Qms8dStageDetail();
        d.setApprovalStatus("待审批");
        when(qms8dStageDetailMapper.selectOne(any())).thenReturn(d);
        SysUser u = new SysUser();
        u.setPasswordHash("hash");
        when(sysUserMapper.selectOne(any())).thenReturn(u);
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);
        assertThrows(BusinessException.class,
                () -> service.approveStage("D8-1", "D1", true, "ok", "bad"));
    }

    @Test
    @DisplayName("approveStage:通过->阶段置已通过并推进到下一阶段(D2)")
    void approve_pass_advances() {
        Qms8dReport r = report("进行中", "D1");
        when(qms8dReportMapper.selectById("D8-1")).thenReturn(r);
        Qms8dStageDetail d = new Qms8dStageDetail();
        d.setApprovalStatus("待审批");
        when(qms8dStageDetailMapper.selectOne(any())).thenReturn(d);
        SysUser u = new SysUser();
        u.setPasswordHash("hash");
        when(sysUserMapper.selectOne(any())).thenReturn(u);
        when(passwordEncoder.matches("123456", "hash")).thenReturn(true);
        service.approveStage("D8-1", "D1", true, "同意", "123456");
        verify(qms8dStageDetailMapper).updateById(org.mockito.ArgumentMatchers.<Qms8dStageDetail>argThat(
                (Qms8dStageDetail x) -> "已通过".equals(x.getApprovalStatus())));
        verify(qms8dReportMapper, org.mockito.Mockito.atLeastOnce()).updateById(org.mockito.ArgumentMatchers.<Qms8dReport>argThat(
                (Qms8dReport x) -> "D2".equals(x.getCurrentStage())));
    }

    @Test
    @DisplayName("approveStage:驳回->阶段置已驳回并退回当前阶段")
    void approve_reject_stays() {
        Qms8dReport r = report("进行中", "D1");
        when(qms8dReportMapper.selectById("D8-1")).thenReturn(r);
        Qms8dStageDetail d = new Qms8dStageDetail();
        d.setApprovalStatus("待审批");
        when(qms8dStageDetailMapper.selectOne(any())).thenReturn(d);
        SysUser u = new SysUser();
        u.setPasswordHash("hash");
        when(sysUserMapper.selectOne(any())).thenReturn(u);
        when(passwordEncoder.matches("123456", "hash")).thenReturn(true);
        service.approveStage("D8-1", "D1", false, "不同意", "123456");
        verify(qms8dStageDetailMapper).updateById(org.mockito.ArgumentMatchers.<Qms8dStageDetail>argThat(
                (Qms8dStageDetail x) -> "已驳回".equals(x.getApprovalStatus())));
        verify(qms8dReportMapper, org.mockito.Mockito.atLeastOnce()).updateById(org.mockito.ArgumentMatchers.<Qms8dReport>argThat(
                (Qms8dReport x) -> "D1".equals(x.getCurrentStage())));
    }
}
