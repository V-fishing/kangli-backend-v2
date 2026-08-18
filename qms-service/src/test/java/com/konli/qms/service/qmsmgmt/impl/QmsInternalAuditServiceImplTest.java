package com.konli.qms.service.qmsmgmt.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.qmsmgmt.entity.QmsAuditNc;
import com.konli.qms.domain.qmsmgmt.entity.QmsInternalAudit;
import com.konli.qms.domain.qmsmgmt.mapper.QmsAuditNcMapper;
import com.konli.qms.domain.qmsmgmt.mapper.QmsInternalAuditMapper;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.qmsmgmt.QmsInternalAuditService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 内审计划/不符合项 Service 单元测试（M8 qmsmgmt 其余，Mockito + MyBatis-Plus Mapper mock + JdbcTemplate mock）。
 * 聚焦：create 默认值回退、update/delete(级联 NC)、状态机(PLANNED→ONGOING→DONE→CLOSED 单向+幂等)、
 * saveNc 新/改 + 关闭置 closedAt、stats 计数 + ncCloseRate。
 */
@ExtendWith(MockitoExtension.class)
class QmsInternalAuditServiceImplTest {

    @Mock QmsInternalAuditMapper mapper;
    @Mock QmsAuditNcMapper ncMapper;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock NotificationService notificationService;
    @InjectMocks QmsInternalAuditServiceImpl service;

    @BeforeEach
    void setUp() {
        CompanyContext.set(new CompanyContext.CurrentUser("U1", "u1", "MZ", "org"));
    }

    @AfterEach
    void clear() {
        CompanyContext.clear();
    }

    private QmsInternalAudit audit(String id, String status) {
        QmsInternalAudit a = new QmsInternalAudit();
        a.setId(id);
        a.setAuditNo("IA-" + id);
        a.setStatus(status);
        return a;
    }

    @Test
    @DisplayName("page:keyword 多字段模糊 + 状态过滤,返回分页结构")
    void page_filters() {
        QmsInternalAudit a = audit("1", "PLANNED");
        Page<QmsInternalAudit> p = new Page<>();
        p.setRecords(List.of(a)); p.setTotal(1);
        when(mapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class))).thenReturn(p);
        PageResult<QmsInternalAudit> r = service.page("IA", "PLANNED", 1, 20);
        assertThat(r.getTotal()).isEqualTo(1);
        verify(mapper).selectPage(any(IPage.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("create:默认值回退(org=curOrg/status=PLANNED/auditNo=IA-时间戳) + insert + createdBy")
    void create_defaults() {
        QmsInternalAudit in = new QmsInternalAudit();
        in.setAuditName("年度内审");
        when(mapper.insert(any(QmsInternalAudit.class))).thenReturn(1);
        QmsInternalAudit out = service.create(in);
        assertThat(out.getOrgId()).isEqualTo("MZ");
        assertThat(out.getStatus()).isEqualTo("PLANNED");
        assertThat(out.getAuditNo()).startsWith("IA-");
        assertThat(out.getCreatedBy()).isEqualTo("U1");
        verify(mapper).insert(any(QmsInternalAudit.class));
    }

    @Test
    @DisplayName("update:不存在抛 BusinessException;存在则编辑字段")
    void update_notFoundAndEdit() {
        when(mapper.selectById("missing")).thenReturn(null);
        QmsInternalAudit a = new QmsInternalAudit(); a.setId("missing");
        assertThatThrownBy(() -> service.update(a))
                .isInstanceOf(BusinessException.class).hasMessageContaining("内审不存在");

        QmsInternalAudit exist = audit("1", "PLANNED");
        when(mapper.selectById("1")).thenReturn(exist);
        when(mapper.updateById(any(QmsInternalAudit.class))).thenReturn(1);
        QmsInternalAudit req = new QmsInternalAudit();
        req.setId("1"); req.setAuditName("改名"); req.setStatus("ONGOING");
        QmsInternalAudit out = service.update(req);
        assertThat(out.getAuditName()).isEqualTo("改名");
        assertThat(out.getStatus()).isEqualTo("ONGOING");
        verify(mapper).updateById(any(QmsInternalAudit.class));
    }

    @Test
    @DisplayName("delete:删除内审并级联软删其不符合项")
    void delete_cascadeNc() {
        service.delete("1");
        verify(mapper).deleteById("1");
        verify(ncMapper).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("advance:非法流转抛错;合法单向推进;停留原状态幂等")
    void advance_stateMachine() {
        // 非法: PLANNED → CLOSED
        when(mapper.selectById("a")).thenReturn(audit("a", "PLANNED"));
        assertThatThrownBy(() -> service.advance("a", "CLOSED"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("非法的状态流转");

        // 合法: PLANNED → ONGOING
        QmsInternalAudit a = audit("b", "PLANNED");
        when(mapper.selectById("b")).thenReturn(a);
        when(mapper.updateById(any(QmsInternalAudit.class))).thenReturn(1);
        service.advance("b", "ONGOING");
        assertThat(a.getStatus()).isEqualTo("ONGOING");

        // 幂等: ONGOING → ONGOING 不抛
        QmsInternalAudit c = audit("c", "ONGOING");
        when(mapper.selectById("c")).thenReturn(c);
        service.advance("c", "ONGOING");
        assertThat(c.getStatus()).isEqualTo("ONGOING");
    }

    @Test
    @DisplayName("saveNc:新增默认 OPEN/MINOR/编号 + 改不存在抛错 + 关闭置 closedAt")
    void saveNc_createAndUpdate() {
        QmsAuditNc nc = new QmsAuditNc();
        nc.setAuditId("A1");
        when(ncMapper.insert(any(QmsAuditNc.class))).thenReturn(1);
        QmsAuditNc created = service.saveNc(nc);
        assertThat(created.getStatus()).isEqualTo("OPEN");
        assertThat(created.getSeverity()).isEqualTo("MINOR");
        assertThat(created.getNcNo()).startsWith("NC-");
        verify(ncMapper).insert(any(QmsAuditNc.class));

        // 改: 不存在抛错
        QmsAuditNc upd = new QmsAuditNc(); upd.setId("missing");
        when(ncMapper.selectById("missing")).thenReturn(null);
        assertThatThrownBy(() -> service.saveNc(upd))
                .isInstanceOf(BusinessException.class).hasMessageContaining("不符合项不存在");

        // 改: 置 CLOSED 自动填 closedAt
        QmsAuditNc exist = new QmsAuditNc(); exist.setId("n1");
        when(ncMapper.selectById("n1")).thenReturn(exist);
        when(ncMapper.updateById(any(QmsAuditNc.class))).thenReturn(1);
        QmsAuditNc req = new QmsAuditNc(); req.setId("n1"); req.setStatus("CLOSED");
        service.saveNc(req);
        assertThat(exist.getStatus()).isEqualTo("CLOSED");
        assertThat(exist.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("stats:审计状态计数 + NC 状态计数 + ncCloseRate 比例")
    void stats_countsAndRate() {
        QmsInternalAudit p = audit("1", "PLANNED");
        QmsInternalAudit o = audit("2", "ONGOING");
        QmsInternalAudit d = audit("3", "DONE");
        QmsInternalAudit c = audit("4", "CLOSED");
        when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(p, o, d, c));

        QmsAuditNc nOpen = new QmsAuditNc(); nOpen.setStatus("OPEN");
        QmsAuditNc nProg = new QmsAuditNc(); nProg.setStatus("IN_PROGRESS");
        QmsAuditNc nClosed1 = new QmsAuditNc(); nClosed1.setStatus("CLOSED");
        QmsAuditNc nClosed2 = new QmsAuditNc(); nClosed2.setStatus("CLOSED");
        when(ncMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(nOpen, nProg, nClosed1, nClosed2));

        Map<String, Object> r = service.stats();
        assertThat(r.get("planned")).isEqualTo(1L);
        assertThat(r.get("ongoing")).isEqualTo(1L);
        assertThat(r.get("done")).isEqualTo(1L);
        assertThat(r.get("closed")).isEqualTo(1L);
        assertThat(r.get("ncTotal")).isEqualTo(4L);
        assertThat(r.get("ncOpen")).isEqualTo(1L);
        assertThat(r.get("ncInProgress")).isEqualTo(1L);
        assertThat(r.get("ncClosed")).isEqualTo(2L);
        assertThat(r.get("ncCloseRate")).isEqualTo(50L); // 2/4*100
    }
}
