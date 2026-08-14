package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmAbnormalRule;

import java.util.List;

/** 来料异常严重度判定规则(复用来料模块 sqm_abnormal_rule 表)。sqm.abnormal-rule.* */
public interface SqmAbnormalRuleService {

    /** 列表(admin 看全部, 非 admin 按 org 过滤)。 */
    List<SqmAbnormalRule> list();

    SqmAbnormalRule save(SqmAbnormalRule rule);

    void delete(String id);
}
