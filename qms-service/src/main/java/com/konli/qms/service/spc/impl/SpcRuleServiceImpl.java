package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcRule;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcRuleMapper;
import com.konli.qms.domain.spc.mapper.SpcSubgroupMapper;
import com.konli.qms.service.spc.SpcRuleService;
import com.konli.qms.service.spc.dto.SpcRuleTriggerVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpcRuleServiceImpl implements SpcRuleService {

    private final SpcRuleMapper spcRuleMapper;
    private final SpcSubgroupMapper spcSubgroupMapper;

    @Override
    public List<SpcRule> list() {
        return spcRuleMapper.selectList(null);
    }

    @Override
    public PageResult<SpcRule> listPage(String keyword, int page, int size) {
        LambdaQueryWrapper<SpcRule> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(k -> k.like(SpcRule::getRuleCode, keyword)
                    .or().like(SpcRule::getRuleName, keyword));
        }
        w.orderByAsc(SpcRule::getRuleCode);
        IPage<SpcRule> ip = spcRuleMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
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

    @Override
    public List<SpcRuleTriggerVo> ruleTriggers() {
        List<SpcRule> rules = spcRuleMapper.selectList(null);
        List<SpcRuleTriggerVo> result = new ArrayList<>();
        for (SpcRule rule : rules) {
            String code = rule.getRuleCode();
            Long cnt = spcSubgroupMapper.selectCount(
                    new LambdaQueryWrapper<SpcSubgroup>().eq(SpcSubgroup::getOutlierRule, code));
            SpcRuleTriggerVo vo = new SpcRuleTriggerVo();
            vo.setCode(code);
            vo.setName(rule.getRuleName());
            vo.setLevel(rule.getLevel());
            vo.setCnt(cnt == null ? 0 : cnt);
            result.add(vo);
        }
        return result;
    }
}
