package com.konli.qms.common.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * String[] <-> PostgreSQL VARCHAR[] 类型处理器。
 * 用于 fia_sign_config.sign_methods 等 PG 数组列。
 */
public class StringArrayTypeHandler extends BaseTypeHandler<String[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException {
        ps.setArray(i, ps.getConnection().createArrayOf("varchar", parameter));
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Array arr = rs.getArray(columnName);
        return arr == null ? null : (String[]) arr.getArray();
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Array arr = rs.getArray(columnIndex);
        return arr == null ? null : (String[]) arr.getArray();
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Array arr = cs.getArray(columnIndex);
        return arr == null ? null : (String[]) arr.getArray();
    }
}
