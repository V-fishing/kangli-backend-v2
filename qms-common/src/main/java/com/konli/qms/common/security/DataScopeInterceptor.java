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
 * 数据权限拦截器(简化多分公司 dataScope,应用级 org_id 过滤)。
 *
 * <ul>
 *   <li>无上下文(登录前)或管理员(dataScope=all):不改写</li>
 *   <li>普通用户:对含 org_id 列的租户表 SELECT 追加 {@code AND org_id = '<dataScope>'}</li>
 * </ul>
 *
 * <p>代码规范§7.3 用 PG RLS(set_app_session),但 SET LOCAL 是连接级、与 MyBatis 连接池不同连接,
 * RLS 不生效;故用应用级 SQL 改写(本拦截器)更可靠。org_id 来自签名 JWT(可信),以字面量拼接。
 * 仅处理单表 PlainSelect;多表 JOIN/子查询等复杂 SQL 跳过(不阻断)。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataScopeInterceptor implements InnerInterceptor {

    private final JdbcTemplate jdbcTemplate;
    private volatile Set<String> orgIdTables;

    /**
     * 全局配置表(org_id 为 NULL 表示全公司共享,不按分公司过滤)。
     * 如 SPC 判异规则(spc_rule)、系统枚举字典(sys_dict) 等参考/主数据。
     */
    private static final Set<String> GLOBAL_TABLES = Set.of("spc_rule", "sys_dict");

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
            boolean touchesTenant = tables.stream()
                    .map(t -> t.toLowerCase().replaceAll("^ops\\.", ""))
                    .anyMatch(t -> orgIdTables.contains(t) && !GLOBAL_TABLES.contains(t));
            if (!touchesTenant) {
                return;
            }
            EqualsTo cond = new EqualsTo(new Column("org_id"), new StringValue(u.orgId()));
            Expression where = ps.getWhere();
            ps.setWhere(where == null ? cond : new AndExpression(where, cond));
            PluginUtils.mpBoundSql(boundSql).sql(ps.toString());
        } catch (Exception e) {
            log.warn("[DataScope] SQL 改写跳过: {}", e.getMessage());
        }
    }

    private synchronized void ensureInited() {
        if (orgIdTables == null) {
            orgIdTables = jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.columns WHERE table_schema='ops' AND column_name='org_id'",
                    String.class).stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        }
    }
}
