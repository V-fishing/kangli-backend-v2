package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcRule;
import com.konli.qms.domain.spc.mapper.SpcRuleMapper;
import com.konli.qms.domain.spc.mapper.SpcSubgroupMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SPC 判异规则 Service 单元测试（M4 spc，Mockito）。
 * 覆盖：列表/分页/启停切换，含"规则不存在抛 400"分支。
 */
@ExtendWith(MockitoExtension.class)
class SpcRuleServiceImplTest {

    @Mock SpcRuleMapper spcRuleMapper;
    @Mock SpcSubgroupMapper spcSubgroupMapper;
    @InjectMocks SpcRuleServiceImpl service;

    SpcRule rule;

    @BeforeEach
    void setUp() {
        rule = new SpcRule();
        rule.setId("r-1");
        rule.setRuleCode("RULE_1");
        rule.setRuleName("1σ 准则");
        rule.setLevel("A");
        rule.setIsEnabled(true);
        lenient().when(spcSubgroupMapper.selectCount(any())).thenReturn(0L);
    }

    @Test
    @DisplayName("list 转发 mapper.selectList")
    void list_forwards() {
        when(spcRuleMapper.selectList(null)).thenReturn(List.of(rule));
        assertThat(service.list()).hasSize(1).contains(rule);
    }

    @Test
    @DisplayName("listPage 关键字过滤并返回 PageResult")
    void listPage_withKeyword() {
        IPage<SpcRule> page = org.mockito.Mockito.mock(IPage.class);
        lenient().when(page.getRecords()).thenReturn(List.of(rule));
        lenient().when(page.getTotal()).thenReturn(1L);
        lenient().when(page.getCurrent()).thenReturn(1L);
        lenient().when(page.getSize()).thenReturn(10L);
        when(spcRuleMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<SpcRule> r = service.listPage("RULE", 1, 10);
        assertThat(r.getTotal()).isEqualTo(1);
        assertThat(r.getRecords()).contains(rule);
    }

    @Test
    @DisplayName("toggle 规则不存在抛 BusinessException(400)")
    void toggle_notFound_throws400() {
        when(spcRuleMapper.selectById("missing")).thenReturn(null);
        assertThatThrownBy(() -> service.toggle("missing", true))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 400);
    }

    @Test
    @DisplayName("toggle 正常:更新 isEnabled 并写回")
    void toggle_found_updatesEnabled() {
        when(spcRuleMapper.selectById("r-1")).thenReturn(rule);
        service.toggle("r-1", false);
        ArgumentCaptor<SpcRule> cap = ArgumentCaptor.forClass(SpcRule.class);
        verify(spcRuleMapper).updateById(cap.capture());
        assertThat(cap.getValue().getIsEnabled()).isFalse();
    }
}
