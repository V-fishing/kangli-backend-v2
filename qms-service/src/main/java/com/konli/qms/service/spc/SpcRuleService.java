package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcRule;

import java.util.List;

/** SPC 判异规则查询与启用/停用。spc.rule.* */
public interface SpcRuleService {

    List<SpcRule> list();

    void toggle(String id, boolean enabled);
}
