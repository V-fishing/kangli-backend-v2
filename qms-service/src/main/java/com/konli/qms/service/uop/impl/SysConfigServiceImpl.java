package com.konli.qms.service.uop.impl;

import com.konli.qms.service.uop.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统开关/配置项服务实现。
 *
 * <p>采用 JdbcTemplate 直查 key-value(避免 MyBatis-Plus 软删除/乐观锁对配置表语义的干扰);
 * 并做 60s 短 TTL 内存缓存,降低高频读取(如 OrgIdResolver 每次请求解析)对配置表的访问压力。</p>
 */
@Slf4j
@Service
public class SysConfigServiceImpl implements SysConfigService {

    private final JdbcTemplate jdbcTemplate;

    private static final long TTL_MS = 60_000;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public SysConfigServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getValue(String key) {
        return getValue(key, null);
    }

    @Override
    public String getValue(String key, String defaultVal) {
        CacheEntry e = cache.get(key);
        if (e != null && System.currentTimeMillis() - e.ts < TTL_MS) {
            return e.value != null ? e.value : defaultVal;
        }
        String v;
        try {
            v = jdbcTemplate.queryForObject(
                    "SELECT config_value FROM ops.sys_config WHERE config_key = ?", String.class, key);
        } catch (Exception ex) {
            v = null;
        }
        cache.put(key, new CacheEntry(v, System.currentTimeMillis()));
        return v == null ? defaultVal : v;
    }

    @Override
    public boolean getBool(String key, boolean defaultVal) {
        String v = getValue(key, null);
        if (v == null) {
            return defaultVal;
        }
        return "1".equals(v) || "true".equalsIgnoreCase(v)
                || "Y".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v);
    }

    @Override
    public void set(String key, String value) {
        set(key, value, null);
    }

    @Override
    public void set(String key, String value, String remark) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ops.sys_config WHERE config_key = ?", Integer.class, key);
        if (cnt != null && cnt > 0) {
            jdbcTemplate.update(
                    "UPDATE ops.sys_config SET config_value = ?, remark = COALESCE(?, remark), updated_at = now() "
                            + "WHERE config_key = ?",
                    value, remark, key);
        } else {
            jdbcTemplate.update(
                    "INSERT INTO ops.sys_config (id, config_key, config_value, remark, created_at, updated_at) "
                            + "VALUES (ops.gen_uuid_v7(), ?, ?, ?, now(), now())",
                    key, value, remark);
        }
        cache.remove(key);
    }

    private static final class CacheEntry {
        final String value;
        final long ts;

        CacheEntry(String value, long ts) {
            this.value = value;
            this.ts = ts;
        }
    }
}
