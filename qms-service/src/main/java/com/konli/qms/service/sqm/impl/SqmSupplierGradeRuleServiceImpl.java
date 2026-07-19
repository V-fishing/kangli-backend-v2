package com.konli.qms.service.sqm.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmSupplierGradeRule;
import com.konli.qms.domain.sqm.mapper.SqmSupplierGradeRuleMapper;
import com.konli.qms.service.sqm.SqmSupplierGradeRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmSupplierGradeRuleServiceImpl implements SqmSupplierGradeRuleService {

    private final SqmSupplierGradeRuleMapper sqmSupplierGradeRuleMapper;

    @Override
    public List<SqmSupplierGradeRule> list() {
        return sqmSupplierGradeRuleMapper.selectList(null);
    }

    @Override
    @Transactional
    public SqmSupplierGradeRule create(SqmSupplierGradeRule rule) {
        sqmSupplierGradeRuleMapper.insert(rule);
        return rule;
    }

    @Override
    @Transactional
    public void update(SqmSupplierGradeRule rule) {
        if (rule.getId() == null || sqmSupplierGradeRuleMapper.selectById(rule.getId()) == null) {
            throw new BusinessException(404, "供应商评级规则不存在");
        }
        sqmSupplierGradeRuleMapper.updateById(rule);
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (sqmSupplierGradeRuleMapper.selectById(id) == null) {
            throw new BusinessException(404, "供应商评级规则不存在");
        }
        sqmSupplierGradeRuleMapper.deleteById(id);
    }
}
