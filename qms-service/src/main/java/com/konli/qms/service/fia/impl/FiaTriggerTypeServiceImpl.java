package com.konli.qms.service.fia.impl;

import com.konli.qms.common.security.CompanyContext;
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
        // org_id 由当前登录用户上下文决定(真实 UUID),避免前端传业务代码导致 UUID 写入失败,
        // 同时保证与列表数据隔离过滤(DataScopeInterceptor)使用同一 org_id,新增后能查到。
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u != null && u.orgId() != null && !u.orgId().isBlank() && !"ROOT".equals(u.orgId())) {
            triggerType.setOrgId(u.orgId());
        } else {
            // 管理员(dataScope=all)或无具体组织时置空=全局,管理员列表不过滤仍可见
            triggerType.setOrgId(null);
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
