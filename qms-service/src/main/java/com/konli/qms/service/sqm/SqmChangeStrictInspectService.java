package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmChangeStrictInspect;

import java.util.List;

/** 加严检验:查询/新增/恢复。sqm.change.* */
public interface SqmChangeStrictInspectService {

    List<SqmChangeStrictInspect> list(String changeId);

    SqmChangeStrictInspect get(String id);

    SqmChangeStrictInspect create(SqmChangeStrictInspect inspect);

    /**
     * 在独立(REQUIRES_NEW)事务中创建加严检验记录。
     * 供变更审批联动调用:即使 insert 因约束等异常失败,也只回滚本事务,
     * 不会把调用方(approve)的主事务标记为 rollback-only。
     */
    SqmChangeStrictInspect createInNewTx(SqmChangeStrictInspect inspect);

    /** 恢复正常检验(restored=true)。 */
    void restore(String id);
}
