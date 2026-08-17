package com.konli.qms.service.ncm.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.mapper.NcmDefectRecordMapper;
import com.konli.qms.domain.ncm.mapper.QmsAssignRecordMapper;
import com.konli.qms.domain.notify.mapper.NotifyChannelMapper;
import com.konli.qms.service.ncm.Ncm8dService;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.ncm.NcmCorrectiveActionService;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;
import com.konli.qms.service.notify.DirectNotifyService;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.notify.NotifyConfigService;
import com.konli.qms.service.sqm.SqmFmeaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 缺陷记录发起 8D 单元测试（M2 ncm，补充统一整改源头核心链路）。
 * 验证：launch8dFromDefect 从缺陷记录发起 8D，8D 的 source=不良记录、sourceRefId 指向缺陷记录，
 * 并回写缺陷记录的 d8No（双向关联）。
 */
@ExtendWith(MockitoExtension.class)
class NcmDefectRecordServiceImplLaunch8dTest {

    @Mock NcmDefectRecordMapper ncmDefectRecordMapper;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock com.konli.qms.domain.ncm.mapper.NcmDefectDictMapper ncmDefectDictMapper;
    @Mock Ncm8dService ncm8dService;
    @Mock NcmCapaService ncmCapaService;
    @Mock NcmCorrectiveActionService ncmCorrectiveActionService;
    @Mock QmsAssignRecordMapper qmsAssignRecordMapper;
    @Mock NotificationService notificationService;
    @Mock NotifyConfigService notifyConfigService;
    @Mock DirectNotifyService directNotifyService;
    @Mock NotifyChannelMapper notifyChannelMapper;
    @Mock SqmFmeaService sqmFmeaService;

    NcmDefectRecordServiceImpl service;

    static final String DEFECT_ID = "def-1";
    static final String OWNER = "0be4a44d-d0f8-5a89-af6a-090f7d3af5f1";

    @BeforeEach
    void setUp() {
        service = new NcmDefectRecordServiceImpl(
                ncmDefectRecordMapper, jdbcTemplate, ncmDefectDictMapper, ncm8dService,
                ncmCapaService, ncmCorrectiveActionService, qmsAssignRecordMapper,
                notificationService, notifyConfigService, directNotifyService,
                notifyChannelMapper, sqmFmeaService);
        // 通用 no-op stub（避免 strict stubbing 报错）
        lenient().when(jdbcTemplate.queryForObject(anyString(), any(Class.class), anyString())).thenReturn("MZ-质量经理");
        lenient().when(notifyConfigService.resolveChannels(any(), any())).thenReturn(List.of("站内弹窗"));
        CompanyContext.set(new CompanyContext.CurrentUser("assigner", "admin", "org-1", "org"));
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    private NcmDefectRecord baseDefect() {
        NcmDefectRecord d = new NcmDefectRecord();
        d.setId(DEFECT_ID);
        d.setDefectNo("DR-001");
        d.setOrgId("org-1");
        d.setIssue("焊接虚焊");
        d.setSeverity("高");
        return d;
    }

    @Test
    @DisplayName("从缺陷记录发起 8D:8D source=不良记录 且 sourceRefId 指向缺陷记录")
    void launch8d_linksBackToDefect() {
        NcmDefectRecord def = baseDefect();
        when(ncmDefectRecordMapper.selectById(DEFECT_ID)).thenReturn(def);

        Qms8dReport created = new Qms8dReport();
        created.setId("8d-id-1");
        created.setD8No("8D-2026-001");
        when(ncm8dService.create(any(Qms8dReport.class))).thenReturn(created);

        DefectLaunchRequest req = new DefectLaunchRequest();
        req.setOwnerUserId(OWNER);

        Qms8dReport result = (Qms8dReport) service.launch8dFromDefect(DEFECT_ID, req);

        // 1) 8D 来源与溯源正确
        ArgumentCaptor<Qms8dReport> cap = ArgumentCaptor.forClass(Qms8dReport.class);
        verify(ncm8dService).create(cap.capture());
        Qms8dReport passed = cap.getValue();
        assertThat(passed.getSource()).isEqualTo("不良记录");
        assertThat(passed.getSourceRefId()).isEqualTo(DEFECT_ID);
        assertThat(passed.getIssue()).isEqualTo("焊接虚焊");
        assertThat(passed.getOrgId()).isEqualTo("org-1");

        // 2) 缺陷记录 d8No 回写（双向关联）
        verify(ncmDefectRecordMapper).updateById(def);
        assertThat(def.getD8No()).isEqualTo("8D-2026-001");

        // 3) 返回创建结果
        assertThat(result.getD8No()).isEqualTo("8D-2026-001");
    }

    @Test
    @DisplayName("缺陷记录不存在:抛 400")
    void launch8d_defectNotFound_throws400() {
        when(ncmDefectRecordMapper.selectById("missing")).thenReturn(null);
        DefectLaunchRequest req = new DefectLaunchRequest();
        assertThatThrownBy(() -> service.launch8dFromDefect("missing", req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 400);
    }

    @Test
    @DisplayName("缺陷记录已发起过 8D:抛 400 不可重复发起")
    void launch8d_alreadyLaunched_throws400() {
        NcmDefectRecord def = baseDefect();
        def.setD8No("8D-EXISTING");
        when(ncmDefectRecordMapper.selectById(DEFECT_ID)).thenReturn(def);
        DefectLaunchRequest req = new DefectLaunchRequest();
        assertThatThrownBy(() -> service.launch8dFromDefect(DEFECT_ID, req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 400);
    }
}
