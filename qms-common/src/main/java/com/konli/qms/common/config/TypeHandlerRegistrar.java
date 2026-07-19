package com.konli.qms.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 注册 {@link TzLocalDateTimeTypeHandler} 为 LocalDateTime 默认 TypeHandler。
 *
 * <p>直接经 SqlSessionFactory 注册(覆盖内置 LocalDateTimeTypeHandler),
 * 比 type-handlers-package + @MappedJdbcTypes 更确定(ResultMapping 无 jdbcType 时也生效),
 * 解决 PG TIMESTAMPTZ -> LocalDateTime 转换异常。</p>
 */
@Component
@RequiredArgsConstructor
public class TypeHandlerRegistrar {

    private final SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void register() {
        sqlSessionFactory.getConfiguration().getTypeHandlerRegistry()
                .register(LocalDateTime.class, new TzLocalDateTimeTypeHandler());
    }
}
