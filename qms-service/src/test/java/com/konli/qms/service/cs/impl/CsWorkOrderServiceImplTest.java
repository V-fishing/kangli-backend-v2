package com.konli.qms.service.cs.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.cs.entity.CsWorkOrder;
import com.konli.qms.domain.cs.mapper.CsWorkOrderMapper;
import com.konli.qms.service.cs.CsWorkOrderService;
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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 售后工单 Service 单元测试（M6 cs，Mockito + MyBatis-Plus Mapper mock + JdbcTemplate mock）。
 * 聚焦：状态机(PENDING→ASSIGNED→DONE→CLOSED)业务校验、create 默认值回退、
 * dashboard 计数聚合、satisfactionStats 异常兜底。
 * 注意：CompanyContext 非 null 才能取到 org/user，@BeforeEach 注入登录用户，@AfterEach clear 防泄漏。
 */
@ExtendWith(MockitoExtension.class)
class CsWorkOrderServiceImplTest {

    @Mock CsWorkOrderMapper mapper;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock NotificationService notificationService;
    @InjectMocks CsWorkOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        CompanyContext.set(new CompanyContext.CurrentUser("U1", "u1", "MZ", "org"));
    }

    @AfterEach
    void clear() {
        CompanyContext.clear();
    }

    private CsWorkOrder wo(String id, String status) {
        CsWorkOrder o = new CsWorkOrder();
        o.setId(id);
        o.setOrderNo("WO-" + id);
        o.setStatus(status);
        o.setCustomerName("客户" + id);
        return o;
    }

    @Test
    @DisplayName("page:keyword 多字段模糊 + 状态/类型/优先级过滤,返回分页结构")
    void page_filtersAndReturnsPage() {
        CsWorkOrder o = wo("1", "PENDING");
        Page<CsWorkOrder> p = new Page<>();
        p.setRecords(List.of(o));
        p.setTotal(1);
        when(mapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class))).thenReturn(p);

        PageResult<CsWorkOrder> res = service.page("客户", "REPAIR", "PENDING", "URGENT", 1, 20);
        assertThat(res.getTotal()).isEqualTo(1);
        assertThat(res.getRecords()).hasSize(1);
        verify(mapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("create:默认值回退(org/status= PENDING/woType=REPAIR/priority=NORMAL/orderNo) + insert + createdBy")
    void create_defaultsAndInsert() {
        CsWorkOrder in = new CsWorkOrder();
        in.setCustomerName("测试客户");
        // orgId 不设置 → curOrg()=MZ
        when(mapper.insert(any(CsWorkOrder.class))).thenReturn(1);

        CsWorkOrder out = service.create(in);
        assertThat(out.getOrgId()).isEqualTo("MZ");
        assertThat(out.getStatus()).isEqualTo("PENDING");
        assertThat(out.getWoType()).isEqualTo("REPAIR");
        assertThat(out.getPriority()).isEqualTo("NORMAL");
        assertThat(out.getOrderNo()).startsWith("WO-");
        assertThat(out.getCreatedBy()).isEqualTo("U1");
        verify(mapper).insert(any(CsWorkOrder.class));
    }

    @Test
    @DisplayName("create:orgId 已设不覆盖")
    void create_orgIdNotOverridden() {
        CsWorkOrder in = new CsWorkOrder();
        in.setOrgId("SZ");
        in.setStatus("ASSIGNED");
        in.setWoType("INSTALL");
        in.setPriority("LOW");
        when(mapper.insert(any(CsWorkOrder.class))).thenReturn(1);

        CsWorkOrder out = service.create(in);
        assertThat(out.getOrgId()).isEqualTo("SZ");
        assertThat(out.getStatus()).isEqualTo("ASSIGNED");
        assertThat(out.getWoType()).isEqualTo("INSTALL");
        assertThat(out.getPriority()).isEqualTo("LOW");
    }

    @Test
    @DisplayName("update:工单不存在抛 BusinessException")
    void update_notFound_throws() {
        when(mapper.selectById("missing")).thenReturn(null);
        CsWorkOrder o = new CsWorkOrder();
        o.setId("missing");
        assertThatThrownBy(() -> service.update(o))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("工单不存在");
    }

    @Test
    @DisplayName("update:存在则仅编辑基础字段并 updateById")
    void update_editsBaseFields() {
        CsWorkOrder exist = wo("1", "PENDING");
        when(mapper.selectById("1")).thenReturn(exist);
        when(mapper.updateById(any(CsWorkOrder.class))).thenReturn(1);

        CsWorkOrder req = new CsWorkOrder();
        req.setId("1");
        req.setCustomerName("新客户");
        req.setWoType("INSTALL");
        req.setPriority("URGENT");
        CsWorkOrder out = service.update(req);

        assertThat(out.getCustomerName()).isEqualTo("新客户");
        assertThat(out.getWoType()).isEqualTo("INSTALL");
        assertThat(out.getPriority()).isEqualTo("URGENT");
        // 状态不被 update 回退(仍保留原 PENDING)
        assertThat(out.getStatus()).isEqualTo("PENDING");
        verify(mapper).updateById(any(CsWorkOrder.class));
    }

    @Test
    @DisplayName("assign:非 PENDING 抛异常;PENDING 成功置 ASSIGNED + owner")
    void assign_stateMachine() {
        // 非 PENDING
        when(mapper.selectById("a")).thenReturn(wo("a", "ASSIGNED"));
        assertThatThrownBy(() -> service.assign("a", "R1", "负责人1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅待派单工单可派单");

        // PENDING
        CsWorkOrder pending = wo("b", "PENDING");
        when(mapper.selectById("b")).thenReturn(pending);
        when(mapper.updateById(any(CsWorkOrder.class))).thenReturn(1);
        service.assign("b", "R1", "负责人1");
        assertThat(pending.getStatus()).isEqualTo("ASSIGNED");
        assertThat(pending.getOwnerId()).isEqualTo("R1");
        assertThat(pending.getOwnerName()).isEqualTo("负责人1");
        verify(mapper).updateById(any(CsWorkOrder.class));
    }

    @Test
    @DisplayName("complete:非 ASSIGNED 抛异常;ASSIGNED→DONE 回填处理详情")
    void complete_stateMachine() {
        when(mapper.selectById("c")).thenReturn(wo("c", "PENDING"));
        assertThatThrownBy(() -> service.complete("c", "修好了"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅处理中工单可标记完成");

        CsWorkOrder assigned = wo("d", "ASSIGNED");
        when(mapper.selectById("d")).thenReturn(assigned);
        when(mapper.updateById(any(CsWorkOrder.class))).thenReturn(1);
        service.complete("d", "已维修完成");
        assertThat(assigned.getStatus()).isEqualTo("DONE");
        assertThat(assigned.getHandleDetail()).isEqualTo("已维修完成");
    }

    @Test
    @DisplayName("close:非 DONE 抛异常;DONE→CLOSED 回填满意度")
    void close_stateMachine() {
        when(mapper.selectById("e")).thenReturn(wo("e", "ASSIGNED"));
        assertThatThrownBy(() -> service.close("e", 5, "好"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅已完成工单可评价闭环");

        CsWorkOrder done = wo("f", "DONE");
        when(mapper.selectById("f")).thenReturn(done);
        when(mapper.updateById(any(CsWorkOrder.class))).thenReturn(1);
        service.close("f", 4, "满意");
        assertThat(done.getStatus()).isEqualTo("CLOSED");
        assertThat(done.getSatisfaction()).isEqualTo(4);
        assertThat(done.getSatisfactionComment()).isEqualTo("满意");
    }

    @Test
    @DisplayName("dashboard:按状态计数 + 紧急待派计数 + monthly 异常兜底空")
    void dashboard_countsAndMonthlyFallback() {
        CsWorkOrder p1 = wo("1", "PENDING"); p1.setPriority("URGENT");
        CsWorkOrder p2 = wo("2", "PENDING"); p2.setPriority("NORMAL");
        CsWorkOrder a1 = wo("3", "ASSIGNED");
        CsWorkOrder d1 = wo("4", "DONE");
        CsWorkOrder c1 = wo("5", "CLOSED");
        when(mapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(p1, p2, a1, d1, c1));
        // monthly queryForList 抛异常 → 走兜底 emptyList
        lenient().when(jdbcTemplate.queryForList(anyString())).thenThrow(new RuntimeException("db"));

        var res = service.dashboard();
        assertThat(res.get("pending")).isEqualTo(2L);
        assertThat(res.get("assigned")).isEqualTo(1L);
        assertThat(res.get("done")).isEqualTo(1L);
        assertThat(res.get("closed")).isEqualTo(1L);
        assertThat(res.get("urgentPending")).isEqualTo(1L);
        assertThat(res.get("total")).isEqualTo(5L);
        assertThat(res.get("monthly")).asList().isEmpty();
    }

    @Test
    @DisplayName("satisfactionStats:queryForMap 异常时安全兜底(avgScore=0/rated=0/分布全0)")
    void satisfactionStats_queryMapExceptionFallback() {
        // queryForMap 抛异常 → catch 兜底默认值
        lenient().when(jdbcTemplate.queryForMap(anyString())).thenThrow(new RuntimeException("db"));
        lenient().when(jdbcTemplate.queryForList(anyString())).thenThrow(new RuntimeException("db"));

        var res = service.satisfactionStats();
        assertThat(res.get("avgScore")).isEqualTo(0);
        assertThat(res.get("rated")).isEqualTo(0L);
        @SuppressWarnings("unchecked")
        java.util.Map<Integer, Long> dist = (java.util.Map<Integer, Long>) res.get("distribution");
        assertThat(dist).containsEntry(1, 0L).containsEntry(5, 0L);
        assertThat(res.get("monthly")).asList().isEmpty();
    }
}
