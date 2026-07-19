package com.konli.qms.common.security;

/**
 * @deprecated 改用 {@link DataScopeInterceptor}(MP 3.5.9 的 DataPermissionHandler 是旧 API
 *     getSqlSegment(Expression, String),拿不到表名无法判断是否含 org_id,故改自定义 InnerInterceptor)。
 *     保留空类以清理文件。
 */
@Deprecated
public class OrgIdDataPermissionHandler {
    private OrgIdDataPermissionHandler() {
    }
}
