package com.konli.qms.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限码加载器(Redis 缓存,代码规范§3.4 @PreAuthorize 所需)。
 *
 * <p>按 userId 查菜单 menu_code + 按钮 btn_code,Redis 缓存 30 分钟;Redis 不可用时降级直查 DB。
 * 角色权限/用户角色变更时由 Service 调 {@link #evictUser}/{@link #evictAll} 失效。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionLoader {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redis;

    public Set<String> loadPermissionCodes(String userId) {
        String key = "qms:perms:" + userId;
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                return cached.isEmpty() ? Set.of() : new HashSet<>(Arrays.asList(cached.split(",")));
            }
        } catch (Exception e) {
            log.debug("[PermissionLoader] Redis 不可用,降级直查: {}", e.getMessage());
        }
        Set<String> codes = loadFromDb(userId);
        try {
            redis.opsForValue().set(key, String.join(",", codes), TTL);
        } catch (Exception ignored) {
            // 缓存写失败不影响鉴权
        }
        return codes;
    }

    /** 失效单个用户权限缓存(用户角色变更时) */
    public void evictUser(String userId) {
        try {
            redis.delete("qms:perms:" + userId);
        } catch (Exception ignored) {
        }
    }

    /** 失效全部权限缓存(角色菜单/按钮变更时) */
    public void evictAll() {
        try {
            Set<String> keys = redis.keys("qms:perms:*");
            if (keys != null && !keys.isEmpty()) {
                redis.delete(keys);
            }
        } catch (Exception ignored) {
        }
    }

    private Set<String> loadFromDb(String userId) {
        Set<String> codes = new HashSet<>();
        codes.addAll(jdbcTemplate.queryForList(
                "SELECT m.menu_code FROM ops.sys_role_menu rm "
                        + "JOIN ops.sys_user_role ur ON ur.role_id = rm.role_id "
                        + "JOIN ops.sys_menu m ON m.id = rm.menu_id "
                        + "WHERE ur.user_id = ?::uuid", String.class, userId));
        codes.addAll(jdbcTemplate.queryForList(
                "SELECT b.btn_code FROM ops.sys_role_button rb "
                        + "JOIN ops.sys_user_role ur ON ur.role_id = rb.role_id "
                        + "JOIN ops.sys_button b ON b.id = rb.button_id "
                        + "WHERE ur.user_id = ?::uuid", String.class, userId));
        return codes;
    }
}
