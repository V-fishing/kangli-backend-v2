package com.konli.qms.service.ncm.impl;

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
        ncmAlertEscalationMapper.insert(escalation);
        return escalation;
    }

    @Override
    public void update(NcmAlertEscalation escalation) {
        ncmAlertEscalationMapper.updateById(escalation);
    }

    @Override
    @Transactional
    public void delete(String id) {
        ncmAlertEscalationMapper.deleteById(id);
    }
}
