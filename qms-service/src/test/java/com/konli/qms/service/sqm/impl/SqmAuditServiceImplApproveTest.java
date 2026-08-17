package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.sqm.entity.SqmAuditApproval;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.mapper.SqmAuditApprovalMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditPlanMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 供应商审核会签 approve 单元测试（M3 sqm，Mockito）。
 * 聚焦已修复的"OR 语义审批人校验"：节点绑定 approver_id 时，仅命中其一者可签。
 */
@ExtendWith(MockitoExtension.class)
class SqmAuditServiceImplApproveTest {

    @Mock SqmAuditApprovalMapper approvalMapper;
    @Mock SqmAuditPlanMapper planMapper;
    @InjectMocks SqmAuditServiceImpl service;

    static final String AUDIT = "audit-1";
    static final String ROLE = "quality";
    static final String APPROVER_A = "0be4a44d-d0f8-5a89-af6a-090f7d3af5f1";
    static final String APPROVER_B = "019f701f-041c-71d1-895e-82b66b183bce";

    SqmAuditApproval node;

    @BeforeEach
    void setUp() {
        node = new SqmAuditApproval();
        node.setAuditId(AUDIT);
        node.setApprovalRole(ROLE);
        node.setApproverId(APPROVER_A + "," + APPROVER_B); // 多人会签，OR 语义
        node.setStatus("pending");

        lenient().when(approvalMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(node);
        lenient().when(approvalMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(node));
        SqmAuditPlan plan = new SqmAuditPlan();
        plan.setId(AUDIT);
        lenient().when(planMapper.selectById(AUDIT)).thenReturn(plan);
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    @Test
    @DisplayName("指定审批人之一登录:允许会签并通过")
    void approve_specifiedApprover_succeeds() {
        CompanyContext.set(new CompanyContext.CurrentUser(APPROVER_A, "mz.qmanager", "org-1", "org"));
        service.approve(AUDIT, ROLE, true, "同意");
        ArgumentCaptor<SqmAuditApproval> cap = ArgumentCaptor.forClass(SqmAuditApproval.class);
        verify(approvalMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("done");
        assertThat(cap.getValue().getOpinion()).isEqualTo("同意");
    }

    @Test
    @DisplayName("非指定审批人登录:抛 403 越权")
    void approve_nonSpecifiedApprover_throws403() {
        CompanyContext.set(new CompanyContext.CurrentUser("other-user", "intruder", "org-1", "org"));
        assertThatThrownBy(() -> service.approve(AUDIT, ROLE, true, "越权签字"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 403);
    }

    @Test
    @DisplayName("节点不存在:抛 404")
    void approve_nodeNotFound_throws404() {
        when(approvalMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        CompanyContext.set(new CompanyContext.CurrentUser(APPROVER_A, "qm", "org-1", "org"));
        assertThatThrownBy(() -> service.approve(AUDIT, ROLE, true, "x"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 404);
    }

    @Test
    @DisplayName("已会签节点重复操作:抛 409")
    void approve_alreadyDone_throws409() {
        node.setStatus("done");
        CompanyContext.set(new CompanyContext.CurrentUser(APPROVER_A, "qm", "org-1", "org"));
        assertThatThrownBy(() -> service.approve(AUDIT, ROLE, true, "重复"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 409);
    }

    @Test
    @DisplayName("审批人留空(历史数据):退化为有权限者均可签")
    void approve_nullApprover_allowsAnyUser() {
        node.setApproverId(null);
        CompanyContext.set(new CompanyContext.CurrentUser("legacy-user", "old", "org-1", "org"));
        service.approve(AUDIT, ROLE, true, "兼容旧数据");
        ArgumentCaptor<SqmAuditApproval> cap = ArgumentCaptor.forClass(SqmAuditApproval.class);
        verify(approvalMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("done");
    }
}
