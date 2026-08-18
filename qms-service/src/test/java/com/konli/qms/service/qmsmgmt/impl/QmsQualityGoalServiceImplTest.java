package com.konli.qms.service.qmsmgmt.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.qmsmgmt.entity.QmsQualityGoal;
import com.konli.qms.domain.qmsmgmt.mapper.QmsQualityGoalMapper;
import com.konli.qms.service.notify.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 质量目标服务单元测试（M8 qmsmgmt，Mockito）。
 * 聚焦：create 默认值填充、update 不存在抛错、delete、stats 达成率计算与未达标计数。
 */
@ExtendWith(MockitoExtension.class)
class QmsQualityGoalServiceImplTest {

    @Mock QmsQualityGoalMapper mapper;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock NotificationService notificationService;

    @InjectMocks QmsQualityGoalServiceImpl service;

    @Test
    @DisplayName("create:无 orgId 时回退 defaultOrgId,并填充默认值")
    void create_fillsDefaults() {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class)))
                .thenReturn("ORG1");

        QmsQualityGoal goal = new QmsQualityGoal();
        goal.setGoalName("来料合格率");
        goal.setTargetValue(new BigDecimal("99"));
        goal.setActualValue(new BigDecimal("98"));

        QmsQualityGoal created = service.create(goal);

        assertThat(created.getOrgId()).isEqualTo("ORG1");
        assertThat(created.getGoalType()).isEqualTo("QUALITY");
        assertThat(created.getUnit()).isEqualTo("%");
        assertThat(created.getTargetValue()).isEqualTo(new BigDecimal("99"));
        assertThat(created.getActualValue()).isEqualTo(new BigDecimal("98"));
        verify(mapper).insert(any(QmsQualityGoal.class));
    }

    @Test
    @DisplayName("create:已设 orgId 则不回退 defaultOrgId")
    void create_keepsProvidedOrgId() {
        QmsQualityGoal goal = new QmsQualityGoal();
        goal.setOrgId("MZ");
        goal.setGoalName("交付准时率");

        QmsQualityGoal created = service.create(goal);

        assertThat(created.getOrgId()).isEqualTo("MZ");
        verify(jdbcTemplate, never()).queryForObject(anyString(), any(Class.class), anyString());
    }

    @Test
    @DisplayName("update:目标不存在抛 BusinessException")
    void update_notFound_throws() {
        when(mapper.selectById("missing")).thenReturn(null);
        QmsQualityGoal g = new QmsQualityGoal();
        g.setId("missing");
        g.setGoalName("x");
        assertThatThrownBy(() -> service.update(g))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("目标不存在");
        verify(mapper, never()).updateById(any(QmsQualityGoal.class));
    }

    @Test
    @DisplayName("update:存在则更新字段并写回")
    void update_ok_updatesFields() {
        QmsQualityGoal exist = new QmsQualityGoal();
        exist.setId("g-1");
        exist.setGoalName("旧名");
        exist.setTargetValue(new BigDecimal("100"));
        when(mapper.selectById("g-1")).thenReturn(exist);

        QmsQualityGoal req = new QmsQualityGoal();
        req.setId("g-1");
        req.setGoalName("新名");
        req.setGoalType("DELIVERY");
        req.setTargetValue(new BigDecimal("95"));
        req.setActualValue(new BigDecimal("90"));
        req.setOwner("张三");

        QmsQualityGoal updated = service.update(req);

        ArgumentCaptor<QmsQualityGoal> cap = ArgumentCaptor.forClass(QmsQualityGoal.class);
        verify(mapper).updateById(cap.capture());
        assertThat(cap.getValue().getGoalName()).isEqualTo("新名");
        assertThat(cap.getValue().getGoalType()).isEqualTo("DELIVERY");
        assertThat(cap.getValue().getOwner()).isEqualTo("张三");
        assertThat(updated.getGoalName()).isEqualTo("新名");
    }

    @Test
    @DisplayName("delete:转发 mapper.deleteById")
    void delete_forwards() {
        service.delete("g-1");
        verify(mapper).deleteById("g-1");
    }

    @Test
    @DisplayName("stats:整体达成率与未达标计数正确(含目标为0兜底100%)")
    void stats_computesRates() {
        QmsQualityGoal g1 = new QmsQualityGoal(); // 达标
        g1.setGoalType("QUALITY");
        g1.setTargetValue(new BigDecimal("100"));
        g1.setActualValue(new BigDecimal("100"));

        QmsQualityGoal g2 = new QmsQualityGoal(); // 未达标 90%
        g2.setGoalType("DELIVERY");
        g2.setTargetValue(new BigDecimal("100"));
        g2.setActualValue(new BigDecimal("90"));

        QmsQualityGoal g3 = new QmsQualityGoal(); // 目标为0 -> 兜底100%
        g3.setGoalType("COST");
        g3.setTargetValue(BigDecimal.ZERO);
        g3.setActualValue(BigDecimal.ZERO);

        lenient().when(mapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(g1, g2, g3));

        Map<String, Object> stats = service.stats();

        assertThat(stats.get("total")).isEqualTo(3L);
        assertThat(stats.get("notReached")).isEqualTo(1L); // 仅 g2
        assertThat((BigDecimal) stats.get("overallRate"))
                .isEqualByComparingTo(new BigDecimal("96.67")); // (100+90+100)/3
        @SuppressWarnings("unchecked")
        Map<String, Integer> byType = (Map<String, Integer>) stats.get("byType");
        assertThat(byType).containsEntry("QUALITY", 1).containsEntry("DELIVERY", 1).containsEntry("COST", 1);
    }

    @Test
    @DisplayName("stats:空列表 overallRate 为 0")
    void stats_empty_returnsZeroRate() {
        lenient().when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        Map<String, Object> stats = service.stats();
        assertThat(stats.get("total")).isEqualTo(0L);
        assertThat((BigDecimal) stats.get("overallRate")).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
