package com.konli.qms.service.notify;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.notify.entity.SysNotification;
import com.konli.qms.domain.notify.mapper.SysNotificationMapper;
import com.konli.qms.service.uop.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全平台站内消息通知服务。
 * 支持:按角色解析接收人、按用户推送、当前用户收件箱、未读计数、标记已读。
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SysNotificationMapper notificationMapper;
    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;

    /** 按角色码解析用户并推送(可排除某用户,避免通知发起者本人)。 */
    public void notifyRoles(List<String> roleCodes, String title, String content,
                            String bizType, String bizId, String link, String excludeUserId) {
        for (String uid : resolveUserIdsByRoles(roleCodes)) {
            if (uid != null && !uid.equals(excludeUserId)) {
                insert(uid, title, content, bizType, bizId, link);
            }
        }
    }

    /** 推送给指定用户。 */
    public void notifyUser(String userId, String title, String content,
                           String bizType, String bizId, String link) {
        if (userId == null) return;
        insert(userId, title, content, bizType, bizId, link);
    }

    public List<SysNotification> listMine() {
        String uid = userService.getCurrent().userId();
        return notificationMapper.selectList(new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getUserId, uid)
                .orderByDesc(SysNotification::getCreateTime));
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

    private void insert(String userId, String title, String content,
                        String bizType, String bizId, String link) {
        SysNotification n = new SysNotification();
        n.setUserId(userId);
        n.setUserName(queryUserName(userId));
        n.setOrgId(currentOrgId());
        n.setTitle(title);
        n.setContent(content);
        n.setBizType(bizType);
        n.setBizId(bizId);
        n.setLink(link);
        n.setIsRead(false);
        n.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    private String currentOrgId() {
        try {
            String org = userService.getCurrent().orgId();
            // org_id 为 uuid 列, "ROOT" 等哨兵值或非 uuid 不可入库, 置空
            if (org == null || "ROOT".equals(org) || !org.matches("[0-9a-fA-F-]{8,36}")) {
                return null;
            }
            return org;
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> resolveUserIdsByRoles(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) return List.of();
        String placeholders = roleCodes.stream().map(c -> "?").collect(Collectors.joining(","));
        String sql = "SELECT DISTINCT u.id FROM ops.sys_user u "
                + "JOIN ops.sys_user_role ur ON ur.user_id = u.id "
                + "JOIN ops.sys_role r ON r.id = ur.role_id "
                + "WHERE r.role_code IN (" + placeholders + ") AND u.status = '启用' AND u.is_deleted = false";
        return jdbcTemplate.query(sql, (rs, row) -> rs.getString(1), roleCodes.toArray());
    }

    private String queryUserName(String userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT real_name FROM ops.sys_user WHERE id = ?::uuid", String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }
}
