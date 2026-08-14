package com.konli.qms.service.notify;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.mapper.NotifyChannelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 外部渠道(钉钉/企微/自定义 webhook)发送。站内弹窗由 SSE 推送,无需此处投递。
 * 逻辑复用原 SpcNotifyChannelServiceImpl 的 webhook 投递实现,供全模块统一调用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalNotifyService {

    private final NotifyChannelMapper notifyChannelMapper;
    private static final ObjectMapper om = new ObjectMapper();
    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    /** 按渠道名列表发送文本消息; 仅启用且非站内弹窗的渠道才投递。 */
    public void send(List<String> channelNames, String title, String content) {
        if (channelNames == null || channelNames.isEmpty()) return;
        List<NotifyChannel> channels = notifyChannelMapper.selectList(
                new LambdaQueryWrapper<NotifyChannel>().in(NotifyChannel::getChannel, channelNames));
        String message = "【" + title + "】\n" + content;
        for (NotifyChannel c : channels) {
            if (!Boolean.TRUE.equals(c.getIsEnabled())) continue;
            if ("站内弹窗".equals(c.getChannel())) continue; // 站内信由 NotificationService 处理
            try {
                dispatch(c, message);
            } catch (Exception e) {
                log.warn("[外部通知] 发送失败 channel={}: {}", c.getChannel(), e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatch(NotifyChannel channel, String message) throws Exception {
        String webhook = channel.getWebhookUrl() != null ? channel.getWebhookUrl() : "";
        if (webhook.isEmpty()) {
            log.warn("[外部通知] {} 未配置 webhook,跳过", channel.getChannel());
            return;
        }
        String ch = channel.getChannel();
        Map<String, Object> body;
        if ("钉钉".equals(ch)) {
            body = Map.of("msgtype", "text", "text", Map.of("content", message));
        } else {
            body = Map.of("msgtype", "markdown", "markdown",
                    Map.of("content", message.replace("\n", "\n\n")));
        }
        String json = om.writeValueAsString(body);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(webhook))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            log.warn("[外部通知] {} webhook 返回 {}: {}", ch, resp.statusCode(), resp.body());
        } else {
            log.info("[外部通知] {} 发送成功", ch);
        }
    }
}
