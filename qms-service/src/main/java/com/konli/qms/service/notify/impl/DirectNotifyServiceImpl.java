package com.konli.qms.service.notify.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.entity.NotifyMessage;
import com.konli.qms.domain.notify.mapper.NotifyChannelMapper;
import com.konli.qms.domain.notify.mapper.NotifyMessageMapper;
import com.konli.qms.service.notify.DirectNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.internet.MimeMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 点对点通知发送核心实现。
 * - 按 channel_type=direct 分派: 钉钉工作通知 / 企业微信应用消息 / 邮件(SMTP) / 短信(预留)
 * - 异步线程池外发, 发送记录先落库(状态=发送中), 异步回写成功/失败+fail_reason
 * - 失败不抛异常、不打印凭据(appSecret/corpsecret/SMTP 密码)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectNotifyServiceImpl implements DirectNotifyService {

    private final NotifyChannelMapper notifyChannelMapper;
    private final NotifyMessageMapper notifyMessageMapper;

    private static final ObjectMapper om = new ObjectMapper();
    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    private static final ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "direct-notify");
        t.setDaemon(true);
        return t;
    });

    /** access_token 缓存(钉钉/企微, 有效期 7200s, 提前 60s 刷新)。 */
    private record TokenEntry(String token, long expireAt) {}
    private static final Map<String, TokenEntry> tokenCache = new ConcurrentHashMap<>();

    @Override
    public List<NotifyMessage> sendToUser(String orgId, String senderId, String senderName,
                                          DirectReceiver receiver, List<String> channels,
                                          String title, String content,
                                          String bizType, String bizId, String bizNo) {
        if (receiver == null || channels == null || channels.isEmpty()) return List.of();
        List<NotifyMessage> records = new ArrayList<>();
        for (String channelName : channels) {
            NotifyChannel ch = notifyChannelMapper.selectOne(new LambdaQueryWrapper<NotifyChannel>()
                    .eq(NotifyChannel::getChannel, channelName));
            if (ch == null || !Boolean.TRUE.equals(ch.getIsEnabled())) continue;
            if (!"direct".equals(ch.getChannelType())) continue;
            // 站内弹窗属于 NotificationService 站内信体系, 不走点对点外发(避免写入未知渠道类型失败记录)
            if ("站内弹窗".equals(ch.getChannel())) continue;
            NotifyMessage rec = new NotifyMessage();
            rec.setOrgId(orgId);
            rec.setSenderId(senderId);
            rec.setSenderName(senderName);
            rec.setReceiverId(receiver.userId());
            rec.setReceiverName(receiver.realName());
            rec.setReceiverType("user");
            rec.setChannel(ch.getChannel());
            rec.setChannelType("direct");
            rec.setTitle(title);
            rec.setContent(content);
            rec.setBizType(bizType);
            rec.setBizId(bizId);
            rec.setBizNo(bizNo);
            rec.setStatus("发送中");
            rec.setSendTime(LocalDateTime.now());
            notifyMessageMapper.insert(rec);
            records.add(rec);
            final String recordId = rec.getId();
            executor.submit(() -> doSend(ch, receiver, recordId, title, content));
        }
        return records;
    }

    private void doSend(NotifyChannel ch, DirectReceiver receiver, String recordId,
                        String title, String content) {
        String fail = null;
        try {
            String type = channelTypeOf(ch);
            fail = switch (type == null ? "" : type) {
                case "dingtalk" -> sendDingTalk(ch, receiver, content);
                case "wecom" -> sendWecom(ch, receiver, content);
                case "mail" -> sendMail(ch, receiver, title, content);
                case "sms" -> sendSms(ch, receiver);
                case "inapp" -> null; // 站内弹窗由 NotificationService 处理, 不应到达此处
                default -> "未知渠道类型: " + type;
            };
        } catch (Exception e) {
            fail = "发送异常: " + e.getMessage();
            log.warn("[点对点通知] 发送失败 channel={}: {}", ch.getChannel(), e.getMessage());
        }
        try {
            NotifyMessage rec = notifyMessageMapper.selectById(recordId);
            if (rec != null) {
                rec.setStatus(fail == null ? "成功" : "失败");
                rec.setFailReason(fail);
                notifyMessageMapper.updateById(rec);
            }
        } catch (Exception e) {
            log.warn("[点对点通知] 回写记录失败 id={}: {}", recordId, e.getMessage());
        }
    }

    // ============ 渠道分派 ============

    /** 从 config_json 读 type(钉钉/企微/邮件/短信的适配器标识)。 */
    private String channelTypeOf(NotifyChannel ch) {
        String json = ch.getConfigJson();
        if (!StringUtils.hasText(json)) return null;
        try {
            JsonNode n = om.readTree(json);
            return n.path("type").asText(null);
        } catch (Exception e) {
            log.warn("[点对点通知] {} config_json 解析失败: {}", ch.getChannel(), e.getMessage());
            return null;
        }
    }

    private Map<String, Object> cfg(NotifyChannel ch) {
        String json = ch.getConfigJson();
        if (!StringUtils.hasText(json)) return Map.of();
        try {
            JsonNode n = om.readTree(json);
            return om.convertValue(n, Map.class);
        } catch (Exception e) {
            log.warn("[点对点通知] {} config_json 解析失败: {}", ch.getChannel(), e.getMessage());
            return Map.of();
        }
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? null : v.toString().trim();
    }

    private static int intVal(Map<String, Object> m, String k, int def) {
        Object v = m.get(k);
        if (v == null) return def;
        try {
            return (int) Double.parseDouble(v.toString());
        } catch (Exception e) {
            return def;
        }
    }

    // ============ 钉钉工作通知 ============

    private String sendDingTalk(NotifyChannel ch, DirectReceiver receiver, String content) {
        Map<String, Object> c = cfg(ch);
        String appKey = str(c, "appKey");
        String appSecret = str(c, "appSecret");
        String agentId = str(c, "agentId");
        if (!StringUtils.hasText(appKey) || !StringUtils.hasText(appSecret)) return "钉钉未配置 appKey/appSecret";
        if (!StringUtils.hasText(agentId)) return "钉钉未配置 agentId";
        String token = dingAccessToken(appKey, appSecret);
        if (token == null) return "钉钉获取 access_token 失败";
        String phone = receiver.phone();
        if (!StringUtils.hasText(phone)) return "接收人未维护手机号,无法桥接钉钉账号";
        String userId = dingUserIdByMobile(token, phone);
        if (userId == null) return "钉钉未找到手机号 " + phone + " 对应用户";
        Map<String, Object> body = Map.of(
                "agent_id", intVal(c, "agentId", 0),
                "userid_list", userId,
                "msg", Map.of("msgtype", "text", "text", Map.of("content", content)));
        JsonNode resp = postJson("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2?access_token=" + token, body);
        int errcode = resp == null ? -1 : resp.path("errcode").asInt(-1);
        if (errcode != 0) {
            return "钉钉发送失败 errcode=" + errcode + " " + (resp == null ? "" : resp.path("errmsg").asText(""));
        }
        return null;
    }

    private String dingAccessToken(String appKey, String appSecret) {
        String key = "ding:" + appKey;
        TokenEntry e = tokenCache.get(key);
        if (e != null && System.currentTimeMillis() < e.expireAt()) return e.token();
        String url = "https://oapi.dingtalk.com/gettoken?appkey=" + appKey + "&appsecret=" + appSecret;
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(8))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode n = om.readTree(resp.body());
            if (n.path("errcode").asInt(-1) != 0) {
                log.warn("[点对点通知] 钉钉 gettoken 失败: {}", n.path("errmsg").asText(""));
                return null;
            }
            String token = n.path("access_token").asText();
            int expire = n.path("expire_in").asInt(7200);
            tokenCache.put(key, new TokenEntry(token, System.currentTimeMillis() + (expire - 60L) * 1000));
            return token;
        } catch (Exception ex) {
            log.warn("[点对点通知] 钉钉 gettoken 异常: {}", ex.getMessage());
            return null;
        }
    }

    private String dingUserIdByMobile(String token, String phone) {
        try {
            JsonNode resp = postJson("https://oapi.dingtalk.com/topapi/v2/user/getbymobile?access_token=" + token,
                    Map.of("mobile", phone));
            if (resp != null && resp.path("errcode").asInt(-1) == 0) {
                return resp.path("result").path("userid").asText(null);
            }
        } catch (Exception e) {
            log.warn("[点对点通知] 钉钉 getbymobile 异常: {}", e.getMessage());
        }
        return null;
    }

    // ============ 企业微信应用消息 ============

    private String sendWecom(NotifyChannel ch, DirectReceiver receiver, String content) {
        Map<String, Object> c = cfg(ch);
        String corpId = str(c, "corpId");
        String secret = str(c, "secret");
        String agentId = str(c, "agentId");
        if (!StringUtils.hasText(corpId) || !StringUtils.hasText(secret)) return "企业微信未配置 corpId/secret";
        if (!StringUtils.hasText(agentId)) return "企业微信未配置 agentId";
        String token = wecomAccessToken(corpId, secret);
        if (token == null) return "企业微信获取 access_token 失败";
        String username = receiver.username();
        if (!StringUtils.hasText(username)) return "接收人缺少系统账号,无法桥接企微成员";
        Map<String, Object> body = Map.of(
                "touser", username,
                "msgtype", "text",
                "agentid", intVal(c, "agentId", 0),
                "text", Map.of("content", content));
        JsonNode resp = postJson("https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token, body);
        int errcode = resp == null ? -1 : resp.path("errcode").asInt(-1);
        if (errcode != 0) {
            return "企业微信发送失败 errcode=" + errcode + " " + (resp == null ? "" : resp.path("errmsg").asText(""));
        }
        return null;
    }

    private String wecomAccessToken(String corpId, String secret) {
        String key = "wecom:" + corpId;
        TokenEntry e = tokenCache.get(key);
        if (e != null && System.currentTimeMillis() < e.expireAt()) return e.token();
        String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpId + "&corpsecret=" + secret;
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(8))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode n = om.readTree(resp.body());
            if (n.path("errcode").asInt(-1) != 0) {
                log.warn("[点对点通知] 企微 gettoken 失败: {}", n.path("errmsg").asText(""));
                return null;
            }
            String token = n.path("access_token").asText();
            tokenCache.put(key, new TokenEntry(token, System.currentTimeMillis() + 7140_000L));
            return token;
        } catch (Exception ex) {
            log.warn("[点对点通知] 企微 gettoken 异常: {}", ex.getMessage());
            return null;
        }
    }

    // ============ 邮件 ============

    private String sendMail(NotifyChannel ch, DirectReceiver receiver, String title, String content) throws Exception {
        Map<String, Object> c = cfg(ch);
        String host = str(c, "host");
        String from = str(c, "from");
        String username = str(c, "username");
        String password = str(c, "password");
        if (!StringUtils.hasText(host)) return "邮件未配置 SMTP host";
        if (!StringUtils.hasText(from)) return "邮件未配置发件人(from)";
        String email = receiver.email();
        if (!StringUtils.hasText(email)) return "接收人未维护邮箱";
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(intVal(c, "port", 465));
        if (StringUtils.hasText(username)) sender.setUsername(username);
        if (StringUtils.hasText(password)) sender.setPassword(password);
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "8000");
        props.put("mail.smtp.connectiontimeout", "8000");
        boolean ssl = intVal(c, "port", 465) == 465;
        if (ssl) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        MimeMessage msg = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(title == null ? "康立QMS通知" : title);
        helper.setText(content, false);
        sender.send(msg);
        return null;
    }

    // ============ 短信(预留) ============

    private String sendSms(NotifyChannel ch, DirectReceiver receiver) {
        Map<String, Object> c = cfg(ch);
        String provider = str(c, "provider");
        if (!StringUtils.hasText(provider)) return "短信服务商未配置(预留适配器)";
        return "短信服务商 " + provider + " 尚未接入(预留适配器)";
    }

    // ============ HTTP 工具 ============

    private JsonNode postJson(String url, Map<String, Object> body) {
        try {
            String json = om.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(8))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                log.warn("[点对点通知] HTTP {} 返回 {}: {}", url, resp.statusCode(), resp.body());
                return null;
            }
            return om.readTree(resp.body());
        } catch (Exception e) {
            log.warn("[点对点通知] HTTP 请求异常 {}: {}", url, e.getMessage());
            return null;
        }
    }
}
