package com.konli.qms.common.security;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器(多分公司 dataScope + sys_data_scope 三级 RBAC)。
 *
 * <ul>
 *   <li>无上下文(登录前)或管理员(dataScope=all):不改写</li>
 *   <li>非管理员:查询 sys_data_scope 表取用户角色中最宽松的 scope_type，按表类型追加条件</li>
 *   <li>ALL → 不追加(全量); ORG_AND_SUB → AND org_id = &lt;orgId&gt;; SELF → AND created_by = &lt;userId&gt;</li>
 * </ul>
 *
 * <p>仅处理单表 PlainSelect;多表 JOIN/子查询等复杂 SQL 跳过(不阻断)。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataScopeInterceptor implements InnerInterceptor {

    private final JdbcTemplate jdbcTemplate;
    private volatile Set<String> orgIdTables;
    private volatile Set<String> createdByTables;

    /**
     * 全局配置表(不按分公司/个人过滤)。
     */
    private static final Set<String> GLOBAL_TABLES = Set.of("spc_rule", "sys_dict");

    /**
     * 用户有效数据范围缓存(key=userId, 存到 ThreadLocal 避免重复查库)。
     * "all"=全量 "org"=按分公司 "self"=按创建人
     */
    private static final ThreadLocal<String> EFFECTIVE_SCOPE = new ThreadLocal<>();

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null || CompanyContext.isAdmin()) {
            return;
        }
        ensureInited();
        String sql = boundSql.getSql();
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            if (!(stmt instanceof Select)) {
                return;
            }
            Select select = (Select) stmt;
            PlainSelect ps = select.getPlainSelect();
            TablesNamesFinder finder = new TablesNamesFinder();
            List<String> tables = finder.getTableList(stmt);

            // 检查涉及的表是否同时有 org_id 和 created_by 列
            boolean touchesOrg = tables.stream()
                    .map(t -> t.toLowerCase().replaceAll("^ops\\.", ""))
                    .anyMatch(t -> orgIdTables.contains(t) && !GLOBAL_TABLES.contains(t));
            boolean touchesCreatedBy = tables.stream()
                    .map(t -> t.toLowerCase().replaceAll("^ops\\.", ""))
                    .anyMatch(t -> createdByTables.contains(t) && !GLOBAL_TABLES.contains(t));

            if (!touchesOrg && !touchesCreatedBy) {
                return;
            }

            String scope = getEffectiveScope(u);
            Expression where = ps.getWhere();

            if ("all".equals(scope)) {
                return; // ALL 数据范围，不过滤
            }

            if ("self".equals(scope) && touchesCreatedBy) {
                // SELF: 仅本人数据
                EqualsTo cond = new EqualsTo(new Column("created_by"), new StringValue(u.userId()));
                ps.setWhere(where == null ? cond : new AndExpression(where, cond));
            } else if (touchesOrg) {
                // ORG_AND_SUB 或未配置 scope: 按分公司过滤(维持现有行为)
                EqualsTo cond = new EqualsTo(new Column("org_id"), new StringValue(u.orgId()));
                ps.setWhere(where == null ? cond : new AndExpression(where, cond));
            } else {
                return;
            }

            PluginUtils.mpBoundSql(boundSql).sql(ps.toString());
        } catch (Exception e) {
            log.warn("[DataScope] SQL 改写跳过: {}", e.getMessage());
        }
    }

    /**
     * 获取用户的有效数据范围(从 sys_data_scope 表查询所有角色的配置，取最宽松值)。
     */
    private String getEffectiveScope(CompanyContext.CurrentUser u) {
        String cached = EFFECTIVE_SCOPE.get();
        if (cached != null) {
            return cached;
        }
        try {
            // 查询用户所有角色的数据范围，取最宽松值: ALL > org > self
            List<String> scopes = jdbcTemplate.queryForList(
                "SELECT DISTINCT ds.scope_type FROM ops.sys_data_scope ds " +
                "JOIN ops.sys_user_role ur ON ds.role_id = ur.role_id " +
                "WHERE ur.user_id = ?::uuid",
                String.class, u.userId());

            String effective;
            if (scopes.isEmpty()) {
                // 未配置 → 默认按 org 过滤(向后兼容)
                effective = "org";
            } else if (scopes.contains("ALL")) {
                effective = "all";
            } else if (scopes.contains("ORG_AND_SUB")) {
                effective = "org";
            } else {
                effective = "self";
            }
            EFFECTIVE_SCOPE.set(effective);
            return effective;
        } catch (Exception e) {
            log.warn("[DataScope] 查询数据范围失败，默认按 org 过滤: {}", e.getMessage());
            EFFECTIVE_SCOPE.set("org");
            return "org";
        }
    }

    /**
     * 请求结束后清理 ThreadLocal 缓存。
     */
    public static void clearScope() {
        EFFECTIVE_SCOPE.remove();
    }

    private synchronized void ensureInited() {
        if (orgIdTables == null) {
            orgIdTables = jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.columns WHERE table_schema='ops' AND column_name='org_id'",
                    String.class).stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        }
        if (createdByTables == null) {
            createdByTables = jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.columns WHERE table_schema='ops' AND column_name='created_by'",
                    String.class).stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        }
    }
}
