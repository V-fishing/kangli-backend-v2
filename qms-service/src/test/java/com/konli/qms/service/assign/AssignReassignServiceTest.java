package com.konli.qms.service.assign;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.ncm.entity.QmsAssignRecord;
import com.konli.qms.domain.ncm.mapper.QmsAssignRecordMapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;
import com.konli.qms.service.notify.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用指派/改派服务单元测试（M12 assign，Mockito + CompanyContext 注入）。
 * 覆盖：单人指派(写记录 + notifyUser + 返回姓名)、角色团队指派(写记录 + notifyRoles + 返回角色名)、
 * 两者皆空抛 IllegalArgumentException、reassign action 标记、currentOperator 取登录用户。
 */
@ExtendWith(MockitoExtension.class)
class AssignReassignServiceTest {

    @Mock QmsAssignRecordMapper assignRecordMapper;
    @Mock NotificationService notificationService;
    @Mock SysUserMapper sysUserMapper;
    @Mock JdbcTemplate jdbcTemplate;

    @InjectMocks AssignReassignService service;

    @AfterEach
    void clear() {
        CompanyContext.clear();
    }

    private AssignReassignService.ReassignContext ctx(DefectLaunchRequest req, boolean reassign) {
        return new AssignReassignService.ReassignContext(
                "8D", "biz-1", "8D-001", "MZ", "/ncm/8d-reports/biz-1",
                "def-1", "DR-1", req, reassign);
    }

    private DefectLaunchRequest reqWithOwner(String ownerUserId) {
        DefectLaunchRequest r = new DefectLaunchRequest();
        r.setOwnerUserId(ownerUserId);
        r.setNotifyChannels(List.of("站内弹窗"));
        r.setRemark("请尽快处理");
        return r;
    }

    @Test
    @DisplayName("单人指派:写 qms_assign_record + notifyUser + 返回姓名")
    void assign_singleUser_writesRecordAndNotifies() {
        CompanyContext.set(new CompanyContext.CurrentUser("op-1", "operator", "MZ", "org"));
        SysUser u = new SysUser();
        u.setRealName("张三");
        u.setUsername("zhangsan");
        when(sysUserMapper.selectById("owner-1")).thenReturn(u);

        String name = service.execute(ctx(reqWithOwner("owner-1"), false));

        assertThat(name).isEqualTo("张三");
        ArgumentCaptor<QmsAssignRecord> cap = ArgumentCaptor.forClass(QmsAssignRecord.class);
        verify(assignRecordMapper).insert(cap.capture());
        assertThat(cap.getValue().getAssigneeUserId()).isEqualTo("owner-1");
        assertThat(cap.getValue().getAssigneeUserName()).isEqualTo("张三");
        assertThat(cap.getValue().getAction()).isEqualTo("assign");
        assertThat(cap.getValue().getAssignerId()).isEqualTo("op-1");
        verify(notificationService).notifyUser(eq("owner-1"), any(), any(), eq("NCM_ASSIGN"), eq("biz-1"), eq("/ncm/8d-reports/biz-1"));
    }

    @Test
    @DisplayName("单人指派:userName 为空时回退 ownerUserId")
    void assign_singleUser_fallbackToUserId() {
        CompanyContext.set(new CompanyContext.CurrentUser("op-1", "operator", "MZ", "org"));
        when(sysUserMapper.selectById("owner-2")).thenReturn(null);

        String name = service.execute(ctx(reqWithOwner("owner-2"), false));
        assertThat(name).isEqualTo("owner-2");
        verify(assignRecordMapper).insert(any(QmsAssignRecord.class));
        verify(notificationService).notifyUser(eq("owner-2"), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("角色团队指派:写记录(角色码/名) + notifyRoles + 返回角色名")
    void assign_roleTeam_writesRecordAndNotifiesRoles() {
        CompanyContext.set(new CompanyContext.CurrentUser("op-1", "operator", "MZ", "org"));
        DefectLaunchRequest r = new DefectLaunchRequest();
        r.setAssignRoleCodes(List.of("sqe", "purchase"));
        r.setNotifyChannels(List.of("站内弹窗"));
        lenient().when(jdbcTemplate.query(any(String.class), any(org.springframework.jdbc.core.RowMapper.class), any(Object[].class)))
                .thenReturn(List.of("SQE", "采购"));

        String name = service.execute(ctx(r, false));
        assertThat(name).isEqualTo("SQE、采购");
        ArgumentCaptor<QmsAssignRecord> cap = ArgumentCaptor.forClass(QmsAssignRecord.class);
        verify(assignRecordMapper).insert(cap.capture());
        assertThat(cap.getValue().getAssigneeRoleCode()).isEqualTo("sqe,purchase");
        assertThat(cap.getValue().getAssigneeRoleName()).isEqualTo("SQE、采购");
        verify(notificationService).notifyRoles(eq(List.of("sqe", "purchase")), any(), any(), any(), any(), any(), eq("op-1"), eq("MZ"));
        verify(notificationService, never()).notifyUser(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("改派:action 标记为 reassign")
    void reassign_marksAction() {
        CompanyContext.set(new CompanyContext.CurrentUser("op-1", "operator", "MZ", "org"));
        when(sysUserMapper.selectById("owner-1")).thenReturn(null);

        service.execute(ctx(reqWithOwner("owner-1"), true));
        ArgumentCaptor<QmsAssignRecord> cap = ArgumentCaptor.forClass(QmsAssignRecord.class);
        verify(assignRecordMapper).insert(cap.capture());
        assertThat(cap.getValue().getAction()).isEqualTo("reassign");
    }

    @Test
    @DisplayName("ownerUserId 与 assignRoleCodes 皆空:抛 IllegalArgumentException")
    void assign_empty_throws() {
        CompanyContext.set(new CompanyContext.CurrentUser("op-1", "operator", "MZ", "org"));
        DefectLaunchRequest r = new DefectLaunchRequest();
        r.setNotifyChannels(List.of("站内弹窗"));
        assertThatThrownBy(() -> service.execute(ctx(r, false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("必须指定 ownerUserId 或 assignRoleCodes");
        verify(assignRecordMapper, never()).insert(any(QmsAssignRecord.class));
    }

    @Test
    @DisplayName("无通知渠道时默认仅站内弹窗")
    void assign_noChannels_defaultInbox() {
        CompanyContext.set(new CompanyContext.CurrentUser("op-1", "operator", "MZ", "org"));
        when(sysUserMapper.selectById("owner-1")).thenReturn(null);
        DefectLaunchRequest r = new DefectLaunchRequest();
        r.setOwnerUserId("owner-1");
        // 不设 notifyChannels

        service.execute(ctx(r, false));
        ArgumentCaptor<QmsAssignRecord> cap = ArgumentCaptor.forClass(QmsAssignRecord.class);
        verify(assignRecordMapper).insert(cap.capture());
        assertThat(cap.getValue().getNotifyChannels()).isEqualTo("站内弹窗");
        verify(notificationService).notifyUser(any(), any(), any(), any(), any(), any());
    }
}
