package com.konli.qms.common.security;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 数据权限拦截器回归测试（M15 common 公共组件）。
 * 聚焦历史修复点：
 *  1) 括号 bug：org_id = ? OR org_id IS NULL 必须整体带括号，否则 AND 优先改写原语义
 *  2) JOIN 歧义：多表 JOIN 时主表含 org_id 须用主表别名（p.org_id）避免列引用歧义
 *  3) 组织切换：switchOrg 优先注入
 *  4) 管理员/全局表不改写
 *
 * 注：beforeQuery 需真实 BoundSql 对象（PluginUtils.mpBoundSql 写回 SQL），其 mybatis 形参用 mock。
 */
@ExtendWith(MockitoExtension.class)
class DataScopeInterceptorTest {

    @Mock JdbcTemplate jdbcTemplate;
    @Mock Executor executor;
    @Mock MappedStatement ms;
    @Mock ResultHandler<?> rh;

    DataScopeInterceptor interceptor;

    /** 测试用组织 UUID（合法格式） */
    static final String ORG = "019fd5dc-5197-75d2-bc6c-6c7be0def01a";
    static final String OTHER_ORG = "019f701f-041c-71d1-895e-82b66b183bce";

    @BeforeEach
    void setUp() {
        interceptor = new DataScopeInterceptor(jdbcTemplate);
        // ensureInited 会查 information_schema 两列，mock 返回含测试表名的集合
        lenient().when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("qms_8d_report", "ncm_defect_record"));
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
        DataScopeInterceptor.clearScope();
    }

    /** 用真实 BoundSql 跑拦截器，返回改写后的 SQL */
    private String rewrite(String sql) throws SQLException {
        // configuration 不能为 null，否则 PluginUtils.mpBoundSql 反射写回 SQL 时 NPE
        BoundSql boundSql = new BoundSql(new org.apache.ibatis.session.Configuration(), sql, null, null);
        interceptor.beforeQuery(executor, ms, null, RowBounds.DEFAULT, rh, boundSql);
        // PluginUtils.mpBoundSql(boundSql).sql(...) 写回原 boundSql
        return PluginUtils.mpBoundSql(boundSql).sql();
    }

    private void loginAsOrgUser() {
        CompanyContext.set(new CompanyContext.CurrentUser("u-1", "user", ORG, "org"));
    }

    @Test
    @DisplayName("单表查询(普通用户 org 范围):原 WHERE 用 AND 连接且 org 条件整体带括号")
    void singleTable_orgScope_wrapsOrgCondInParens() throws Exception {
        loginAsOrgUser();
        String out = rewrite("SELECT * FROM qms_8d_report WHERE status = 'DONE'");
        assertThat(out).contains("WHERE status = 'DONE'");
        assertThat(out).contains("AND (org_id = '" + ORG + "' OR org_id IS NULL)");
        // 括号 bug 回归：OR 不被提升为顶层（不能出现 "status = 'DONE' OR org_id IS NULL"）
        assertThat(out).doesNotContain("status = 'DONE' OR org_id IS NULL");
    }

    @Test
    @DisplayName("JOIN 双表(主表别名 p 含 org_id):用主表别名避免列歧义")
    void joinTwoTables_usesAliasForOrgColumn() throws Exception {
        loginAsOrgUser();
        String out = rewrite(
                "SELECT p.id FROM qms_8d_report p JOIN ncm_defect_record d ON p.id = d.d8_id " +
                "WHERE p.status = 'DONE'");
        assertThat(out).contains("p.org_id = '" + ORG + "'");
        assertThat(out).doesNotContain("AND (org_id = '" + ORG + "'");
    }

    @Test
    @DisplayName("管理员(dataScope=all):不改写 SQL")
    void admin_noRewrite() throws Exception {
        CompanyContext.set(new CompanyContext.CurrentUser("u-1", "admin", ORG, "all"));
        String sql = "SELECT * FROM qms_8d_report WHERE status = 'DONE'";
        String out = rewrite(sql);
        assertThat(out).isEqualTo(sql);
    }

    @Test
    @DisplayName("组织切换(switchOrg)优先注入切换组织条件")
    void switchOrg_overridesUserOrg() throws Exception {
        loginAsOrgUser();
        CompanyContext.setSwitchOrgId(OTHER_ORG);
        String out = rewrite("SELECT * FROM qms_8d_report");
        assertThat(out).contains("(" + "org_id = '" + OTHER_ORG + "' OR org_id IS NULL)");
        assertThat(out).doesNotContain("org_id = '" + ORG + "'");
    }

    @Test
    @DisplayName("全局表(sys_dict)不追加 org 过滤")
    void globalTable_noRewrite() throws Exception {
        loginAsOrgUser();
        String sql = "SELECT * FROM sys_dict WHERE type = 'SEVERITY'";
        String out = rewrite(sql);
        assertThat(out).isEqualTo(sql);
    }
}
