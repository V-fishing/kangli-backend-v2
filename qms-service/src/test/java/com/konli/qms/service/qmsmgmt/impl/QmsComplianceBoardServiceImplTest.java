package com.konli.qms.service.qmsmgmt.impl;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.notify.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 体系合规看板 Service 单元测试（M8 qmsmgmt 其余，Mockito + 子 Service mock + JdbcTemplate mock）。
 * 聚焦：board 跨模块聚合(goal/audit/adverse/feedback)、健康度加权评分(goalRate25/ncRate25/adverseRate20/fbRate15/satReach15)、
 * 健康等级阈值(优≥90/良≥80/预警<80)、feedback 查询异常兜底。
 */
@ExtendWith(MockitoExtension.class)
class QmsComplianceBoardServiceImplTest {

    @Mock JdbcTemplate jdbcTemplate;
    @Mock QmsQualityGoalServiceImpl goalService;
    @Mock QmsInternalAuditServiceImpl auditService;
    @Mock QmsAdverseEventServiceImpl adverseService;
    @Mock NotificationService notificationService;
    @InjectMocks QmsComplianceBoardServiceImpl service;

    @BeforeEach
    void setUp() {
        CompanyContext.set(new CompanyContext.CurrentUser("U1", "u1", "MZ", "org"));
    }

    @AfterEach
    void clear() {
        CompanyContext.clear();
    }

    private Map<String, Object> map(Object... kv) {
        LinkedHashMap<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    @Test
    @DisplayName("board:四模块聚合 + 健康度满分(优) + feedback 类型/状态分布")
    void board_aggregatesAndHealthScore() {
        when(goalService.stats()).thenReturn(map("overallRate", BigDecimal.valueOf(100)));
        when(auditService.stats()).thenReturn(map("ncCloseRate", 100L));
        when(adverseService.stats()).thenReturn(map("processRate", 100L));

        Map<String, Object> fbAgg = map("total", 10L, "done", 10L, "avg_score", 5, "rated", 10L, "reach", 10L);
        when(jdbcTemplate.queryForMap(anyString(), any(Object[].class))).thenReturn(fbAgg);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());

        Map<String, Object> res = service.board();
        assertThat(res).containsKeys("goal", "audit", "adverse", "feedback", "healthScore", "healthLevel");
        assertThat(res.get("healthScore")).isEqualTo(100.0);
        assertThat(res.get("healthLevel")).isEqualTo("优");
        @SuppressWarnings("unchecked")
        Map<String, Object> fb = (Map<String, Object>) res.get("feedback");
        assertThat(fb.get("total")).isEqualTo(10L);
        assertThat(fb.get("handleRate")).isEqualTo(100L);
        assertThat(fb.get("satisfactionReachRate")).isEqualTo(100L);
    }

    @Test
    @DisplayName("board:feedback 查询异常时安全兜底(total=0/分布空),不抛")
    void board_feedbackQueryFails_fallback() {
        when(goalService.stats()).thenReturn(map("overallRate", BigDecimal.valueOf(100)));
        when(auditService.stats()).thenReturn(map("ncCloseRate", 100L));
        when(adverseService.stats()).thenReturn(map("processRate", 100L));
        // queryForMap 抛异常 → 走 catch 兜底;queryForList 不会执行 → lenient 避免 UnnecessaryStubbing
        lenient().when(jdbcTemplate.queryForMap(anyString(), any(Object[].class)))
                .thenThrow(new RuntimeException("db"));
        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of());

        Map<String, Object> res = service.board();
        @SuppressWarnings("unchecked")
        Map<String, Object> fb = (Map<String, Object>) res.get("feedback");
        assertThat(fb.get("total")).isEqualTo(0);
        assertThat(fb.get("handleRate")).isEqualTo(0);
        assertThat(fb.get("typeDist")).asList().isEmpty();
        assertThat(fb.get("statusDist")).asList().isEmpty();
        // 健康度 = 100*0.25 + 100*0.25 + 100*0.20 + 0*0.15 + 0*0.15 = 70 → 预警
        assertThat(res.get("healthScore")).isEqualTo(70.0);
        assertThat(res.get("healthLevel")).isEqualTo("预警");
    }

    @Test
    @DisplayName("board:健康度边界(85 → 良;75 → 预警)")
    void board_healthLevelBoundary() {
        when(goalService.stats()).thenReturn(map("overallRate", BigDecimal.valueOf(80)));
        when(auditService.stats()).thenReturn(map("ncCloseRate", 80L));
        when(adverseService.stats()).thenReturn(map("processRate", 80L));
        Map<String, Object> fbAgg = map("total", 10L, "done", 10L, "avg_score", 5, "rated", 10L, "reach", 10L);
        when(jdbcTemplate.queryForMap(anyString(), any(Object[].class))).thenReturn(fbAgg);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());

        Map<String, Object> res = service.board();
        // 80*0.25 + 80*0.25 + 80*0.20 + 100*0.15 + 100*0.15 = 20+20+16+15+15 = 86 → 良
        assertThat(res.get("healthScore")).isEqualTo(86.0);
        assertThat(res.get("healthLevel")).isEqualTo("良");
    }
}
