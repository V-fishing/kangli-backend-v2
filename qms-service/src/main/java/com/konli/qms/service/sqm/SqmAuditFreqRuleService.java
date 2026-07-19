package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmAuditFreqRule;

import java.util.List;

/** 审核频次规则:查询/新增/编辑/删除。sqm.audit.* */
public interface SqmAuditFreqRuleService {

    List<SqmAuditFreqRule> list();

    SqmAuditFreqRule create(SqmAuditFreqRule rule);

    void update(SqmAuditFreqRule rule);

    void delete(String id);
}
