package com.konli.qms.service.notify;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.entity.NotifyMessage;
import com.konli.qms.domain.notify.entity.SysNotification;
import com.konli.qms.domain.notify.mapper.NotifyChannelMapper;
import com.konli.qms.domain.notify.mapper.NotifyMessageMapper;
import com.konli.qms.domain.notify.mapper.SysNotificationMapper;
import com.konli.qms.service.uop.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全平台站内消息通知服务。
 * 支持:按角色解析接收人、按用户推送、当前用户收件箱、未读计数、标记已读。
 * 另提供 notify(module,eventCode,...) 统一入口,按 notify_config 配置解析角色与外部渠道。
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SysNotificationMapper notificationMapper;
    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotifyConfigService notifyConfigService;
    private final ExternalNotifyService externalNotifyService;
    private final DirectNotifyService directNotifyService;
    private final NotifyChannelMapper notifyChannelMapper;
    private final NotifyMessageMapper notifyMessageMapper;

    /**
     * 统一通知入口: 按配置解析接收人(角色用户 ∪ 具体接收人)与外部渠道。
     * 站内信发给每个接收人; webhook 渠道群发; direct 渠道(钉钉/企微/邮件/短信)按接收人逐人点对点发送。
     * 配置禁用或查不到时静默不发,不影响主流程。
     */
    public void notify(String module, String eventCode, String title, String content,
                        String bizType, String bizId, String link) {
        notify(module, eventCode, title, content, bizType, bizId, null, link);
    }

    /**
     * 统一通知入口(带可读单据号 bizNo): 按配置解析接收人(角色用户 ∪ 具体接收人)与外部渠道。
     * 站内信发给每个接收人; webhook 渠道群发; direct 渠道(钉钉/企微/邮件/短信)按接收人逐人点对点发送。
     * 配置禁用或查不到时静默不发,不影响主流程。
     */
    public void notify(String module, String eventCode, String title, String content,
                        String bizType, String bizId, String bizNo, String link) {
        // 业务归属 org: 取发起人当前组织, 使角色接收人按公司隔离(见 resolveUserIdsByRoles)。
        // 发起人为跨公司管理员(org=null)时退化为全局广播, 与 DataScope 范式一致。
        String orgId = currentOrgId();
        notify(module, eventCode, title, content, bizType, bizId, bizNo, link, orgId);
    }

    /** 统一通知入口(显式指定业务归属 org 做公司隔离)。 */
    public void notify(String module, String eventCode, String title, String content,
                        String bizType, String bizId, String bizNo, String link, String orgId) {
        List<String> roles = notifyConfigService.resolveRoles(module, eventCode);
        List<String> receiverIds = notifyConfigService.resolveReceiverIds(module, eventCode);
        // 接收人 = 角色解析出的(按 org 隔离的)用户 ∪ 具体接收人(显式点名, 不按 org 过滤)
        Set<String> userIds = new LinkedHashSet<>(resolveUserIdsByRoles(roles, orgId));
        if (receiverIds != null) userIds.addAll(receiverIds.stream().filter(id -> id != null && !id.isBlank()).toList());
        for (String uid : userIds) {
            insert(uid, title, content, bizType, bizId, bizNo, link);
        }
        // 渠道: 按 channel_type 分流, webhook 群发 / direct 逐人点对点
        List<String> channels = notifyConfigService.resolveChannels(module, eventCode);
        if (channels.isEmpty() || userIds.isEmpty()) {
            if (!channels.isEmpty()) externalNotifyService.send(channels, title, content);
            return;
        }
        List<String> webhookCh = new ArrayList<>();
        List<String> directCh = new ArrayList<>();
        for (NotifyChannel c : notifyChannelMapper.selectList(new LambdaQueryWrapper<NotifyChannel>()
                .in(NotifyChannel::getChannel, channels))) {
            if ("direct".equals(c.getChannelType())) directCh.add(c.getChannel());
            else webhookCh.add(c.getChannel());
        }
        if (!webhookCh.isEmpty()) externalNotifyService.send(webhookCh, title, content);
        if (!directCh.isEmpty()) {
            String senderId = null;
            String senderName = null;
            try {
                var cur = userService.getCurrent();
                senderId = cur.userId();
                senderName = cur.username();
            } catch (Exception ignored) { /* 定时任务等无当前用户场景 */ }
            for (String uid : userIds) {
                // 把站内信主记录 id 透传给外发明细, 使通知中心同一条通知的站内+外发聚合展示
                String notifId = lastInsertedId(uid, bizType, bizId, title);
                directNotifyService.sendToUser(null, senderId, senderName, buildReceiver(uid),
                        directCh, title, content, bizType, bizId, bizNo, notifId);
            }
        }
    }

    /** 取最近写入该接收人的站内信主记录 id(供外发明细挂接)。 */
    private String lastInsertedId(String userId, String bizType, String bizId, String title) {
        try {
            return notificationMapper.selectOne(new LambdaQueryWrapper<SysNotification>()
                    .eq(SysNotification::getUserId, userId)
                    .eq(bizType != null, SysNotification::getBizType, bizType)
                    .eq(bizId != null, SysNotification::getBizId, bizId)
                    .eq(SysNotification::getTitle, title)
                    .orderByDesc(SysNotification::getCreateTime)
                    .last("LIMIT 1")).getId();
        } catch (Exception e) {
            return null;
        }
    }

    /** 构建点对点接收人(手机号/邮箱供钉钉·企微桥接与邮件发送)。 */
    private DirectNotifyService.DirectReceiver buildReceiver(String userId) {
        try {
            String sql = "SELECT username, real_name, COALESCE(email,''), COALESCE(phone,'') FROM ops.sys_user WHERE id = ?::uuid";
            return jdbcTemplate.query(sql, rs -> {
                if (!rs.next()) return null;
                return new DirectNotifyService.DirectReceiver(userId, rs.getString(2),
                        rs.getString(1), rs.getString(3), rs.getString(4));
            }, userId);
        } catch (Exception e) {
            return new DirectNotifyService.DirectReceiver(userId, null, null, null, null);
        }
    }

    /** 按角色码解析用户并推送(可排除某用户,避免通知发起者本人)。 */
    public void notifyRoles(List<String> roleCodes, String title, String content,
                            String bizType, String bizId, String link, String excludeUserId) {
        notifyRoles(roleCodes, title, content, bizType, bizId, null, link, excludeUserId);
    }

    /** 按角色码解析用户并推送(带可读单据号 bizNo)。 */
    public void notifyRoles(List<String> roleCodes, String title, String content,
                            String bizType, String bizId, String bizNo, String link, String excludeUserId) {
        // 未显式传 org 时, 沿用发起人当前组织做默认隔离
        notifyRoles(roleCodes, title, content, bizType, bizId, bizNo, link, excludeUserId, currentOrgId());
    }

    /** 按角色码解析用户并推送(显式指定业务归属 org 做公司隔离)。 */
    public void notifyRoles(List<String> roleCodes, String title, String content,
                            String bizType, String bizId, String bizNo, String link,
                            String excludeUserId, String orgId) {
        for (String uid : resolveUserIdsByRoles(roleCodes, orgId)) {
            if (uid != null && !uid.equals(excludeUserId)) {
                insert(uid, title, content, bizType, bizId, bizNo, link);
            }
        }
    }

    /** 推送给指定用户。 */
    public void notifyUser(String userId, String title, String content,
                           String bizType, String bizId, String link) {
        notifyUser(userId, title, content, bizType, bizId, null, link);
    }

    /** 推送给指定用户(带可读单据号 bizNo)。 */
    public void notifyUser(String userId, String title, String content,
                           String bizType, String bizId, String bizNo, String link) {
        if (userId == null) return;
        insert(userId, title, content, bizType, bizId, bizNo, link);
    }

    public List<SysNotification> listMine() {
        String uid = userService.getCurrent().userId();
        return notificationMapper.selectList(new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getUserId, uid)
                .orderByDesc(SysNotification::getCreateTime));
    }

    /**
     * 当前用户站内信分页(时间倒序)。供「工作台 → 消息中心」列表页使用,
     * 避免 1.7 万行全量一次返回;支持未读过滤与标题/内容/类型关键字模糊搜索。
     */
    public PageResult<SysNotification> listMinePage(int page, int size, String keyword, Boolean unreadOnly, String bizType) {
        String uid = userService.getCurrent().userId();
        LambdaQueryWrapper<SysNotification> w = new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getUserId, uid)
                .eq(StringUtils.hasText(bizType), SysNotification::getBizType, bizType)
                .eq(Boolean.TRUE.equals(unreadOnly), SysNotification::getIsRead, false);
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            w.and(k -> k.like(SysNotification::getTitle, kw)
                    .or().like(SysNotification::getContent, kw)
                    .or().like(SysNotification::getBizType, kw));
        }
        w.orderByDesc(SysNotification::getCreateTime);
        IPage<SysNotification> ip = notificationMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
    }

    public long unreadCount() {
        String uid = userService.getCurrent().userId();
        return notificationMapper.selectCount(new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getUserId, uid)
                .eq(SysNotification::getIsRead, false));
    }

    public void markRead(String id) {
        SysNotification n = notificationMapper.selectById(id);
        if (n != null) {
            n.setIsRead(true);
            notificationMapper.updateById(n);
        }
    }

    public void markAllRead() {
        String uid = userService.getCurrent().userId();
        notificationMapper.selectList(new LambdaQueryWrapper<SysNotification>()
                        .eq(SysNotification::getUserId, uid)
                        .eq(SysNotification::getIsRead, false))
                .forEach(n -> {
                    n.setIsRead(true);
                    notificationMapper.updateById(n);
                });
    }

    /**
     * 查询当前用户在指定时间戳之后的新通知，供 SSE 增量推送使用。
     * @param since 时间戳(含)，返回 createTime >= since 的通知
     */
    public List<SysNotification> listSince(LocalDateTime since) {
        String uid = userService.getCurrent().userId();
        return notificationMapper.selectList(new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getUserId, uid)
                .ge(since != null, SysNotification::getCreateTime, since)
                .orderByDesc(SysNotification::getCreateTime));
    }

    private void insert(String userId, String title, String content,
                        String bizType, String bizId, String bizNo, String link) {
        SysNotification n = new SysNotification();
        n.setUserId(userId);
        n.setUserName(queryUserName(userId));
        n.setOrgId(queryUserOrg(userId));
        n.setTitle(title);
        n.setContent(content);
        n.setBizType(bizType);
        n.setBizId(bizId);
        n.setBizNo(bizNo);
        n.setLink(link);
        n.setChannel("站内弹窗");
        n.setIsRead(false);
        n.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(n);
        // 站内信主记录落库后, 同步写一条「站内弹窗」投递明细到 ops.notify_message,
        // 使通知中心(查 notify_message)能覆盖每一条站内信通知。
        try {
            // 发送人取当前登录用户(业务操作发起人); 无登录态(定时任务等)时降级为空, 不影响主流程
            String senderId = null;
            String senderName = null;
            try {
                var cur = userService.getCurrent();
                senderId = cur.userId();
                // 优先真实姓名, 取不到回落登录账号
                senderName = queryUserName(cur.userId());
                if (senderName == null) senderName = cur.username();
            } catch (Exception ignored) { /* 无当前用户场景 */ }
            NotifyMessage inbox = new NotifyMessage();
            inbox.setOrgId(n.getOrgId());
            inbox.setSenderId(senderId);
            inbox.setSenderName(senderName);
            inbox.setReceiverId(userId);
            inbox.setReceiverName(n.getUserName());
            inbox.setReceiverType("user");
            inbox.setChannel("站内弹窗");
            inbox.setChannelType("inbox");
            inbox.setTitle(title);
            inbox.setContent(content);
            inbox.setBizType(bizType);
            inbox.setBizId(bizId);
            // 业务调用方传入的可读单据号; 未传则留空, 通知中心回退展示「—」而非 UUID
            inbox.setBizNo(bizNo);
            inbox.setStatus("成功");
            inbox.setSendTime(LocalDateTime.now());
            inbox.setNotificationId(n.getId());
            notifyMessageMapper.insert(inbox);
        } catch (Exception ignored) {
            // 投递明细写失败不影响站内信主流程
        }
        // 发布事件供 SSE 实时推送
        try {
            eventPublisher.publishEvent(new NotificationCreatedEvent(n));
        } catch (Exception ignored) {
            // 事件发布失败不影响主流程
        }
    }

    /**
     * 按角色码解析用户(按 org 隔离): 仅返回归属 org 内、或跨公司(org=null)的管理员。
     * orgId 为 null 时退化为全公司广播(系统公告/定时任务等无业务 org 上下文场景)。
     */
    private List<String> resolveUserIdsByRoles(List<String> roleCodes, String orgId) {
        if (roleCodes == null || roleCodes.isEmpty()) return List.of();
        String placeholders = roleCodes.stream().map(c -> "?").collect(Collectors.joining(","));
        StringBuilder sql = new StringBuilder("SELECT DISTINCT u.id FROM ops.sys_user u "
                + "JOIN ops.sys_user_role ur ON ur.user_id = u.id "
                + "JOIN ops.sys_role r ON r.id = ur.role_id "
                + "WHERE r.role_code IN (" + placeholders + ") AND u.status = '启用' AND u.is_deleted = false");
        List<Object> args = new ArrayList<>(roleCodes);
        if (orgId != null && !orgId.isBlank()) {
            sql.append(" AND (u.org_id = ? OR u.org_id IS NULL)");
            args.add(orgId);
        }
        return jdbcTemplate.query(sql.toString(), (rs, row) -> rs.getString(1), args.toArray());
    }

    /** 取当前登录用户归属 org(跨公司管理员为 null)。无登录态时返回 null(全局广播)。 */
    private String currentOrgId() {
        try {
            return userService.getCurrent().orgId();
        } catch (Exception ignored) {
            return null;
        }
    }

    /** 解析某事件配置的接收角色中文名(用于通知日志 receiver 展示)。 */
    public List<String> resolveRoleNames(String module, String eventCode) {
        List<String> roles = notifyConfigService.resolveRoles(module, eventCode);
        if (roles.isEmpty()) return List.of();
        String placeholders = roles.stream().map(c -> "?").collect(Collectors.joining(","));
        String sql = "SELECT DISTINCT r.role_name FROM ops.sys_role r WHERE r.role_code IN (" + placeholders + ")";
        List<String> names = jdbcTemplate.query(sql, (rs, row) -> rs.getString(1), roles.toArray());
        return names == null ? List.of() : names;
    }

    private String queryUserName(String userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT real_name FROM ops.sys_user WHERE id = ?::uuid", String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }

    /** 取接收者用户的组织,使通知归属与接收者一致(避免被按 org 的数据权限过滤)。 */
    private String queryUserOrg(String userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT org_id FROM ops.sys_user WHERE id = ?::uuid", String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }
}
