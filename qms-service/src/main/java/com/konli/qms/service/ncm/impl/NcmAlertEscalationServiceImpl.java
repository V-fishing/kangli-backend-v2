package com.konli.qms.service.ncm.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.DataScopeGuard;
import com.konli.qms.domain.ncm.entity.NcmAlertEscalation;
import com.konli.qms.domain.ncm.mapper.NcmAlertEscalationMapper;
import com.konli.qms.service.ncm.NcmAlertEscalationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NcmAlertEscalationServiceImpl implements NcmAlertEscalationService {

    private final NcmAlertEscalationMapper ncmAlertEscalationMapper;

    @Override
    public List<NcmAlertEscalation> list() {
        return ncmAlertEscalationMapper.selectList(null);
    }

    @Override
    @Transactional
    public NcmAlertEscalation create(NcmAlertEscalation escalation) {
        // 普通用户只能创建本公司配置(防跨公司伪造 org_id);全局配置(org_id=null)仅管理员可建
        CompanyContext.CurrentUser cur = CompanyContext.get();
        if (cur != null && !CompanyContext.isAdmin()) {
            escalation.setOrgId(cur.orgId());
        }
        ncmAlertEscalationMapper.insert(escalation);
        return escalation;
    }

    @Override
    public void update(NcmAlertEscalation escalation) {
        NcmAlertEscalation existing = ncmAlertEscalationMapper.selectById(escalation.getId());
        if (existing == null) {
            throw new BusinessException(404, "告警升级配置不存在");
        }
        DataScopeGuard.ensureOwner(existing.getOrgId());
        // 禁止通过 update 改动归属
        escalation.setOrgId(existing.getOrgId());
        ncmAlertEscalationMapper.updateById(escalation);
    }

    @Override
    @Transactional
    public void delete(String id) {
        NcmAlertEscalation existing = ncmAlertEscalationMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "告警升级配置不存在");
        }
        DataScopeGuard.ensureOwner(existing.getOrgId());
        ncmAlertEscalationMapper.deleteById(id);
    }
}
