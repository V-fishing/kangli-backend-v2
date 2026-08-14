package com.konli.qms.service.notify.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.entity.NotifyConfig;
import com.konli.qms.domain.notify.mapper.NotifyChannelMapper;
import com.konli.qms.domain.notify.mapper.NotifyConfigMapper;
import com.konli.qms.service.notify.NotifyConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一通知配置实现: 本地缓存避免每次通知查库, update 时主动失效对应条目。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyConfigServiceImpl implements NotifyConfigService {

    private final NotifyConfigMapper notifyConfigMapper;
    private final NotifyChannelMapper notifyChannelMapper;

    private static final ObjectMapper om = new ObjectMapper();

    /** 缓存 key = module|eventCode */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private static final class CacheEntry {
        final List<String> roles;
        final List<String> receiverIds;
        final List<String> channels;
        final boolean enabled;
        CacheEntry(List<String> roles, List<String> receiverIds, List<String> channels, boolean enabled) {
            this.roles = roles; this.receiverIds = receiverIds; this.channels = channels; this.enabled = enabled;
        }
    }

    private static String key(String module, String eventCode) {
        return module + "|" + eventCode;
    }

    private static List<String> split(String s) {
        if (!StringUtils.hasText(s)) return List.of();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .toList();
    }

    @Override
    public List<NotifyConfig> listAll() {
        return notifyConfigMapper.selectList(
                new LambdaQueryWrapper<NotifyConfig>().orderByAsc(NotifyConfig::getModule, NotifyConfig::getEventCode));
    }

    @Override
    public List<NotifyChannel> listChannels() {
        List<NotifyChannel> list = notifyChannelMapper.selectList(null);
        list.forEach(c -> {
            if (c.getConfigJson() != null) {
                c.setConfigJson(maskSecrets(c.getConfigJson()));
            }
        });
        return list;
    }

    /** 敏感字段(appSecret/secret/corpsecret/password 等)脱敏为 ****。 */
    private String maskSecrets(String json) {
        if (!StringUtils.hasText(json)) return json;
        try {
            Map<String, Object> map = om.readValue(json, Map.class);
            map.forEach((k, v) -> {
                if (v instanceof String s && !s.isEmpty() && isSecretKey(k)) {
                    map.put(k, "****");
                }
            });
            return om.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("[通知配置] config_json 脱敏失败: {}", e.getMessage());
            return json;
        }
    }

    private static boolean isSecretKey(String k) {
        String key = k.toLowerCase();
        return key.contains("secret") || key.contains("password") || key.contains("appsecret")
                || key.contains("corpsecret") || key.equals("pwd") || key.contains("token");
    }

    @Override
    @Transactional
    public void update(String id, String roleCodes, String receiverIds, String channels, Boolean enabled) {
        NotifyConfig c = notifyConfigMapper.selectById(id);
        if (c == null) throw new com.konli.qms.common.exception.BusinessException(400, "通知配置不存在");
        if (roleCodes != null) c.setRoleCodes(roleCodes);
        if (receiverIds != null) c.setReceiverIds(receiverIds);
        if (channels != null) c.setChannels(channels);
        if (enabled != null) c.setEnabled(enabled);
        notifyConfigMapper.updateById(c);
        cache.remove(key(c.getModule(), c.getEventCode()));
    }

    @Override
    @Transactional
    public void updateChannel(String id, String webhookUrl, Boolean enabled,
                              String channelType, String configJson) {
        NotifyChannel ch = notifyChannelMapper.selectById(id);
        if (ch == null) throw new com.konli.qms.common.exception.BusinessException(400, "通知渠道不存在");
        if (webhookUrl != null) ch.setWebhookUrl(webhookUrl);
        if (enabled != null) ch.setIsEnabled(enabled);
        if (channelType != null) ch.setChannelType(channelType);
        // 点对点凭据: 前端回传的 "****" 字段保持后端原值, 其余覆盖
        if (configJson != null) {
            ch.setConfigJson(mergeMasked(ch.getConfigJson(), configJson));
        }
        notifyChannelMapper.updateById(ch);
    }

    /** 合并前端回传的凭据: 前端字段值为 **** 时保留后端原值, 其余覆盖。 */
    private String mergeMasked(String oldJson, String newJson) {
        if (!StringUtils.hasText(newJson)) return oldJson;
        if (!StringUtils.hasText(oldJson)) return newJson;
        try {
            Map<String, Object> oldMap = om.readValue(oldJson, Map.class);
            Map<String, Object> newMap = om.readValue(newJson, Map.class);
            newMap.forEach((k, v) -> {
                if ("****".equals(v)) {
                    newMap.put(k, oldMap.get(k));
                }
            });
            return om.writeValueAsString(newMap);
        } catch (Exception e) {
            log.warn("[通知配置] config_json 合并失败, 采用新值: {}", e.getMessage());
            return newJson;
        }
    }

    @Override
    public List<String> resolveRoles(String module, String eventCode) {
        CacheEntry e = load(module, eventCode);
        return (e == null || !e.enabled) ? List.of() : e.roles;
    }

    @Override
    public List<String> resolveReceiverIds(String module, String eventCode) {
        CacheEntry e = load(module, eventCode);
        return (e == null || !e.enabled) ? List.of() : e.receiverIds;
    }

    @Override
    public List<String> resolveChannels(String module, String eventCode) {
        CacheEntry e = load(module, eventCode);
        return (e == null || !e.enabled) ? List.of() : e.channels;
    }

    private CacheEntry load(String module, String eventCode) {
        String k = key(module, eventCode);
        CacheEntry e = cache.get(k);
        if (e != null) return e;
        NotifyConfig c = notifyConfigMapper.selectOne(new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getModule, module)
                .eq(NotifyConfig::getEventCode, eventCode));
        if (c == null) {
            e = new CacheEntry(List.of(), List.of(), List.of(), false);
        } else {
            e = new CacheEntry(split(c.getRoleCodes()), split(c.getReceiverIds()), split(c.getChannels()),
                    Boolean.TRUE.equals(c.getEnabled()));
        }
        cache.put(k, e);
        return e;
    }
}
