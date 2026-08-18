package com.konli.qms.service.qmsmgmt.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.qmsmgmt.entity.QmsAdverseEvent;
import com.konli.qms.domain.qmsmgmt.mapper.QmsAdverseEventMapper;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 不良事件 Service 单元测试（M8 qmsmgmt 其余，Mockito + MyBatis-Plus Mapper mock + JdbcTemplate mock）。
 * 聚焦：create 默认值回退(PENDING/GENERAL/编号)+insert+通知、update 不存在抛错、
 * handle 设置状态/描述/责任人、stats 状态+严重度计数+processRate。
 */
@ExtendWith(MockitoExtension.class)
class QmsAdverseEventServiceImplTest {

    @Mock QmsAdverseEventMapper mapper;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock NotificationService notificationService;
    @InjectMocks QmsAdverseEventServiceImpl service;

    @BeforeEach
    void setUp() {
        CompanyContext.set(new CompanyContext.CurrentUser("U1", "u1", "MZ", "org"));
    }

    @AfterEach
    void clear() {
        CompanyContext.clear();
    }

    private QmsAdverseEvent ev(String id, String status, String severity) {
        QmsAdverseEvent e = new QmsAdverseEvent();
        e.setId(id);
        e.setEventNo("AE-" + id);
        e.setStatus(status);
        e.setSeverity(severity);
        return e;
    }

    @Test
    @DisplayName("page:keyword 多字段模糊 + 类型/状态过滤,返回分页结构")
    void page_filters() {
        QmsAdverseEvent e = ev("1", "PENDING", "GENERAL");
        Page<QmsAdverseEvent> p = new Page<>();
        p.setRecords(List.of(e)); p.setTotal(1);
        when(mapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class))).thenReturn(p);
        PageResult<QmsAdverseEvent> r = service.page("AE", "投诉", "PENDING", 1, 20);
        assertThat(r.getTotal()).isEqualTo(1);
        verify(mapper).selectPage(any(IPage.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("create:默认值回退(org=curOrg/status=PENDING/severity=GENERAL/编号) + insert")
    void create_defaults() {
        QmsAdverseEvent in = new QmsAdverseEvent();
        in.setEventType("投诉");
        when(mapper.insert(any(QmsAdverseEvent.class))).thenReturn(1);
        QmsAdverseEvent out = service.create(in);
        assertThat(out.getOrgId()).isEqualTo("MZ");
        assertThat(out.getStatus()).isEqualTo("PENDING");
        assertThat(out.getSeverity()).isEqualTo("GENERAL");
        assertThat(out.getEventNo()).startsWith("AE-");
        assertThat(out.getCreatedBy()).isEqualTo("U1");
        verify(mapper).insert(any(QmsAdverseEvent.class));
    }

    @Test
    @DisplayName("update:不存在抛 BusinessException;存在则编辑字段")
    void update_notFoundAndEdit() {
        when(mapper.selectById("missing")).thenReturn(null);
        QmsAdverseEvent e = new QmsAdverseEvent(); e.setId("missing");
        assertThatThrownBy(() -> service.update(e))
                .isInstanceOf(BusinessException.class).hasMessageContaining("不良事件不存在");

        QmsAdverseEvent exist = ev("1", "PENDING", "GENERAL");
        when(mapper.selectById("1")).thenReturn(exist);
        when(mapper.updateById(any(QmsAdverseEvent.class))).thenReturn(1);
        QmsAdverseEvent req = new QmsAdverseEvent();
        req.setId("1"); req.setEventType("器械故障"); req.setSeverity("SERIOUS");
        QmsAdverseEvent out = service.update(req);
        assertThat(out.getEventType()).isEqualTo("器械故障");
        assertThat(out.getSeverity()).isEqualTo("SERIOUS");
        verify(mapper).updateById(any(QmsAdverseEvent.class));
    }

    @Test
    @DisplayName("handle:设置状态/描述/责任人并 updateById")
    void handle_setsFields() {
        QmsAdverseEvent exist = ev("1", "PENDING", "GENERAL");
        when(mapper.selectById("1")).thenReturn(exist);
        when(mapper.updateById(any(QmsAdverseEvent.class))).thenReturn(1);
        service.handle("1", "DONE", "已处理", "张三");
        assertThat(exist.getStatus()).isEqualTo("DONE");
        assertThat(exist.getHandleDesc()).isEqualTo("已处理");
        assertThat(exist.getOwner()).isEqualTo("张三");
        verify(mapper).updateById(any(QmsAdverseEvent.class));
    }

    @Test
    @DisplayName("stats:状态(PENDING/HANDLING/DONE)+严重度(GENERAL/SERIOUS/CRITICAL)计数 + processRate")
    void stats_countsAndRate() {
        QmsAdverseEvent p = ev("1", "PENDING", "GENERAL");
        QmsAdverseEvent h = ev("2", "HANDLING", "SERIOUS");
        QmsAdverseEvent d = ev("3", "DONE", "CRITICAL");
        when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(p, h, d));
        Map<String, Object> r = service.stats();
        assertThat(((Number) r.get("total")).longValue()).isEqualTo(3L);
        assertThat(r.get("pending")).isEqualTo(1L);
        assertThat(r.get("handling")).isEqualTo(1L);
        assertThat(r.get("done")).isEqualTo(1L);
        assertThat(r.get("general")).isEqualTo(1L);
        assertThat(r.get("serious")).isEqualTo(1L);
        assertThat(r.get("critical")).isEqualTo(1L);
        // processRate = (handling + done) / total = 2/3 ≈ 67
        assertThat(r.get("processRate")).isEqualTo(67L);
    }
}
