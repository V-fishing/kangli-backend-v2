package com.konli.qms.service.sqm.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmAuditFreqRule;
import com.konli.qms.domain.sqm.mapper.SqmAuditFreqRuleMapper;
import com.konli.qms.service.sqm.SqmAuditFreqRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmAuditFreqRuleServiceImpl implements SqmAuditFreqRuleService {

    private final SqmAuditFreqRuleMapper sqmAuditFreqRuleMapper;

    @Override
    public List<SqmAuditFreqRule> list() {
        return sqmAuditFreqRuleMapper.selectList(null);
    }

    @Override
    @Transactional
    public SqmAuditFreqRule create(SqmAuditFreqRule rule) {
        sqmAuditFreqRuleMapper.insert(rule);
        return rule;
    }

    @Override
    @Transactional
    public void update(SqmAuditFreqRule rule) {
        if (rule.getId() == null || sqmAuditFreqRuleMapper.selectById(rule.getId()) == null) {
            throw new BusinessException(404, "审核频次规则不存在");
        }
        sqmAuditFreqRuleMapper.updateById(rule);
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (sqmAuditFreqRuleMapper.selectById(id) == null) {
            throw new BusinessException(404, "审核频次规则不存在");
        }
        sqmAuditFreqRuleMapper.deleteById(id);
    }
}
