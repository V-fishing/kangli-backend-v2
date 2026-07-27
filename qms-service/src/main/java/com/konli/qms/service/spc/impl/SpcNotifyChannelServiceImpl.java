package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcAlarm;
import com.konli.qms.domain.spc.entity.SpcNotifyChannel;
import com.konli.qms.domain.spc.entity.SpcNotifyRecord;
import com.konli.qms.domain.spc.mapper.SpcNotifyChannelMapper;
import com.konli.qms.domain.spc.mapper.SpcNotifyRecordMapper;
import com.konli.qms.service.spc.SpcNotifyChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SPC 通知渠道: 站内弹窗 / 企微 Webhook / 钉钉 Webhook / 自定义 Webhook。
 * 管理员通过 /spc/notify-channels 接口配置渠道, config_json 存放 webhook URL 等。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpcNotifyChannelServiceImpl implements SpcNotifyChannelService {

    private final SpcNotifyChannelMapper spcNotifyChannelMapper;
    private final SpcNotifyRecordMapper spcNotifyRecordMapper;
    private static final ObjectMapper om = new ObjectMapper();
    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public List<SpcNotifyChannel> list() {
        return spcNotifyChannelMapper.selectList(null);
    }

    @Override
    public void toggle(String id, boolean enabled) {
        SpcNotifyChannel channel = spcNotifyChannelMapper.selectById(id);
        if (channel == null) throw new BusinessException(400, "通知渠道不存在");
        channel.setIsEnabled(enabled);
        spcNotifyChannelMapper.updateById(channel);
    }

    @Override
    public void send(SpcAlarm alarm) {
        if (alarm == null || alarm.getId() == null) return;

        List<SpcNotifyChannel> chs = spcNotifyChannelMapper.selectList(
                new LambdaQueryWrapper<SpcNotifyChannel>()
                        .eq(SpcNotifyChannel::getIsEnabled, true)
                        .and(w -> w.eq(SpcNotifyChannel::getOrgId, alarm.getOrgId())
                                .or().isNull(SpcNotifyChannel::getOrgId)));
        if (chs.isEmpty()) return;

        // 预警仅站内弹窗; 报警走全部启用渠道
        if ("预警".equals(alarm.getLevel())) {
            chs = chs.stream().filter(c -> "站内弹窗".equals(c.getChannel())).toList();
        }
        if (chs.isEmpty()) return;

        String message = buildMessage(alarm);
        for (SpcNotifyChannel c : chs) {
            SpcNotifyRecord rec = new SpcNotifyRecord();
            rec.setOrgId(alarm.getOrgId());
            rec.setAlarmId(alarm.getId());
            rec.setChannel(c.getChannel());
            rec.setChannelName(c.getChannel());
            rec.setMessage(message);
            try {
                dispatch(c, message, alarm);
                rec.setStatus("SENT");
                rec.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                log.warn("[SPC通知] 发送失败 channel={} alarm={}: {}", c.getChannel(), alarm.getCode(), e.getMessage());
                rec.setStatus("FAILED");
                rec.setError(e.getMessage());
            }
            spcNotifyRecordMapper.insert(rec);
        }
    }

    @Override
    public List<SpcNotifyRecord> listRecords(String alarmId) {
        LambdaQueryWrapper<SpcNotifyRecord> w = new LambdaQueryWrapper<>();
        if (alarmId != null && !alarmId.isBlank()) w.eq(SpcNotifyRecord::getAlarmId, alarmId);
        w.orderByDesc(SpcNotifyRecord::getCreatedAt);
        List<SpcNotifyRecord> list = spcNotifyRecordMapper.selectList(w);
        return list == null ? Collections.emptyList() : list;
    }

    // ── 投递实现 ──────────────────────────────────────────

    private String buildMessage(SpcAlarm a) {
        return String.format("[SPC异常报警] 参数:%s 规则:%s 级别:%s 当前值:%s 时间:%s 告警号:%s",
                a.getParamName(), a.getTriggeredRule(), a.getLevel(),
                a.getCurrentValue(), a.getAlarmTime(), a.getCode());
    }

    @SuppressWarnings("unchecked")
    private void dispatch(SpcNotifyChannel channel, String message, SpcAlarm alarm) throws Exception {
        String channelType = channel.getChannel();
        Map<String, Object> cfg = channel.getConfigJson() != null && !channel.getConfigJson().isBlank()
                ? om.readValue(channel.getConfigJson(), Map.class) : Collections.emptyMap();
        String webhook = (String) cfg.getOrDefault("webhook", "");

        switch (channelType) {
            case "站内弹窗":
                // 无需外部投递: WebSocket 已推送, 仅落记录
                return;
            case "企业微信":
            case "钉钉":
            case "自定义Webhook":
                if (webhook.isEmpty()) throw new BusinessException(400, channelType + " 未配置 webhook");
                sendWebhook(webhook, channelType, message, alarm);
                break;
            default:
                throw new BusinessException(400, "不支持的通知渠道: " + channelType);
        }
    }

    private void sendWebhook(String url, String channelType, String message, SpcAlarm alarm) throws Exception {
        Map<String, Object> body;
        if ("钉钉".equals(channelType)) {
            body = Map.of("msgtype", "text", "text", Map.of("content", message));
        } else {
            // 企业微信 / 自定义 webhook → Markdown
            body = Map.of("msgtype", "markdown", "markdown", Map.of("content",
                    "## SPC 异常报警\n"
                    + "> 参数: " + alarm.getParamName() + "\n"
                    + "> 规则: " + alarm.getTriggeredRule() + "\n"
                    + "> 级别: **" + alarm.getLevel() + "**\n"
                    + "> 当前值: " + alarm.getCurrentValue() + "\n"
                    + "> 时间: " + alarm.getAlarmTime() + "\n"
                    + "> 告警号: " + alarm.getCode() + "\n"));
        }
        String json = om.writeValueAsString(body);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new BusinessException(500, channelType + " webhook 返回 " + resp.statusCode() + ": " + resp.body());
        }
        log.info("[SPC通知] {} 发送成功 alarm={}", channelType, alarm.getCode());
    }
}
