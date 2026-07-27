package com.konli.qms.common.security;

import com.konli.qms.common.exception.BusinessException;

/**
 * 数据归属校验工具(配合 {@link DataScopeInterceptor})。
 *
 * <p>{@code DataScopeInterceptor} 只改写 SELECT,对 UPDATE/DELETE 不生效。
 * 因此写操作(update/delete/resetPassword 等)前需显式校验目标记录归属:
 * 普通用户只能操作本公司(org_id == 当前上下文 orgId)记录,跨公司管理员
 * (dataScope=all)放行,org_id 为 null 的全局数据放行。</p>
 *
 * <p>典型用法:
 * <pre>
 *   T existing = mapper.selectById(id);
 *   if (existing == null) throw new BusinessException(404, "记录不存在");
 *   DataScopeGuard.ensureOwner(existing.getOrgId());
 *   mapper.deleteById(id);
 * </pre>
 * </p>
 */
public final class DataScopeGuard {

    private DataScopeGuard() {}

    /**
     * 校验目标记录归属。普通用户操作别公司记录抛 403。
     *
     * @param recordOrgId 目标记录的 org_id;null 视为全局数据(org_id 为 null),放行
     */
    public static void ensureOwner(String recordOrgId) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null) {
            throw new BusinessException(401, "未认证");
        }
        if (CompanyContext.isAdmin()) {
            return;
        }
        if (recordOrgId == null) {
            return;
        }
        if (!recordOrgId.equals(u.orgId())) {
            throw new BusinessException(403, "无权操作其他公司的数据");
        }
    }
}
