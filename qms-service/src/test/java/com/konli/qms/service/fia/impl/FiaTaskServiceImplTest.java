package com.konli.qms.service.fia.impl;

import com.konli.qms.common.enums.QmsEnums.FiaTaskStatus;
import com.konli.qms.common.enums.QmsEnums.FactoryDisposition;
import com.konli.qms.common.enums.QmsEnums.SupplierDisposition;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaApproval;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.fia.mapper.FiaArchivedReportMapper;
import com.konli.qms.domain.fia.mapper.FiaInspItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdMapper;
import com.konli.qms.domain.fia.mapper.FiaTaskLogMapper;
import com.konli.qms.domain.fia.mapper.FiaTaskMapper;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.tlm.entity.TlmTooling;
import com.konli.qms.domain.tlm.mapper.TlmToolingMapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.fia.FiaApprovalService;
import com.konli.qms.service.fia.FiaWoLockService;
import com.konli.qms.service.fia.SignConfigService;
import com.konli.qms.service.ncm.NcmDefectRecordService;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.spc.SpcParamService;
import com.konli.qms.service.spc.SpcSubgroupService;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.domain.spc.mapper.SpcSpecStandardMapper;
import com.konli.qms.domain.sqm.entity.SqmSupplier;
import com.konli.qms.domain.sqm.mapper.SqmSupplierMapper;
import com.konli.qms.service.sqm.SqmTraceService;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 首件检验任务 Service 测试（M4 fia，Mockito 纯单元）。
 * 覆盖核心分级签名状态机 + 处置路径枚举校验 + 审批驳回幂等。
 * 密码校验通过 signConfigService.get 返回 null(免密配置)绕过,聚焦状态流转。
 */
@ExtendWith(MockitoExtension.class)
class FiaTaskServiceImplTest {

    @Mock FiaTaskMapper fiaTaskMapper;
    @Mock FiaInspItemMapper fiaInspItemMapper;
    @Mock FiaInspStdMapper fiaInspStdMapper;
    @Mock FiaInspStdItemMapper fiaInspStdItemMapper;
    @Mock FiaArchivedReportMapper fiaArchivedReportMapper;
    @Mock FiaTaskLogMapper fiaTaskLogMapper;
    @Mock SignConfigService signConfigService;
    @Mock SysUserMapper sysUserMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock SpcParamMapper spcParamMapper;
    @Mock SpcSpecStandardMapper spcSpecStandardMapper;
    @Mock SqmSupplierMapper sqmSupplierMapper;
    @Mock SpcParamService spcParamService;
    @Mock SpcSubgroupService spcSubgroupService;
    @Mock FiaApprovalService fiaApprovalService;
    @Mock FiaWoLockService fiaWoLockService;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock NcmDefectRecordService ncmDefectRecordService;
    @Mock SqmTraceService sqmTraceService;
    @Mock NotificationService notificationService;
    @Mock TlmToolingMapper tlmToolingMapper;

    @InjectMocks FiaTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        // 免密:签名配置为空 -> verifyPassword 直接 return
        lenient().when(signConfigService.get(any())).thenReturn(null);
        CompanyContext.set(new CompanyContext.CurrentUser("op-1", "tester", "ORG-MZ", "MZ"));
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    private FiaTask taskWithStatus(String status) {
        FiaTask t = new FiaTask();
        t.setId("T1");
        t.setOrgId("ORG-MZ");
        t.setCode("FA-TEST");
        t.setWoNo("WO-1");
        t.setStatus(status);
        return t;
    }

    // ---------- signInspector ----------
    @Test
    @DisplayName("signInspector:任务不存在抛 BusinessException")
    void signInspector_notFound() {
        when(fiaTaskMapper.selectById("T1")).thenReturn(null);
        assertThrows(BusinessException.class, () -> service.signInspector("T1", "p", null));
    }

    @Test
    @DisplayName("signInspector:状态非进行中抛 BusinessException")
    void signInspector_wrongStatus() {
        when(fiaTaskMapper.selectById("T1")).thenReturn(taskWithStatus(FiaTaskStatus.WAIT_REVIEW));
        assertThrows(BusinessException.class, () -> service.signInspector("T1", "p", null));
    }

    @Test
    @DisplayName("signInspector:整单签名(进行中)->待复核")
    void signInspector_fullToWaitReview() {
        when(fiaTaskMapper.selectById("T1")).thenReturn(taskWithStatus(FiaTaskStatus.IN_PROGRESS));
        service.signInspector("T1", "p", null);
        verify(fiaTaskMapper).updateById(org.mockito.ArgumentMatchers.<FiaTask>argThat(
                (FiaTask t) -> FiaTaskStatus.WAIT_REVIEW.equals(t.getStatus()) && "op-1".equals(t.getInspectorId())));
    }

    @Test
    @DisplayName("signInspector:逐项签名(itemId非空)不改 task 状态")
    void signInspector_itemLevelKeepsStatus() {
        FiaTask t = taskWithStatus(FiaTaskStatus.IN_PROGRESS);
        when(fiaTaskMapper.selectById("T1")).thenReturn(t);
        service.signInspector("T1", "p", "item-9");
        // 逐项签名仅记日志,不 updateById(状态不变)
        verify(fiaTaskMapper, never()).updateById(any(FiaTask.class));
    }

    // ---------- signReviewer ----------
    @Test
    @DisplayName("signReviewer:状态非待复核抛 BusinessException")
    void signReviewer_wrongStatus() {
        when(fiaTaskMapper.selectById("T1")).thenReturn(taskWithStatus(FiaTaskStatus.IN_PROGRESS));
        assertThrows(BusinessException.class, () -> service.signReviewer("T1", "p", null));
    }

    @Test
    @DisplayName("signReviewer:需审批路径(disposition=让步接收)->创建审批单并置审批中")
    void signReviewer_needsApproval() {
        FiaTask t = taskWithStatus(FiaTaskStatus.WAIT_REVIEW);
        t.setDisposition(FactoryDisposition.CONCESSION); // 在 APPROVAL_DISPOSITIONS
        when(fiaTaskMapper.selectById("T1")).thenReturn(t);
        service.signReviewer("T1", "p", null);
        verify(fiaApprovalService).removePendingByTask("T1");
        verify(fiaApprovalService).create(any(FiaApproval.class));
        verify(fiaTaskMapper).updateById(org.mockito.ArgumentMatchers.<FiaTask>argThat(
                (FiaTask x) -> FiaTaskStatus.IN_APPROVAL.equals(x.getStatus())));
    }

    // ---------- signApprover ----------
    @Test
    @DisplayName("signApprover:状态非待批准抛 BusinessException")
    void signApprover_wrongStatus() {
        when(fiaTaskMapper.selectById("T1")).thenReturn(taskWithStatus(FiaTaskStatus.WAIT_REVIEW));
        assertThrows(BusinessException.class, () -> service.signApprover("T1", "p"));
    }

    // ---------- setDisposition ----------
    @Test
    @DisplayName("setDisposition:SUPPLIER 源用工厂处置(返工)抛 BusinessException")
    void setDisposition_supplierRejectsFactory() {
        FiaTask t = taskWithStatus(FiaTaskStatus.IN_PROGRESS);
        t.setSource("SUPPLIER");
        when(fiaTaskMapper.selectById("T1")).thenReturn(t);
        assertThrows(BusinessException.class,
                () -> service.setDisposition("T1", FactoryDisposition.REWORK, "r"));
    }

    @Test
    @DisplayName("setDisposition:SUPPLIER 源接受 SORT(供应商分拣)并写入")
    void setDisposition_supplierAcceptsSort() {
        FiaTask t = taskWithStatus(FiaTaskStatus.IN_PROGRESS);
        t.setSource("SUPPLIER");
        when(fiaTaskMapper.selectById("T1")).thenReturn(t);
        when(fiaTaskLogMapper.selectCount(any())).thenReturn(0L);
        service.setDisposition("T1", SupplierDisposition.SORT, "分拣");
        verify(fiaTaskMapper).updateById(org.mockito.ArgumentMatchers.<FiaTask>argThat(
                (FiaTask x) -> SupplierDisposition.SORT.equals(x.getDisposition())
                        && "分拣".equals(x.getRemark())));
    }

    @Test
    @DisplayName("setDisposition:FACTORY 源接受 REWORK(返工)并写入")
    void setDisposition_factoryAcceptsRework() {
        FiaTask t = taskWithStatus(FiaTaskStatus.IN_PROGRESS);
        t.setSource("FACTORY");
        when(fiaTaskMapper.selectById("T1")).thenReturn(t);
        when(fiaTaskLogMapper.selectCount(any())).thenReturn(0L);
        service.setDisposition("T1", FactoryDisposition.REWORK, "返工处理");
        verify(fiaTaskMapper).updateById(org.mockito.ArgumentMatchers.<FiaTask>argThat(
                (FiaTask x) -> FactoryDisposition.REWORK.equals(x.getDisposition())));
    }

    // ---------- rejectTask ----------
    @Test
    @DisplayName("rejectTask:任务不存在抛 BusinessException")
    void rejectTask_notFound() {
        when(fiaTaskMapper.selectById("T1")).thenReturn(null);
        assertThrows(BusinessException.class, () -> service.rejectTask("T1"));
    }

    @Test
    @DisplayName("rejectTask:非审批中(已驳回)幂等返回,不更新")
    void rejectTask_idempotentWhenNotInApproval() {
        when(fiaTaskMapper.selectById("T1")).thenReturn(taskWithStatus(FiaTaskStatus.REJECTED));
        service.rejectTask("T1");
        verify(fiaTaskMapper, never()).updateById(any(FiaTask.class));
    }

    @Test
    @DisplayName("rejectTask:审批中->已驳回")
    void rejectTask_toRejected() {
        when(fiaTaskMapper.selectById("T1")).thenReturn(taskWithStatus(FiaTaskStatus.IN_APPROVAL));
        service.rejectTask("T1");
        verify(fiaTaskMapper).updateById(org.mockito.ArgumentMatchers.<FiaTask>argThat(
                (FiaTask x) -> FiaTaskStatus.REJECTED.equals(x.getStatus())));
    }
}
