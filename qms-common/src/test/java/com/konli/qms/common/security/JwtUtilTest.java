package com.konli.qms.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT 工具单元测试（M15 common 公共组件）。
 * JwtUtil 用 @Value 注入 secret/expiry，此处反射注入后验证 generate→parse 往返。
 */
class JwtUtilTest {

    private JwtUtil jwtUtil = new JwtUtil();

    @BeforeEach
    void setUp() throws Exception {
        setField("secret", "test-secret-key-for-unit-test-only-1234567890");
        setField("expirySeconds", 1800L);
    }

    private void setField(String name, Object value) throws Exception {
        Field f = JwtUtil.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(jwtUtil, value);
    }

    @Test
    @DisplayName("generate 后再 parse 可还原全部 claim")
    void generate_thenParse_roundTrip() {
        String token = jwtUtil.generate("u-1", "admin", "org-1", "all");
        Claims c = jwtUtil.parse(token);
        assertThat(c.getSubject()).isEqualTo("u-1");
        assertThat(c.get("username", String.class)).isEqualTo("admin");
        assertThat(c.get("orgId", String.class)).isEqualTo("org-1");
        assertThat(c.get("dataScope", String.class)).isEqualTo("all");
    }

    @Test
    @DisplayName("不同 secret 生成的 token 无法被本工具解析(签名校验)")
    void tamperedSecret_failsParse() throws Exception {
        String token = jwtUtil.generate("u-1", "admin", "org-1", "all");
        JwtUtil other = new JwtUtil();
        Field f = JwtUtil.class.getDeclaredField("secret");
        f.setAccessible(true);
        f.set(other, "different-secret-key-which-is-also-long-enough-123");
        // 不同密钥解析应抛异常(JWT 签名验证失败)
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class, () -> other.parse(token));
    }
}
