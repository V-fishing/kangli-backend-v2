package com.konli.qms.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.konli.qms.common.security.DataScopeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置(补齐代码规范文档缺失的工程化配置)。
 *
 * <p>数据权限(租户 org_id 过滤,配合 {@link DataScopeInterceptor})
 * + 分页(PostgreSQL)+ 乐观锁(BaseEntity.{@code @Version})。
 * 逻辑删除由 application.yml 全局配置 + BaseEntity.{@code @TableLogic} 生效。</p>
 */
@Configuration
@RequiredArgsConstructor
public class MybatisPlusConfig {

    private final DataScopeInterceptor dataScopeInterceptor;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(dataScopeInterceptor);
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
