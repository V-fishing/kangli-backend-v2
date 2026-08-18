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
 * 缓存 VALUE 带全局权限版本号(格式 {@code {version}:{code1,code2,...}}),读前先比对
 * {@code ops.sys_perm_version.version};任一权限表(sys_role_menu / sys_role_button /
 * sys_user_role)变更都会经 DB 触发器自增该版本号,从而让所有用户缓存强制失效——
 * 无论变更来自页面、手工 SQL 还是迁移脚本,都不会出现「改了库却 30min 不刷新」的窗口。
 * 保留 {@link #evictUser}/{@link #evictAll} 作为手动清 Redis 的兜底手段。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionLoader {

    private static final Duration TTL = Duration.ofMinutes(30);
    private static final String VERSION_KEY = "qms:perm_version";
    private static final String USER_KEY_PREFIX = "qms:perms:";

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redis;

    /** 读取全局权限版本号(DB 单行表,触发器维护);读不到返回 -1 触发强制重查。 */
    private long currentVersion() {
        try {
            Long v = jdbcTemplate.queryForObject(
                "SELECT version FROM ops.sys_perm_version WHERE id = 1", Long.class);
            return v == null ? -1L : v;
        } catch (Exception e) {
            log.debug("[PermissionLoader] 读版本号失败,降级直查: {}", e.getMessage());
            return -1L;
        }
    }

    public Set<String> loadPermissionCodes(String userId) {
        String key = USER_KEY_PREFIX + userId;
        long version = currentVersion();
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null && !cached.isEmpty()) {
                int sep = cached.indexOf(':');
                if (sep > 0) {
                    long cachedVer = Long.parseLong(cached.substring(0, sep));
                    // 版本一致 → 直接返回缓存的码(跳过 DB 查询)
                    if (cachedVer == version) {
                        String codesPart = cached.substring(sep + 1);
                        return codesPart.isEmpty() ? Set.of()
                            : new HashSet<>(Arrays.asList(codesPart.split(",")));
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[PermissionLoader] Redis 不可用,降级直查: {}", e.getMessage());
        }
        Set<String> codes = loadFromDb(userId);
        // 写回缓存时带上版本号前缀,下次比对可命中
        try {
            redis.opsForValue().set(key, version + ":" + String.join(",", codes), TTL);
        } catch (Exception ignored) {
            // 缓存写失败不影响鉴权
        }
        return codes;
    }

    /** 失效单个用户权限缓存(用户角色变更时兜底手动清)。 */
    public void evictUser(String userId) {
        try {
            redis.delete(USER_KEY_PREFIX + userId);
        } catch (Exception ignored) {
        }
    }

    /** 失效全部权限缓存(角色菜单/按钮变更时兜底手动清)。 */
    public void evictAll() {
        try {
            Set<String> keys = redis.keys(USER_KEY_PREFIX + "*");
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
