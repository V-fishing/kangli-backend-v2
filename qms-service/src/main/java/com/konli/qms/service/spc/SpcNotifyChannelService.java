package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcAlarm;
import com.konli.qms.domain.spc.entity.SpcNotifyChannel;
import com.konli.qms.domain.spc.entity.SpcNotifyRecord;

import java.util.List;

/** SPC 通知渠道(启用/停用)与报警触发时的推送。spc.param.* */
public interface SpcNotifyChannelService {

    List<SpcNotifyChannel> list();

    void toggle(String id, boolean enabled);

    /**
     * 报警触发后,向所有启用(且匹配组织)的渠道推送通知,并落库推送记录(留痕)。
     * 任何渠道投递失败不影响报警本身。
     */
    void send(SpcAlarm alarm);

    /** 查询推送记录:alarmId 为空则返回最近记录(全局留痕) */
    List<SpcNotifyRecord> listRecords(String alarmId);
}
