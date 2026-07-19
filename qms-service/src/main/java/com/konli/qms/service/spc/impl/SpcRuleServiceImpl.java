package com.konli.qms.service.spc.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcRule;
import com.konli.qms.domain.spc.mapper.SpcRuleMapper;
import com.konli.qms.service.spc.SpcRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpcRuleServiceImpl implements SpcRuleService {

    private final SpcRuleMapper spcRuleMapper;

    @Override
    public List<SpcRule> list() {
        return spcRuleMapper.selectList(null);
    }

    @Override
    public void toggle(String id, boolean enabled) {
        SpcRule rule = spcRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(400, "判异规则不存在");
        }
        rule.setIsEnabled(enabled);
        spcRuleMapper.updateById(rule);
    }
}
