package com.konli.qms.service.notify;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.entity.NotifyMessage;

import java.util.List;

/** 通知中心: 手动发起点对点通知 + 发送记录查询。 */
public interface NotifyMessageService {

    /**
     * 手动发起通知: 向多个接收人按渠道点对点发送(异步), 返回创建的发送记录数。
     */
    int send(String title, String content, List<String> receiverIds,
             List<String> channels, String bizType, String bizId, String bizNo);

    /** 分页查询发送记录(按状态/渠道/关键字筛选)。 */
    PageResult<NotifyMessage> list(String status, String channel, String keyword, int page, int size);

    /**
     * 通知中心统一视图: 以「通知」为粒度聚合(同一 notification_id 的多条投递明细归并为一行),
     * 每行含全部渠道投递结果(deliveries)。按notification_id(或自身id)分组后分页。
     */
    PageResult<NotifyCenterRow> centerPage(String status, String channel, String keyword, int page, int size);

    /** 点对点(direct)类型渠道列表(凭据已脱敏, 供通知中心发送时选择)。 */
    List<NotifyChannel> listDirectChannels();
}
