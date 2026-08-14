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
import com.konli.qms.service.notify.NotifyMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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

    private String queryUserName(String userId) {
        if (userId == null) return null;
        SysUser u = sysUserMapper.selectById(userId);
        return u != null ? u.getRealName() : null;
    }
}
