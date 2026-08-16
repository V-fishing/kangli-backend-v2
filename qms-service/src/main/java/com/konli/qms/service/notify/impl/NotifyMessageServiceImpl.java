package com.konli.qms.service.notify.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.entity.NotifyMessage;
import com.konli.qms.domain.notify.mapper.NotifyChannelMapper;
import com.konli.qms.domain.notify.mapper.NotifyMessageMapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.notify.DirectNotifyService;
import com.konli.qms.service.notify.NotifyCenterRow;
import com.konli.qms.service.notify.NotifyMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 通知中心实现: 复用 DirectNotifyService 发送核心, 发送记录分页查询。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyMessageServiceImpl implements NotifyMessageService {

    private final NotifyMessageMapper notifyMessageMapper;
    private final NotifyChannelMapper notifyChannelMapper;
    private final SysUserMapper sysUserMapper;
    private final DirectNotifyService directNotifyService;

    @Override
    public int send(String title, String content, List<String> receiverIds,
                    List<String> channels, String bizType, String bizId, String bizNo) {
        if (!StringUtils.hasText(title)) throw new BusinessException(400, "通知标题不能为空");
        if (!StringUtils.hasText(content)) throw new BusinessException(400, "通知内容不能为空");
        if (receiverIds == null || receiverIds.isEmpty()) throw new BusinessException(400, "请至少选择一位接收人");
        if (channels == null || channels.isEmpty()) throw new BusinessException(400, "请至少选择一种通知渠道");

        CompanyContext.CurrentUser cur = CompanyContext.get();
        String senderId = cur != null ? cur.userId() : null;
        String senderName = queryUserName(senderId);
        String orgId = cur != null ? cur.orgId() : null;

        int created = 0;
        List<String> usedChannels = new ArrayList<>(channels);
        for (String receiverId : receiverIds) {
            SysUser u = sysUserMapper.selectById(receiverId);
            if (u == null || !"启用".equals(u.getStatus())) {
                throw new BusinessException(400, "接收人不存在或已停用");
            }
            DirectNotifyService.DirectReceiver receiver = new DirectNotifyService.DirectReceiver(
                    u.getId(), u.getRealName(), u.getUsername(), u.getEmail(), u.getPhone());
            created += directNotifyService.sendToUser(orgId, senderId, senderName, receiver,
                    usedChannels, title, content, bizType, bizId, bizNo).size();
        }
        return created;
    }

    @Override
    public PageResult<NotifyMessage> list(String status, String channel, String keyword, int page, int size) {
        if (size <= 0) size = 20;
        if (page <= 0) page = 1;
        Page<NotifyMessage> pg = new Page<>(page, size);
        LambdaQueryWrapper<NotifyMessage> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) w.eq(NotifyMessage::getStatus, status.trim());
        if (StringUtils.hasText(channel)) w.eq(NotifyMessage::getChannel, channel.trim());
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            w.and(aw -> aw.like(NotifyMessage::getTitle, kw)
                    .or().like(NotifyMessage::getReceiverName, kw)
                    .or().like(NotifyMessage::getBizNo, kw)
                    .or().like(NotifyMessage::getSenderName, kw));
        }
        w.orderByDesc(NotifyMessage::getCreatedAt);
        notifyMessageMapper.selectPage(pg, w);
        PageResult<NotifyMessage> pr = new PageResult<>();
        pr.setRecords(pg.getRecords());
        pr.setTotal(pg.getTotal());
        pr.setPage(page);
        pr.setSize(size);
        return pr;
    }

    @Override
    public List<NotifyChannel> listDirectChannels() {
        return notifyChannelMapper.selectList(new LambdaQueryWrapper<NotifyChannel>()
                .eq(NotifyChannel::getChannelType, "direct"));
    }

    @Override
    public PageResult<NotifyCenterRow> centerPage(String status, String channel, String keyword,
                                                 int page, int size) {
        if (size <= 0) size = 20;
        if (page <= 0) page = 1;
        // 拉取全部投递明细(通知量中等, 内存按 notification_id 归并; 审计页可接受)
        List<NotifyMessage> all = notifyMessageMapper.selectList(
                new LambdaQueryWrapper<NotifyMessage>().orderByDesc(NotifyMessage::getCreatedAt));
        // 归并: key = notification_id(非空) 否则 自身 id
        Map<String, List<NotifyMessage>> groups = new LinkedHashMap<>();
        for (NotifyMessage m : all) {
            String key = (m.getNotificationId() != null && !m.getNotificationId().isBlank())
                    ? m.getNotificationId() : m.getId();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
        }
        List<NotifyCenterRow> rows = new ArrayList<>();
        for (Map.Entry<String, List<NotifyMessage>> e : groups.entrySet()) {
            List<NotifyMessage> items = e.getValue();
            NotifyMessage first = items.get(0);
            NotifyCenterRow row = new NotifyCenterRow();
            row.setId(e.getKey());
            row.setSendTime(items.stream().map(NotifyMessage::getSendTime).filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo).orElse(first.getSendTime()));
            row.setSenderName(first.getSenderName());
            row.setReceiverId(first.getReceiverId());
            row.setReceiverName(first.getReceiverName());
            row.setTitle(first.getTitle());
            row.setBizType(first.getBizType());
            row.setBizId(first.getBizId());
            // 优先取调用方传入的可读单据号; 未传则为空(前端展示「—」), 不再回退显示 UUID
            row.setBizNo(first.getBizNo());
            row.setChannel(first.getChannel());
            // 综合状态: 任一失败→失败; 否则任一发送中→发送中; 否则成功
            boolean anyFail = items.stream().anyMatch(i -> "失败".equals(i.getStatus()));
            boolean anySending = items.stream().anyMatch(i -> "发送中".equals(i.getStatus()));
            row.setStatus(anyFail ? "失败" : (anySending ? "发送中" : "成功"));
            List<NotifyCenterRow.Delivery> deliveries = new ArrayList<>();
            for (NotifyMessage i : items) {
                NotifyCenterRow.Delivery d = new NotifyCenterRow.Delivery();
                d.setChannel(i.getChannel());
                d.setStatus(i.getStatus());
                d.setFailReason(i.getFailReason());
                deliveries.add(d);
            }
            row.setDeliveries(deliveries);
            rows.add(row);
        }
        // 筛选(状态/渠道/关键字)
        if (StringUtils.hasText(status)) {
            rows = rows.stream().filter(r -> status.equals(r.getStatus())
                    || r.getDeliveries().stream().anyMatch(d -> status.equals(d.getStatus()))).toList();
        }
        if (StringUtils.hasText(channel)) {
            rows = rows.stream().filter(r -> r.getDeliveries().stream()
                    .anyMatch(d -> channel.equals(d.getChannel()))).toList();
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            rows = rows.stream().filter(r ->
                    (r.getTitle() != null && r.getTitle().contains(kw))
                    || (r.getReceiverName() != null && r.getReceiverName().contains(kw))
                    || (r.getBizNo() != null && r.getBizNo().contains(kw))
                    || (r.getSenderName() != null && r.getSenderName().contains(kw))
            ).toList();
        }
        long total = rows.size();
        int from = Math.min((page - 1) * size, rows.size());
        int to = Math.min(from + size, rows.size());
        List<NotifyCenterRow> pageRows = rows.subList(from, to);
        PageResult<NotifyCenterRow> pr = new PageResult<>();
        pr.setRecords(pageRows);
        pr.setTotal(total);
        pr.setPage(page);
        pr.setSize(size);
        return pr;
    }

    private String queryUserName(String userId) {
        if (userId == null) return null;
        SysUser u = sysUserMapper.selectById(userId);
        return u != null ? u.getRealName() : null;
    }
}
