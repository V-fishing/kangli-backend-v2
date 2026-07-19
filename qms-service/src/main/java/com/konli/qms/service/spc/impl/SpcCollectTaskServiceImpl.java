package com.konli.qms.service.spc.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcCollectTask;
import com.konli.qms.domain.spc.mapper.SpcCollectTaskMapper;
import com.konli.qms.service.spc.SpcCollectTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpcCollectTaskServiceImpl implements SpcCollectTaskService {

    private final SpcCollectTaskMapper spcCollectTaskMapper;

    @Override
    public List<SpcCollectTask> list() {
        return spcCollectTaskMapper.selectList(null);
    }

    @Override
    @Transactional
    public SpcCollectTask create(SpcCollectTask task) {
        if (task.getIsPlannedDowntime() == null) {
            task.setIsPlannedDowntime(false);
        }
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("待采集");
        }
        spcCollectTaskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public void markDowntime(String id, Boolean isPlannedDowntime, String reason) {
        SpcCollectTask task = spcCollectTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(400, "采集任务不存在");
        }
        task.setIsPlannedDowntime(isPlannedDowntime);
        // reason 当前表无独立字段,接口保留以便后续扩展(代码规范§3.2 备注列)。
        spcCollectTaskMapper.updateById(task);
    }
}
