package com.konli.qms.service.spc.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.spc.entity.SpcMeasurement;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcAlarmMapper;
import com.konli.qms.domain.spc.mapper.SpcControlLimitMapper;
import com.konli.qms.domain.spc.mapper.SpcMeasurementMapper;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.domain.spc.mapper.SpcRuleMapper;
import com.konli.qms.domain.spc.mapper.SpcSubgroupMapper;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.spc.SpcCapabilityService;
import com.konli.qms.service.spc.SpcCollectTaskService;
import com.konli.qms.service.spc.SpcGlobalConfigService;
import com.konli.qms.service.spc.SpcNotifyChannelService;
import com.konli.qms.service.spc.SpcSampleTaskService;
import com.konli.qms.service.spc.dto.CountSeries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SPC 子组采集 Service 测试（M2 spc，Mockito 纯单元）。
 * 覆盖 create 核心分支:参数不存在抛错 / 计量型空值抛错 / 计量型正常落库(xbar 计算+测量值落库) /
 * 计数型正常落库(xbar 保持 null)。判异规则因无历史子组(selectList 空)走正常分支,聚焦采集落库语义。
 */
@ExtendWith(MockitoExtension.class)
class SpcSubgroupServiceImplTest {

    @Mock SpcSubgroupMapper spcSubgroupMapper;
    @Mock SysUserMapper sysUserMapper;
    @Mock SpcParamMapper spcParamMapper;
    @Mock SpcMeasurementMapper spcMeasurementMapper;
    @Mock SpcRuleMapper spcRuleMapper;
    @Mock SpcAlarmMapper spcAlarmMapper;
    @Mock SpcControlLimitMapper spcControlLimitMapper;
    @Mock SpcNotifyChannelService spcNotifyChannelService;
    @Mock SpcGlobalConfigService spcGlobalConfigService;
    @Mock SpcCapabilityService spcCapabilityService;
    @Mock SpcCollectTaskService spcCollectTaskService;
    @Mock SpcSampleTaskService spcSampleTaskService;
    @Mock NotificationService notificationService;
    @Mock PlatformTransactionManager transactionManager;

    @InjectMocks SpcSubgroupServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(spcSubgroupMapper.insert(any(SpcSubgroup.class))).thenReturn(1);
        lenient().when(spcMeasurementMapper.insert(any(SpcMeasurement.class))).thenReturn(1);
        // 无历史子组 -> 判异规则走正常分支(返回 null)
        lenient().when(spcSubgroupMapper.selectList(any())).thenReturn(List.of());
        CompanyContext.set(new CompanyContext.CurrentUser("u-1", "op", "ORG-MZ", "MZ"));
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    private SpcParam param() {
        SpcParam p = new SpcParam();
        p.setId("P1");
        p.setOrgId("ORG-MZ");
        p.setParamName("直径");
        p.setProcName("车削");
        p.setChartCandidates(null); // 计数判异不触发
        return p;
    }

    @Test
    @DisplayName("create:参数不存在抛 BusinessException")
    void create_paramNotFound() {
        when(spcParamMapper.selectById("P1")).thenReturn(null);
        SpcSubgroup sg = new SpcSubgroup();
        sg.setParamId("P1");
        assertThrows(BusinessException.class, () -> service.create(sg, List.of(new BigDecimal("1.0"))));
    }

    @Test
    @DisplayName("create:计量型 values 为空且未给计数型字段抛 BusinessException")
    void create_variableEmpty() {
        when(spcParamMapper.selectById("P1")).thenReturn(param());
        SpcSubgroup sg = new SpcSubgroup();
        sg.setParamId("P1");
        sg.setOrgId("ORG-MZ");
        assertThrows(BusinessException.class, () -> service.create(sg, null));
    }

    @Test
    @DisplayName("create:计量型正常落库 -> xbar 计算 + 测量值落库 + 判定正常")
    void create_variable_ok() {
        when(spcParamMapper.selectById("P1")).thenReturn(param());
        SpcSubgroup sg = new SpcSubgroup();
        sg.setParamId("P1");
        sg.setOrgId("ORG-MZ");
        sg.setId("SG-1");
        sg.setInspectN(3);
        // values: 10.0, 12.0, 11.0 -> xbar = 11.0
        service.create(sg, List.of(new BigDecimal("10.0"), new BigDecimal("12.0"), new BigDecimal("11.0")));
        verify(spcSubgroupMapper).insert(org.mockito.ArgumentMatchers.<SpcSubgroup>argThat(
                (SpcSubgroup s) -> new BigDecimal("11.0000").compareTo(s.getXbar()) == 0
                        && "正常".equals(s.getJudge()) && !Boolean.TRUE.equals(s.getIsOutlier())));
        // 3 个测量值落库
        verify(spcMeasurementMapper, org.mockito.Mockito.times(3)).insert((com.konli.qms.domain.spc.entity.SpcMeasurement) any());
    }

    @Test
    @DisplayName("create:计数型(P/NP)正常落库 -> xbar 保持 null + 判定正常")
    void create_count_ok() {
        SpcParam p = param();
        p.setChartCandidates("P"); // 计数图候选
        when(spcParamMapper.selectById("P1")).thenReturn(p);
        SpcSubgroup sg = new SpcSubgroup();
        sg.setParamId("P1");
        sg.setOrgId("ORG-MZ");
        sg.setId("SG-2");
        sg.setNonconforming(2);
        sg.setInspectN(20);
        service.create(sg, null);
        verify(spcSubgroupMapper).insert(org.mockito.ArgumentMatchers.<SpcSubgroup>argThat(
                (SpcSubgroup s) -> s.getXbar() == null
                        && Integer.valueOf(20).equals(s.getN())
                        && "正常".equals(s.getJudge())));
        // 计数型无测量值落库
        verify(spcMeasurementMapper, org.mockito.Mockito.never()).insert((com.konli.qms.domain.spc.entity.SpcMeasurement) any());
    }
}
