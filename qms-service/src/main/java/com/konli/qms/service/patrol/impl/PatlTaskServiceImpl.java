package com.konli.qms.service.patrol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.patrol.entity.PatlAbnormal;
import com.konli.qms.domain.patrol.entity.PatlCheckpoint;
import com.konli.qms.domain.patrol.entity.PatlRecord;
import com.konli.qms.domain.patrol.entity.PatlRoute;
import com.konli.qms.domain.patrol.entity.PatlTask;
import com.konli.qms.domain.patrol.mapper.PatlAbnormalMapper;
import com.konli.qms.domain.patrol.mapper.PatlCheckpointMapper;
import com.konli.qms.domain.patrol.mapper.PatlRecordMapper;
import com.konli.qms.domain.patrol.mapper.PatlRouteMapper;
import com.konli.qms.domain.patrol.mapper.PatlTaskMapper;
import com.konli.qms.service.patrol.PatlTaskService;
import com.konli.qms.service.patrol.dto.PatlTaskVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatlTaskServiceImpl implements PatlTaskService {

    private final PatlTaskMapper patlTaskMapper;
    private final PatlRecordMapper patlRecordMapper;
    private final PatlAbnormalMapper patlAbnormalMapper;
    private final PatlRouteMapper patlRouteMapper;
    private final PatlCheckpointMapper patlCheckpointMapper;

    @Override
    public List<PatlTask> list() {
        return patlTaskMapper.selectList(null);
    }

    @Override
    public PatlTaskVo get(String id) {
        PatlTaskVo vo = new PatlTaskVo();
        vo.setTask(patlTaskMapper.selectById(id));
        vo.setRecords(patlRecordMapper.selectList(
                new LambdaQueryWrapper<PatlRecord>()
                        .eq(PatlRecord::getTaskId, id)
                        .orderByAsc(PatlRecord::getCheckTime)));
        return vo;
    }

    @Override
    @Transactional
    public PatlTask create(String orgId, String routeId, String shift, String planTime) {
        PatlRoute route = patlRouteMapper.selectById(routeId);
        if (route == null) {
            throw new BusinessException(404, "巡检路线不存在");
        }
        List<PatlCheckpoint> checkpoints = patlCheckpointMapper.selectList(
                new LambdaQueryWrapper<PatlCheckpoint>()
                        .eq(PatlCheckpoint::getRouteId, routeId)
                        .orderByAsc(PatlCheckpoint::getSeq));

        PatlTask task = new PatlTask();
        task.setOrgId(orgId);
        task.setTaskNo("PT-" + System.currentTimeMillis());
        task.setRouteId(routeId);
        task.setShift(shift);
        if (planTime != null && !planTime.isEmpty()) {
            task.setPlanTime(LocalDateTime.parse(planTime));
        }
        task.setStatus("待巡检");
        task.setTotalPoints(checkpoints.size());
        task.setDonePoints(0);
        task.setAbnormalCount(0);
        patlTaskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public void submitRecord(String taskId, String checkpointId, String checkpointName, String result, String remark, String operatorId) {
        PatlTask task = patlTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "巡检任务不存在");
        }
        if ("已完成".equals(task.getStatus())) {
            throw new BusinessException(400, "任务已完成,无法继续提交");
        }

        // 创建巡检记录
        PatlRecord record = new PatlRecord();
        record.setOrgId(task.getOrgId());
        record.setTaskId(taskId);
        record.setCheckpointId(checkpointId);
        record.setCheckpointName(checkpointName);
        record.setResult(result);
        record.setRemark(remark);
        record.setOperatorId(operatorId);
        record.setCheckTime(LocalDateTime.now());
        patlRecordMapper.insert(record);

        // 异常则创建 patl_abnormal,任务异常数 +1
        if ("异常".equals(result)) {
            PatlAbnormal abnormal = new PatlAbnormal();
            abnormal.setOrgId(task.getOrgId());
            abnormal.setTaskId(taskId);
            abnormal.setRecordId(record.getId());
            abnormal.setCheckpointName(checkpointName);
            abnormal.setDescription(remark);
            abnormal.setSeverity("一般");
            abnormal.setStatus("待处理");
            patlAbnormalMapper.insert(abnormal);

            task.setAbnormalCount((task.getAbnormalCount() == null ? 0 : task.getAbnormalCount()) + 1);
        }

        // 已检点位数 +1;若全部完成则置已完成
        task.setDonePoints((task.getDonePoints() == null ? 0 : task.getDonePoints()) + 1);
        if (task.getTotalPoints() != null && task.getDonePoints() >= task.getTotalPoints()) {
            task.setStatus("已完成");
            task.setFinishTime(LocalDateTime.now());
        }
        patlTaskMapper.updateById(task);
    }

    @Override
    @Transactional
    public void close(String taskId) {
        PatlTask task = patlTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "巡检任务不存在");
        }
        task.setStatus("已完成");
        task.setFinishTime(LocalDateTime.now());
        patlTaskMapper.updateById(task);
    }

    @Override
    public List<PatlAbnormal> listAbnormals() {
        return patlAbnormalMapper.selectList(null);
    }

    @Override
    @Transactional
    public void closeAbnormal(String id, String handleRemark) {
        PatlAbnormal abnormal = patlAbnormalMapper.selectById(id);
        if (abnormal == null) {
            throw new BusinessException(404, "巡检异常不存在");
        }
        abnormal.setStatus("已关闭");
        abnormal.setHandleRemark(handleRemark);
        abnormal.setHandledBy(currentOperator());
        abnormal.setHandledAt(LocalDateTime.now());
        patlAbnormalMapper.updateById(abnormal);
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null || u.userId() == null ? "系统" : u.userId();
    }
}
