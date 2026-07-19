package com.konli.qms.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具(代码规范:自建账号 + JWT + Redis 会话)。
 *
 * <p>Claims:sub=userId, username, orgId, dataScope。HS256,密钥与有效期由 application.yml 注入。</p>
 */
@Component
public class JwtUtil {

    @Value("${qms.jwt.secret}")
    private String secret;

    @Value("${qms.jwt.expiry:1800}")
    private long expirySeconds;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(String userId, String username, String orgId, String dataScope) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("orgId", orgId)
                .claim("dataScope", dataScope)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirySeconds * 1000L))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
