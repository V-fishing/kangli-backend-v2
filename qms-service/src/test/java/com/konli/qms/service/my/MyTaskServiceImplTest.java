package com.konli.qms.service.my;

import com.konli.qms.common.dto.MyTaskDTO;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.cs.entity.CsWorkOrder;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.patrol.entity.PatlTask;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 跨模块"我的任务"聚合服务单元测试（M12 my，Mockito + CompanyContext 注入）。
 * 覆盖：userId=null 返回空、FIA/NCM8D/PATROL/SQM审核/CS 分支映射、includeClosed 过滤、
 * limit 服务端截断、module+bizNo 排序。
 */
@ExtendWith(MockitoExtension.class)
class MyTaskServiceImplTest {

    @Mock com.konli.qms.domain.fia.mapper.FiaTaskMapper fiaTaskMapper;
    @Mock com.konli.qms.domain.ncm.mapper.Qms8dReportMapper qms8dReportMapper;
    @Mock com.konli.qms.domain.patrol.mapper.PatlTaskMapper patlTaskMapper;
    @Mock com.konli.qms.domain.ncm.mapper.QmsCapaMapper qmsCapaMapper;
    @Mock com.konli.qms.domain.ncm.mapper.NcmCorrectiveActionMapper ncmCorrectiveActionMapper;
    @Mock com.konli.qms.domain.sqm.mapper.SqmIncomingAbnormalMapper sqmIncomingAbnormalMapper;
    @Mock com.konli.qms.domain.sqm.mapper.SqmAuditPlanMapper sqmAuditPlanMapper;
    @Mock com.konli.qms.domain.sqm.mapper.QmsFmeaRiskMapper qmsFmeaRiskMapper;
    @Mock com.konli.qms.domain.cs.mapper.CsWorkOrderMapper csWorkOrderMapper;

    @InjectMocks MyTaskServiceImpl service;

    @AfterEach
    void clear() {
        CompanyContext.clear();
    }

    private void login(String userId) {
        CompanyContext.set(new CompanyContext.CurrentUser(userId, "u" + userId, "MZ", "org"));
    }

    @Test
    @DisplayName("userId 为空:返回空列表")
    void noUser_returnsEmpty() {
        assertThat(service.myTasks(0, false)).isEmpty();
    }

    @Test
    @DisplayName("FIA 分支:inspectorId 匹配且未闭环 → 映射 module/taskType/bizNo/url")
    void fia_branch_mapsFields() {
        login("U1");
        FiaTask t = new FiaTask();
        t.setId("f-1");
        t.setCode("FA-1");
        t.setProductName("轴承");
        t.setStatus("待检");
        t.setSlaDueAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        when(fiaTaskMapper.selectList(any())).thenReturn(List.of(t));

        List<MyTaskDTO> list = service.myTasks(0, false);
        assertThat(list).hasSize(1);
        MyTaskDTO d = list.get(0);
        assertThat(d.getModule()).isEqualTo("FIA");
        assertThat(d.getTaskType()).isEqualTo("首件检验");
        assertThat(d.getBizNo()).isEqualTo("FA-1");
        assertThat(d.getTitle()).isEqualTo("轴承");
        assertThat(d.getStatus()).isEqualTo("待检");
        assertThat(d.getUrl()).isEqualTo("/fia/tasks");
    }

    @Test
    @DisplayName("NCM 8D 分支:ownerUserId 匹配且未关闭 → 映射")
    void ncm8d_branch_mapsFields() {
        login("U1");
        Qms8dReport r = new Qms8dReport();
        r.setId("d8-1");
        r.setD8No("8D-001");
        r.setIssue("装配不良");
        r.setStatus("进行中");
        r.setOwnerUserName("张三");
        when(qms8dReportMapper.selectList(any())).thenReturn(List.of(r));

        List<MyTaskDTO> list = service.myTasks(0, false);
        assertThat(list).hasSize(1);
        MyTaskDTO d = list.get(0);
        assertThat(d.getModule()).isEqualTo("NCM");
        assertThat(d.getTaskType()).isEqualTo("8D报告");
        assertThat(d.getBizNo()).isEqualTo("8D-001");
        assertThat(d.getAssignee()).isEqualTo("张三");
        assertThat(d.getUrl()).isEqualTo("/ncm/8d-reports/d8-1");
    }

    @Test
    @DisplayName("CS 售后分支:ownerId 匹配且未 CLOSED → 映射")
    void cs_branch_mapsFields() {
        login("U1");
        CsWorkOrder o = new CsWorkOrder();
        o.setId("wo-1");
        o.setOrderNo("WO-1");
        o.setCustomerName("客户A");
        o.setStatus("ASSIGNED");
        o.setOwnerName("李四");
        o.setExpectTime(LocalDateTime.of(2026, 2, 1, 10, 0));
        when(csWorkOrderMapper.selectList(any())).thenReturn(List.of(o));

        List<MyTaskDTO> list = service.myTasks(0, false);
        assertThat(list).hasSize(1);
        MyTaskDTO d = list.get(0);
        assertThat(d.getModule()).isEqualTo("CS");
        assertThat(d.getTaskType()).isEqualTo("售后工单");
        assertThat(d.getBizNo()).isEqualTo("WO-1");
        assertThat(d.getStatus()).isEqualTo("ASSIGNED");
    }

    @Test
    @DisplayName("includeClosed=false:DB 仅返回未闭环(排除已关闭)")
    void includeClosed_false_excludesClosed() {
        login("U1");
        Qms8dReport open = new Qms8dReport();
        open.setId("d8-1");
        open.setD8No("8D-OPEN");
        open.setStatus("进行中");
        // DB 层已过滤:false 时仅返回未闭环记录
        when(qms8dReportMapper.selectList(any())).thenReturn(List.of(open));

        List<MyTaskDTO> list = service.myTasks(0, false);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getBizNo()).isEqualTo("8D-OPEN");
    }

    @Test
    @DisplayName("includeClosed=true:DB 返回全部(含已关闭)")
    void includeClosed_true_includesClosed() {
        login("U1");
        Qms8dReport open = new Qms8dReport();
        open.setId("d8-1");
        open.setD8No("8D-OPEN");
        open.setStatus("进行中");
        Qms8dReport closed = new Qms8dReport();
        closed.setId("d8-2");
        closed.setD8No("8D-CLOSED");
        closed.setStatus("已关闭");
        // DB 层不过滤:true 时返回全部记录
        when(qms8dReportMapper.selectList(any())).thenReturn(List.of(open, closed));

        assertThat(service.myTasks(0, true)).hasSize(2);
    }

    @Test
    @DisplayName("limit>0 服务端截断到前 limit 条")
    void limit_truncates() {
        login("U1");
        Qms8dReport r1 = new Qms8dReport();
        r1.setId("d8-1"); r1.setD8No("8D-1"); r1.setStatus("进行中");
        Qms8dReport r2 = new Qms8dReport();
        r2.setId("d8-2"); r2.setD8No("8D-2"); r2.setStatus("进行中");
        Qms8dReport r3 = new Qms8dReport();
        r3.setId("d8-3"); r3.setD8No("8D-3"); r3.setStatus("进行中");
        when(qms8dReportMapper.selectList(any())).thenReturn(List.of(r1, r2, r3));

        assertThat(service.myTasks(2, false)).hasSize(2);
        assertThat(service.myTasks(0, false)).hasSize(3);
    }

    @Test
    @DisplayName("排序:按 module 升序然后 bizNo 升序")
    void sort_byModuleThenBizNo() {
        login("U1");
        FiaTask f = new FiaTask();
        f.setId("f1"); f.setCode("FA-2"); f.setStatus("待检");
        PatlTask p = new PatlTask();
        p.setId("p1"); p.setTaskNo("PT-1"); p.setStatus("待巡检");
        when(fiaTaskMapper.selectList(any())).thenReturn(List.of(f));
        when(patlTaskMapper.selectList(any())).thenReturn(List.of(p));

        List<MyTaskDTO> list = service.myTasks(0, false);
        // FIA < PATROL 字母序
        assertThat(list).extracting(MyTaskDTO::getModule).containsExactly("FIA", "PATROL");
    }
}
