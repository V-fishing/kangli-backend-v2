package com.konli.qms.service.ncm.impl;

import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.mapper.Qms8dReportMapper;
import com.konli.qms.domain.ncm.mapper.Qms8dStageDetailMapper;
import com.konli.qms.domain.sqm.mapper.SqmIncomingAbnormalMapper;
import com.konli.qms.service.assign.AssignReassignService;
import com.konli.qms.service.ncm.Ncm8dApprovalConfigService;
import com.konli.qms.service.ncm.Ncm8dArchiveService;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.ncm.NcmDefectRecordService;
import com.konli.qms.service.ncm.dto.Abnormal8dLaunchRequest;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;
import com.konli.qms.service.notify.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Ncm8dServiceImpl.create 单元测试:聚焦"统一整改源头"改造(第三步)。
 * 验证人工建 8D 时先登记缺陷记录(source=人工, defectDictCode=NCM),
 * 再从缺陷记录发起 8D,使 8D 的 source=不良记录、sourceRefId 指向缺陷记录主键。
 *
 * 注:Ncm8dServiceImpl 用 @RequiredArgsConstructor(final 字段)+ @Autowired @Lazy(非 final 字段 ncmDefectRecordService)。
 * Mockito @InjectMocks 走构造器注入后不会回填 @Lazy 字段,故此处手动构造并反射注入该字段。
 */
@ExtendWith(MockitoExtension.class)
class Ncm8dServiceImplCreateTest {

    @Mock Qms8dReportMapper qms8dReportMapper;
    @Mock Qms8dStageDetailMapper qms8dStageDetailMapper;
    @Mock SqmIncomingAbnormalMapper abnormalMapper;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock NcmCapaService ncmCapaService;
    @Mock Ncm8dApprovalConfigService ncm8dApprovalConfigService;
    @Mock com.konli.qms.domain.uop.mapper.SysUserMapper sysUserMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock NotificationService notificationService;
    @Mock com.konli.qms.service.ncm.Qms8dFishboneService fishboneService;
    @Mock Ncm8dArchiveService ncm8dArchiveService;
    @Mock com.konli.qms.domain.ncm.mapper.QmsAssignRecordMapper assignRecordMapper;
    @Mock AssignReassignService assignReassignService;
    @Mock NcmDefectRecordService ncmDefectRecordService;

    Ncm8dServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        // 构造器注入 13 个 final 依赖(不含 @Lazy 的 ncmDefectRecordService)
        service = new Ncm8dServiceImpl(
                qms8dReportMapper, qms8dStageDetailMapper, abnormalMapper,
                jdbcTemplate, ncmCapaService, ncm8dApprovalConfigService,
                sysUserMapper, passwordEncoder, notificationService, fishboneService,
                ncm8dArchiveService, assignRecordMapper, assignReassignService);
        // 反射注入 @Lazy @Autowired 字段
        Field f = Ncm8dServiceImpl.class.getDeclaredField("ncmDefectRecordService");
        f.setAccessible(true);
        f.set(service, ncmDefectRecordService);

        // lenient:避免未使用的 stub 触发 strict stubbing 报错
        lenient().when(ncmDefectRecordService.create(any(NcmDefectRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("人工建 8D(正常流程):先落缺陷记录,8D source=不良记录 且 sourceRefId 指向缺陷记录")
    void create_manual8d_linksDefectRecord() throws Exception {
        Qms8dReport req = new Qms8dReport();
        req.setFlowType("8D");
        req.setSource("人工");
        req.setIssue("单元测试-人工建8D主题");
        req.setSeverity("高");
        req.setOrgId("019fd5dc-5197-75d2-bc6c-6c7be0def01a");

        // 缺陷记录 create 后回填 id
        when(ncmDefectRecordService.create(any(NcmDefectRecord.class))).thenAnswer(inv -> {
            NcmDefectRecord d = inv.getArgument(0);
            d.setId(UUID.randomUUID().toString());
            return d;
        });
        // launch8dFromDefect 返回一条报告(模拟标准范式)
        Qms8dReport launched = new Qms8dReport();
        when(ncmDefectRecordService.launch8dFromDefect(anyString(), any(DefectLaunchRequest.class)))
                .thenReturn(launched);

        Qms8dReport result = service.create(req);

        // 1) 缺陷记录被创建一次,且 source/字典/issue 正确
        ArgumentCaptor<NcmDefectRecord> defCap = ArgumentCaptor.forClass(NcmDefectRecord.class);
        verify(ncmDefectRecordService, times(1)).create(defCap.capture());
        NcmDefectRecord def = defCap.getValue();
        assertThat(def.getSource()).isEqualTo("人工");
        assertThat(def.getDefectDictCode()).isEqualTo("NCM");
        assertThat(def.getIssue()).isEqualTo("单元测试-人工建8D主题");

        // 2) 从缺陷记录发起 8D 一次,且传入缺陷记录 id
        ArgumentCaptor<String> idCap = ArgumentCaptor.forClass(String.class);
        verify(ncmDefectRecordService, times(1))
                .launch8dFromDefect(idCap.capture(), any(DefectLaunchRequest.class));
        assertThat(idCap.getValue()).isEqualTo(def.getId());

        // 3) 返回的 8D 由 launch8dFromDefect 产生(统一来源范式)
        assertThat(result).isSameAs(launched);
    }

    @Test
    @DisplayName("事件类来源(SQM异常)未填来源单号时抛 BusinessException(400)")
    void create_eventSourceWithoutRef_throws() {
        Qms8dReport req = new Qms8dReport();
        req.setFlowType("8D");
        req.setSource("SQM异常");
        req.setOrgId("019fd5dc-5197-75d2-bc6c-6c7be0def01a");
        // sourceRefId 留空

        org.junit.jupiter.api.Assertions.assertThrows(
                com.konli.qms.common.exception.BusinessException.class,
                () -> service.create(req));
    }

    @Test
    @DisplayName("简易流程:先落缺陷记录,8D 直接闭环且来源指向缺陷记录")
    void create_simpleFlow_linksDefectRecordAndCloses() throws Exception {
        Qms8dReport req = new Qms8dReport();
        req.setFlowType("简易");
        req.setSource("人工");
        req.setIssue("单元测试-简易闭环");
        req.setSeverity("中");
        req.setOrgId("019fd5dc-5197-75d2-bc6c-6c7be0def01a");

        when(ncmDefectRecordService.create(any(NcmDefectRecord.class))).thenAnswer(inv -> {
            NcmDefectRecord d = inv.getArgument(0);
            d.setId(UUID.randomUUID().toString());
            return d;
        });

        Qms8dReport result = service.create(req);

        verify(ncmDefectRecordService, times(1)).create(any(NcmDefectRecord.class));
        verify(ncmDefectRecordService, times(1)).linkD8(anyString(), anyString());
        verify(ncm8dArchiveService, times(1)).archive(any(Qms8dReport.class));
        assertThat(result.getStatus()).isEqualTo("已闭环");
        assertThat(result.getSource()).isEqualTo("不良记录");
        assertThat(result.getCurrentStage()).isEqualTo("D8");
    }
}
