package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcNotifyChannel;

import java.util.List;

/** SPC 通知渠道(启用/停用)。spc.param.* */
public interface SpcNotifyChannelService {

    List<SpcNotifyChannel> list();

    void toggle(String id, boolean enabled);
}
