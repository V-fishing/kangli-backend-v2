package com.konli.qms.service.spc;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.spc.entity.SpcRule;
import com.konli.qms.service.spc.dto.SpcRuleTriggerVo;

import java.util.List;

/** SPC 判异规则查询与启用/停用。spc.rule.* */
public interface SpcRuleService {

    List<SpcRule> list();

    PageResult<SpcRule> listPage(String keyword, int page, int size);

    void toggle(String id, boolean enabled);

    /** 看板"判异规则触发次数":每条规则命中异常子组的数量。 */
    List<SpcRuleTriggerVo> ruleTriggers();
}
