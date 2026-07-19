package com.konli.qms.common.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * LocalDateTime 类型处理器(适配 PostgreSQL TIMESTAMPTZ)。
 *
 * <p>PG JDBC 对 TIMESTAMPTZ 列返回 OffsetDateTime,默认 LocalDateTimeTypeHandler 用
 * getObject(LocalDateTime.class) 会抛 "Cannot convert TIMESTAMPTZ to LocalDateTime"。
 * 本处理器经 OffsetDateTime 中转:读取 toLocalDateTime(),写入 setObject(LocalDateTime)。</p>
 *
 * <p>无 @MappedJdbcTypes,由 {@link TypeHandlerRegistrar} 经
 * {@code register(LocalDateTime.class, this)} 注册为默认处理器(覆盖内置),确保 ResultMapping
 * 无 jdbcType 时也生效。</p>
 */
public class TzLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        OffsetDateTime odt = rs.getObject(columnName, OffsetDateTime.class);
        return odt == null ? null : odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        OffsetDateTime odt = rs.getObject(columnIndex, OffsetDateTime.class);
        return odt == null ? null : odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        OffsetDateTime odt = cs.getObject(columnIndex, OffsetDateTime.class);
        return odt == null ? null : odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }
}
