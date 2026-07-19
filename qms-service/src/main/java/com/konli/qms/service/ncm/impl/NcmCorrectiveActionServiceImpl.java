package com.konli.qms.service.ncm.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.ncm.entity.NcmCorrectiveAction;
import com.konli.qms.domain.ncm.mapper.NcmCorrectiveActionMapper;
import com.konli.qms.service.ncm.NcmCorrectiveActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NcmCorrectiveActionServiceImpl implements NcmCorrectiveActionService {

    private final NcmCorrectiveActionMapper ncmCorrectiveActionMapper;

    @Override
    public List<NcmCorrectiveAction> list() {
        return ncmCorrectiveActionMapper.selectList(null);
    }

    @Override
    public NcmCorrectiveAction get(String id) {
        return ncmCorrectiveActionMapper.selectById(id);
    }

    @Override
    @Transactional
    public NcmCorrectiveAction create(NcmCorrectiveAction action) {
        action.setCaNo("CA-" + System.currentTimeMillis());
        action.setStatus("待启动");
        if (action.getProgress() == null) {
            action.setProgress((short) 0);
        }
        ncmCorrectiveActionMapper.insert(action);
        return action;
    }

    @Override
    @Transactional
    public void updateProgress(String id, Short progress) {
        NcmCorrectiveAction action = ncmCorrectiveActionMapper.selectById(id);
        if (action == null) {
            throw new BusinessException(404, "纠正措施不存在");
        }
        NcmCorrectiveAction upd = new NcmCorrectiveAction();
        upd.setId(id);
        upd.setProgress(progress);
        // progress=100 -> status="已完成"
        if (progress != null && progress == 100) {
            upd.setStatus("已完成");
        }
        ncmCorrectiveActionMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void close(String id) {
        NcmCorrectiveAction action = ncmCorrectiveActionMapper.selectById(id);
        if (action == null) {
            throw new BusinessException(404, "纠正措施不存在");
        }
        NcmCorrectiveAction upd = new NcmCorrectiveAction();
        upd.setId(id);
        upd.setStatus("已关闭");
        ncmCorrectiveActionMapper.updateById(upd);
    }
}
