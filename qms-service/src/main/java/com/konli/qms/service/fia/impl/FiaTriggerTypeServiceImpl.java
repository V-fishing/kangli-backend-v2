package com.konli.qms.service.fia.impl;

import com.konli.qms.domain.fia.entity.FiaTriggerType;
import com.konli.qms.domain.fia.mapper.FiaTriggerTypeMapper;
import com.konli.qms.service.fia.FiaTriggerTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FiaTriggerTypeServiceImpl implements FiaTriggerTypeService {

    private final FiaTriggerTypeMapper fiaTriggerTypeMapper;

    @Override
    public List<FiaTriggerType> list() {
        return fiaTriggerTypeMapper.selectList(null);
    }

    @Override
    @Transactional
    public FiaTriggerType create(FiaTriggerType triggerType) {
        if (triggerType.getIsEnabled() == null) {
            triggerType.setIsEnabled(true);
        }
        fiaTriggerTypeMapper.insert(triggerType);
        return triggerType;
    }

    @Override
    public void update(FiaTriggerType triggerType) {
        fiaTriggerTypeMapper.updateById(triggerType);
    }

    @Override
    @Transactional
    public void delete(String id) {
        fiaTriggerTypeMapper.deleteById(id);
    }

    @Override
    public void toggle(String id, boolean enabled) {
        FiaTriggerType t = new FiaTriggerType();
        t.setId(id);
        t.setIsEnabled(enabled);
        fiaTriggerTypeMapper.updateById(t);
    }
}
