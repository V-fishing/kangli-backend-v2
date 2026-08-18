package com.konli.qms.service.sqm.impl;

import com.konli.qms.domain.sqm.entity.SqmSupplierGradeRule;
import com.konli.qms.domain.sqm.mapper.SqmIncomingLotMapper;
import com.konli.qms.domain.sqm.mapper.SqmPerfMetricCfgMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierGradeRuleMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierPerformanceMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierShareMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 供应商绩效等级计算单元测试（M13 sqm 绩效 R3 修复点）。
 *
 * <p>聚焦 V114 落地的全局分级规则 sqm_supplier_grade_rule 区间匹配逻辑:
 *   A[90,101) / B[80,90) / C[70,80) / D[0,70), score_max=101 保证 100 分命中 A。
 * 通过反射调用 private levelByRule / levelOf,验证关键边界(100→A、90→A、89.99→B、70→C、69.99→D)。
 */
@ExtendWith(MockitoExtension.class)
class SqmSupplierPerformanceGradeTest {

    @Mock SqmSupplierPerformanceMapper sqmSupplierPerformanceMapper;
    @Mock SqmIncomingLotMapper sqmIncomingLotMapper;
    @Mock SqmSupplierGradeRuleMapper gradeRuleMapper;
    @Mock SqmSupplierMapper sqmSupplierMapper;
    @Mock SqmPerfMetricCfgMapper metricCfgMapper;
    @Mock SqmSupplierShareMapper sqmSupplierShareMapper;

    @InjectMocks
    SqmSupplierPerformanceServiceImpl service;

    private static SqmSupplierGradeRule rule(String level, int min, int max) {
        SqmSupplierGradeRule r = new SqmSupplierGradeRule();
        r.setLevel(level);
        r.setScoreMin(BigDecimal.valueOf(min));
        r.setScoreMax(BigDecimal.valueOf(max));
        return r;
    }

    private String invokeLevelByRule(BigDecimal score) throws Exception {
        Method m = SqmSupplierPerformanceServiceImpl.class
                .getDeclaredMethod("levelByRule", BigDecimal.class);
        m.setAccessible(true);
        return (String) m.invoke(service, score);
    }

    private String invokeLevelOf(BigDecimal score) throws Exception {
        Method m = SqmSupplierPerformanceServiceImpl.class
                .getDeclaredMethod("levelOf", BigDecimal.class);
        m.setAccessible(true);
        return (String) m.invoke(service, score);
    }

    // ---- levelByRule: 规则表命中(模拟 V114 种子 A[90,101)/B[80,90)/C[70,80)/D[0,70)) ----

    @Test
    @DisplayName("满分 100 -> A(规则 scoreMax=101 命中)")
    void levelByRule_fullScore_isA() throws Exception {
        when(gradeRuleMapper.selectList(null)).thenReturn(List.of(
                rule("A", 90, 101), rule("B", 80, 90), rule("C", 70, 80), rule("D", 0, 70)));
        assertThat(invokeLevelByRule(new BigDecimal("100"))).isEqualTo("A");
    }

    @Test
    @DisplayName("边界 90 -> A(左闭)")
    void levelByRule_90_isA() throws Exception {
        when(gradeRuleMapper.selectList(null)).thenReturn(List.of(
                rule("A", 90, 101), rule("B", 80, 90), rule("C", 70, 80), rule("D", 0, 70)));
        assertThat(invokeLevelByRule(new BigDecimal("90"))).isEqualTo("A");
    }

    @Test
    @DisplayName("89.99 -> B(右开)")
    void levelByRule_8999_isB() throws Exception {
        when(gradeRuleMapper.selectList(null)).thenReturn(List.of(
                rule("A", 90, 101), rule("B", 80, 90), rule("C", 70, 80), rule("D", 0, 70)));
        assertThat(invokeLevelByRule(new BigDecimal("89.99"))).isEqualTo("B");
    }

    @Test
    @DisplayName("边界 70 -> C(左闭)")
    void levelByRule_70_isC() throws Exception {
        when(gradeRuleMapper.selectList(null)).thenReturn(List.of(
                rule("A", 90, 101), rule("B", 80, 90), rule("C", 70, 80), rule("D", 0, 70)));
        assertThat(invokeLevelByRule(new BigDecimal("70"))).isEqualTo("C");
    }

    @Test
    @DisplayName("69.99 -> D(右开)")
    void levelByRule_6999_isD() throws Exception {
        when(gradeRuleMapper.selectList(null)).thenReturn(List.of(
                rule("A", 90, 101), rule("B", 80, 90), rule("C", 70, 80), rule("D", 0, 70)));
        assertThat(invokeLevelByRule(new BigDecimal("69.99"))).isEqualTo("D");
    }

    @Test
    @DisplayName("规则未配置 -> 回退 levelOf")
    void levelByRule_noRule_fallsBackToLevelOf() throws Exception {
        when(gradeRuleMapper.selectList(null)).thenReturn(List.of());
        assertThat(invokeLevelByRule(new BigDecimal("85"))).isEqualTo("B");
    }

    @Test
    @DisplayName("规则查询异常 -> 回退 levelOf(不抛)")
    void levelByRule_queryThrows_fallsBack() throws Exception {
        when(gradeRuleMapper.selectList(null)).thenThrow(new RuntimeException("db down"));
        assertThat(invokeLevelByRule(new BigDecimal("85"))).isEqualTo("B");
    }

    // ---- levelOf: 兜底简单算法(无规则表时) ----

    @Test
    @DisplayName("levelOf: null -> D")
    void levelOf_null_isD() throws Exception {
        assertThat(invokeLevelOf(null)).isEqualTo("D");
    }

    @Test
    @DisplayName("levelOf: 95 -> A")
    void levelOf_95_isA() throws Exception {
        assertThat(invokeLevelOf(new BigDecimal("95"))).isEqualTo("A");
    }

    @Test
    @DisplayName("levelOf: 80 -> B(边界)")
    void levelOf_80_isB() throws Exception {
        assertThat(invokeLevelOf(new BigDecimal("80"))).isEqualTo("B");
    }

    @Test
    @DisplayName("levelOf: 60 -> D")
    void levelOf_60_isD() throws Exception {
        assertThat(invokeLevelOf(new BigDecimal("60"))).isEqualTo("D");
    }
}
