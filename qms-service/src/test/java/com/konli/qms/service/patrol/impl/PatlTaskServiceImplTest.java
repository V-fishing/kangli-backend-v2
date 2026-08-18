package com.konli.qms.service.patrol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.patrol.entity.PatlAbnormal;
import com.konli.qms.domain.patrol.entity.PatlRecord;
import com.konli.qms.domain.patrol.entity.PatlRoute;
import com.konli.qms.domain.patrol.entity.PatlTask;
import com.konli.qms.domain.patrol.mapper.PatlAbnormalMapper;
import com.konli.qms.domain.patrol.mapper.PatlCheckpointMapper;
import com.konli.qms.domain.patrol.mapper.PatlRecordMapper;
import com.konli.qms.domain.patrol.mapper.PatlRouteMapper;
import com.konli.qms.domain.patrol.mapper.PatlTaskMapper;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.patrol.PatlArchiveService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 巡检任务服务单元测试（M7 patrol，Mockito）。
 * 聚焦状态机与异常计数纯逻辑：create / submitRecord / close / closeAbnormal。
 */
@ExtendWith(MockitoExtension.class)
class PatlTaskServiceImplTest {

    @Mock PatlTaskMapper taskMapper;
    @Mock PatlRecordMapper recordMapper;
    @Mock PatlAbnormalMapper abnormalMapper;
    @Mock PatlRouteMapper routeMapper;
    @Mock PatlCheckpointMapper checkpointMapper;
    @Mock NotificationService notificationService;
    @Mock PatlArchiveService patlArchiveService;

    @InjectMocks PatlTaskServiceImpl service;

    static final String ROUTE = "route-1";
    static final String TASK = "task-1";

    PatlTask task;

    @BeforeEach
    void setUp() {
        task = new PatlTask();
        task.setId(TASK);
        task.setOrgId("MZ");
        task.setRouteId(ROUTE);
        task.setStatus("待巡检");
        task.setTotalPoints(2);
        task.setDonePoints(0);
        task.setAbnormalCount(0);

        lenient().when(taskMapper.selectById(TASK)).thenReturn(task);
        lenient().when(routeMapper.selectById(ROUTE)).thenReturn(new PatlRoute());
        // 默认:任务有 2 个检查点
        lenient().when(checkpointMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(new PatlCheckpointStub(), new PatlCheckpointStub()));
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    @Test
    @DisplayName("create:路线不存在抛 404")
    void create_routeNotFound_throws404() {
        when(routeMapper.selectById("bad")).thenReturn(null);
        assertThatThrownBy(() -> service.create("MZ", "bad", "早班", null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 404);
        verify(taskMapper, never()).insert(any(PatlTask.class));
    }

    @Test
    @DisplayName("create:路线存在则落库并触发创建通知")
    void create_ok_insertsAndNotifies() {
        PatlRoute route = new PatlRoute();
        route.setId(ROUTE);
        when(routeMapper.selectById(ROUTE)).thenReturn(route);
        when(checkpointMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(new PatlCheckpointStub()));

        PatlTask created = service.create("MZ", ROUTE, "早班", null);

        assertThat(created.getStatus()).isEqualTo("待巡检");
        assertThat(created.getTotalPoints()).isEqualTo(1);
        verify(taskMapper).insert(any(PatlTask.class));
        verify(notificationService).notify(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("submitRecord:任务不存在抛 404")
    void submitRecord_taskNotFound_throws404() {
        when(taskMapper.selectById("missing")).thenReturn(null);
        assertThatThrownBy(() -> service.submitRecord("missing", "cp1", "点位A", "正常", "", "op1"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 404);
    }

    @Test
    @DisplayName("submitRecord:任务已完成抛 400")
    void submitRecord_taskDone_throws400() {
        task.setStatus("已完成");
        assertThatThrownBy(() -> service.submitRecord(TASK, "cp1", "点位A", "正常", "", "op1"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 400);
    }

    @Test
    @DisplayName("submitRecord:正常结果 -> donePoints+1,不创建异常,未全完成")
    void submitRecord_normal_incrementsDone() {
        service.submitRecord(TASK, "cp1", "点位A", "正常", "", "op1");

        ArgumentCaptor<PatlTask> cap = ArgumentCaptor.forClass(PatlTask.class);
        verify(taskMapper).updateById(cap.capture());
        assertThat(cap.getValue().getDonePoints()).isEqualTo(1);
        assertThat(cap.getValue().getStatus()).isEqualTo("待巡检"); // 未全完成
        verify(recordMapper).insert(any(PatlRecord.class));
        verify(abnormalMapper, never()).insert(any(PatlAbnormal.class));
        // 每次提交末尾无条件触发归档
        verify(patlArchiveService).archive(TASK);
    }

    @Test
    @DisplayName("submitRecord:异常结果 -> abnormalCount+1 并创建异常记录")
    void submitRecord_abnormal_createsAbnormal() {
        service.submitRecord(TASK, "cp1", "点位A", "异常", "螺丝松动", "op1");

        ArgumentCaptor<PatlTask> taskCap = ArgumentCaptor.forClass(PatlTask.class);
        verify(taskMapper).updateById(taskCap.capture());
        assertThat(taskCap.getValue().getAbnormalCount()).isEqualTo(1);

        ArgumentCaptor<PatlAbnormal> abCap = ArgumentCaptor.forClass(PatlAbnormal.class);
        verify(abnormalMapper).insert(abCap.capture());
        assertThat(abCap.getValue().getStatus()).isEqualTo("待处理");
        assertThat(abCap.getValue().getSeverity()).isEqualTo("一般");
        verify(notificationService, times(1)).notify(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("submitRecord:全部点检完成 -> 状态置已完成并触发归档")
    void submitRecord_allDone_marksCompletedAndArchives() {
        // totalPoints=2;先提交一次正常,再提交一次使 done=2 完成
        service.submitRecord(TASK, "cp1", "点位A", "正常", "", "op1");
        service.submitRecord(TASK, "cp2", "点位B", "正常", "", "op1");

        ArgumentCaptor<PatlTask> cap = ArgumentCaptor.forClass(PatlTask.class);
        // 第二次提交产生最后一次 updateById
        verify(taskMapper, times(2)).updateById(cap.capture());
        PatlTask last = cap.getValue();
        assertThat(last.getDonePoints()).isEqualTo(2);
        assertThat(last.getStatus()).isEqualTo("已完成");
        // 每次 submitRecord 末尾都触发归档,两次提交共 2 次
        verify(patlArchiveService, times(2)).archive(TASK);
    }

    @Test
    @DisplayName("closeAbnormal:异常不存在抛 404")
    void closeAbnormal_notFound_throws404() {
        when(abnormalMapper.selectById("bad")).thenReturn(null);
        assertThatThrownBy(() -> service.closeAbnormal("bad", "已处理"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 404);
    }

    @Test
    @DisplayName("closeAbnormal:正常关闭 -> 状态已关闭并填处理信息")
    void closeAbnormal_ok_closes() {
        PatlAbnormal ab = new PatlAbnormal();
        ab.setId("ab-1");
        ab.setStatus("待处理");
        when(abnormalMapper.selectById("ab-1")).thenReturn(ab);
        CompanyContext.set(new CompanyContext.CurrentUser("op-9", "inspector", "MZ", "org"));

        service.closeAbnormal("ab-1", "已复检合格");

        ArgumentCaptor<PatlAbnormal> cap = ArgumentCaptor.forClass(PatlAbnormal.class);
        verify(abnormalMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("已关闭");
        assertThat(cap.getValue().getHandleRemark()).isEqualTo("已复检合格");
        assertThat(cap.getValue().getHandledBy()).isEqualTo("op-9");
    }

    // 轻量桩:仅用于撑起检查点列表 size,无实际字段依赖
    static class PatlCheckpointStub extends com.konli.qms.domain.patrol.entity.PatlCheckpoint {
        public PatlCheckpointStub() { setId("cp-stub"); }
    }
}
