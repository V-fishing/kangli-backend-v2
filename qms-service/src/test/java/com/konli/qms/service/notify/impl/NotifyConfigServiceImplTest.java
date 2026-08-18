package com.konli.qms.service.notify.impl;

import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.entity.NotifyConfig;
import com.konli.qms.domain.notify.mapper.NotifyChannelMapper;
import com.konli.qms.domain.notify.mapper.NotifyConfigMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 统一通知配置服务单元测试（M10 notify/system，Mockito）。
 * 聚焦：listAll 排序、listChannels 敏感字段脱敏、update/updateChannel 兜底、缓存失效、
 * resolveRoles/ReceiverIds/Channels 经缓存命中、空 roleCodes 解析。
 */
@ExtendWith(MockitoExtension.class)
class NotifyConfigServiceImplTest {

    @Mock NotifyConfigMapper notifyConfigMapper;
    @Mock NotifyChannelMapper notifyChannelMapper;

    @InjectMocks NotifyConfigServiceImpl service;

    private NotifyConfig cfg(String id, String module, String event, String roles,
                             String receivers, String channels, Boolean enabled) {
        NotifyConfig c = new NotifyConfig();
        c.setId(id);
        c.setModule(module);
        c.setEventCode(event);
        c.setRoleCodes(roles);
        c.setReceiverIds(receivers);
        c.setChannels(channels);
        c.setEnabled(enabled);
        return c;
    }

    @Test
    @DisplayName("listAll:按 module,eventCode 升序返回")
    void listAll_returnsSorted() {
        NotifyConfig a = cfg("1", "sqm", "audit", null, null, null, true);
        NotifyConfig b = cfg("2", "ncm", "8d", null, null, null, true);
        when(notifyConfigMapper.selectList(any())).thenReturn(List.of(b, a));

        List<NotifyConfig> res = service.listAll();
        assertThat(res).hasSize(2);
        assertThat(res.get(0).getModule()).isEqualTo("ncm");
        assertThat(res.get(1).getModule()).isEqualTo("sqm");
    }

    @Test
    @DisplayName("listChannels:configJson 含 secret 字段脱敏为 ****")
    void listChannels_masksSecrets() {
        NotifyChannel ch = new NotifyChannel();
        ch.setId("c1");
        ch.setChannel("dingtalk");
        ch.setConfigJson("{\"appKey\":\"k\",\"appSecret\":\"s3cr3t\",\"token\":\"t\"}");
        when(notifyChannelMapper.selectList(any())).thenReturn(List.of(ch));

        List<NotifyChannel> res = service.listChannels();
        assertThat(res).hasSize(1);
        String json = res.get(0).getConfigJson();
        assertThat(json).contains("\"appSecret\":\"****\"");
        assertThat(json).contains("\"token\":\"****\"");
        assertThat(json).contains("\"appKey\":\"k\"");
        assertThat(json).doesNotContain("s3cr3t");
    }

    @Test
    @DisplayName("listChannels:非 secret 字段(pingUrl)不脱敏")
    void listChannels_keepsNonSecret() {
        NotifyChannel ch = new NotifyChannel();
        ch.setConfigJson("{\"pingUrl\":\"http://x\",\"password\":\"p\"}");
        when(notifyChannelMapper.selectList(any())).thenReturn(List.of(ch));

        String json = service.listChannels().get(0).getConfigJson();
        assertThat(json).contains("\"pingUrl\":\"http://x\"");
        assertThat(json).contains("\"password\":\"****\"");
    }

    @Test
    @DisplayName("update:查不到配置抛 BusinessException")
    void update_notFound_throws() {
        when(notifyConfigMapper.selectById("missing")).thenReturn(null);
        assertThatThrownBy(() -> service.update("missing", "R1", null, null, null))
                .isInstanceOf(com.konli.qms.common.exception.BusinessException.class)
                .hasMessageContaining("通知配置不存在");
        verify(notifyConfigMapper, never()).updateById(any(NotifyConfig.class));
    }

    @Test
    @DisplayName("update:部分字段 null 只更新传入项,并失效缓存")
    void update_partial_updatesAndEvictsCache() {
        NotifyConfig c = cfg("1", "sqm", "audit", "R1", "U1", "webhook", true);
        when(notifyConfigMapper.selectById("1")).thenReturn(c);

        // 预热缓存
        service.resolveChannels("sqm", "audit");
        // 仅更新 enabled
        service.update("1", null, null, null, false);

        ArgumentCaptor<NotifyConfig> cap = ArgumentCaptor.forClass(NotifyConfig.class);
        verify(notifyConfigMapper).updateById(cap.capture());
        assertThat(cap.getValue().getEnabled()).isFalse();
        assertThat(cap.getValue().getRoleCodes()).isEqualTo("R1"); // 未变
    }

    @Test
    @DisplayName("updateChannel:查不到渠道抛 BusinessException")
    void updateChannel_notFound_throws() {
        when(notifyChannelMapper.selectById("missing")).thenReturn(null);
        assertThatThrownBy(() -> service.updateChannel("missing", null, null, null, null))
                .isInstanceOf(com.konli.qms.common.exception.BusinessException.class)
                .hasMessageContaining("通知渠道不存在");
        verify(notifyChannelMapper, never()).updateById(any(NotifyChannel.class));
    }

    @Test
    @DisplayName("updateChannel:回传 **** 字段保留后端原值")
    void updateChannel_keepsMaskedSecrets() {
        NotifyChannel ch = new NotifyChannel();
        ch.setId("c1");
        ch.setConfigJson("{\"appSecret\":\"realSecret\"}");
        when(notifyChannelMapper.selectById("c1")).thenReturn(ch);

        service.updateChannel("c1", "http://hook", true, "webhook", "{\"appSecret\":\"****\",\"token\":\"new\"}");

        ArgumentCaptor<NotifyChannel> cap = ArgumentCaptor.forClass(NotifyChannel.class);
        verify(notifyChannelMapper).updateById(cap.capture());
        String json = cap.getValue().getConfigJson();
        assertThat(json).contains("\"appSecret\":\"realSecret\"");
        assertThat(json).contains("\"token\":\"new\"");
        assertThat(cap.getValue().getWebhookUrl()).isEqualTo("http://hook");
        assertThat(cap.getValue().getIsEnabled()).isTrue();
    }

    @Test
    @DisplayName("resolveRoles:启用配置返回解析角色,禁用返回空")
    void resolveRoles_disabledReturnsEmpty() {
        NotifyConfig c = cfg("1", "sqm", "audit", "R1, R2", "U1", "webhook", false);
        when(notifyConfigMapper.selectOne(any())).thenReturn(c);
        assertThat(service.resolveRoles("sqm", "audit")).isEmpty();
    }

    @Test
    @DisplayName("resolveRoles:启用配置返回去空白角色列表,并缓存命中")
    void resolveRoles_enabledReturnsTrimmed() {
        NotifyConfig c = cfg("1", "sqm", "audit", "R1, R2", "U1", "webhook", true);
        when(notifyConfigMapper.selectOne(any())).thenReturn(c);

        List<String> roles = service.resolveRoles("sqm", "audit");
        assertThat(roles).containsExactly("R1", "R2");
        // 二次调用走缓存,不再查库(selectOne 仅 stub 一次)
        List<String> roles2 = service.resolveRoles("sqm", "audit");
        assertThat(roles2).containsExactly("R1", "R2");
    }

    @Test
    @DisplayName("resolveReceiverIds/Channels:空串返回空列表")
    void resolveReceiversEmptyWhenBlank() {
        NotifyConfig c = cfg("1", "sqm", "audit", "", "", "", true);
        when(notifyConfigMapper.selectOne(any())).thenReturn(c);
        assertThat(service.resolveReceiverIds("sqm", "audit")).isEmpty();
        assertThat(service.resolveChannels("sqm", "audit")).isEmpty();
    }

    @Test
    @DisplayName("resolve*:查不到配置返回空(静默)")
    void resolveReturnsEmptyWhenNotFound() {
        when(notifyConfigMapper.selectOne(any())).thenReturn(null);
        assertThat(service.resolveRoles("x", "y")).isEmpty();
        assertThat(service.resolveReceiverIds("x", "y")).isEmpty();
        assertThat(service.resolveChannels("x", "y")).isEmpty();
    }
}
