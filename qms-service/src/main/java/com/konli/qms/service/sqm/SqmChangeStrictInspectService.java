package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmChangeStrictInspect;

import java.util.List;

/** 加严检验:查询/新增/恢复。sqm.change.* */
public interface SqmChangeStrictInspectService {

    List<SqmChangeStrictInspect> list(String changeId);

    SqmChangeStrictInspect get(String id);

    SqmChangeStrictInspect create(SqmChangeStrictInspect inspect);

    /** 恢复正常检验(restored=true)。 */
    void restore(String id);
}
