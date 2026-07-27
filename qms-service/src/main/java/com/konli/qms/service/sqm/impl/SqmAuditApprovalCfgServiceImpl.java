package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konli.qms.domain.sqm.entity.SqmAuditApprovalCfg;
import com.konli.qms.domain.sqm.mapper.SqmAuditApprovalCfgMapper;
import com.konli.qms.service.sqm.SqmAuditApprovalCfgService;
import com.konli.qms.service.sqm.dto.AuditorDef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqmAuditApprovalCfgServiceImpl implements SqmAuditApprovalCfgService {

    private final SqmAuditApprovalCfgMapper mapper;
    private static final ObjectMapper OM = new ObjectMapper();

    @Override
    public List<SqmAuditApprovalCfg> listAll() {
        return mapper.selectList(new LambdaQueryWrapper<SqmAuditApprovalCfg>()
                .orderByAsc(SqmAuditApprovalCfg::getAuditType));
    }

    @Override
    public SqmAuditApprovalCfg getByType(String auditType) {
        return mapper.selectOne(new LambdaQueryWrapper<SqmAuditApprovalCfg>()
                .eq(SqmAuditApprovalCfg::getAuditType, auditType));
    }

    @Override
    public SqmAuditApprovalCfg save(String auditType, List<AuditorDef> auditors) {
        String json = serialize(auditors);
        SqmAuditApprovalCfg existing = getByType(auditType);
        if (existing == null) {
            existing = new SqmAuditApprovalCfg();
            existing.setAuditType(auditType);
        }
        existing.setAuditors(json);
        if (existing.getId() == null) {
            mapper.insert(existing);
        } else {
            mapper.updateById(existing);
        }
        return existing;
    }

    @Override
    public List<AuditorDef> resolve(String auditType) {
        SqmAuditApprovalCfg cfg = getByType(auditType);
        if (cfg == null || cfg.getAuditors() == null || cfg.getAuditors().isBlank()) {
            return null;
        }
        try {
            List<AuditorDef> list = OM.readValue(cfg.getAuditors(),
                    OM.getTypeFactory().constructCollectionType(List.class, AuditorDef.class));
            return list == null ? Collections.emptyList() : list;
        } catch (Exception e) {
            log.warn("审核会签配置解析失败, auditType={}: {}", auditType, e.getMessage());
            return null;
        }
    }

    private static String serialize(List<AuditorDef> auditors) {
        try {
            return OM.writeValueAsString(auditors == null ? Collections.emptyList() : auditors);
        } catch (Exception e) {
            return "[]";
        }
    }
}
