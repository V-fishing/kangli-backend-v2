package com.konli.qms.service.notify;

import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.entity.NotifyConfig;

import java.util.List;

/**
 * 统一通知配置读写 + 解析入口。
 * 各业务模块发通知时调用 resolveRoles / resolveChannels 取代硬编码角色与渠道。
 */
public interface NotifyConfigService {

    /** 全量配置(前端配置页用)。 */
    List<NotifyConfig> listAll();

    /** 全量外部渠道(前端渠道多选用)。 */
    List<NotifyChannel> listChannels();

    /** 更新某事件配置的角色、具体接收人与渠道(并清缓存)。 */
    void update(String id, String roleCodes, String receiverIds, String channels, Boolean enabled);

    /** 更新外部渠道(webhook/启停/点对点凭据)。configJson 为 null 表示不修改;secret 类字段传 "****" 视为不修改。 */
    void updateChannel(String id, String webhookUrl, Boolean enabled, String channelType, String configJson);

    /** 解析事件接收角色码; 查不到或禁用返回空列表(不发站内信)。 */
    List<String> resolveRoles(String module, String eventCode);

    /** 解析事件具体接收人用户ID列表(与角色并存取并集, 可为空)。 */
    List<String> resolveReceiverIds(String module, String eventCode);

    /** 解析事件外部渠道名列表; 查不到返回空。 */
    List<String> resolveChannels(String module, String eventCode);
}
