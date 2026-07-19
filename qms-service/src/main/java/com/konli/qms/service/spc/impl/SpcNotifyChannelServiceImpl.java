package com.konli.qms.service.spc.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcNotifyChannel;
import com.konli.qms.domain.spc.mapper.SpcNotifyChannelMapper;
import com.konli.qms.service.spc.SpcNotifyChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpcNotifyChannelServiceImpl implements SpcNotifyChannelService {

    private final SpcNotifyChannelMapper spcNotifyChannelMapper;

    @Override
    public List<SpcNotifyChannel> list() {
        return spcNotifyChannelMapper.selectList(null);
    }

    @Override
    public void toggle(String id, boolean enabled) {
        SpcNotifyChannel channel = spcNotifyChannelMapper.selectById(id);
        if (channel == null) {
            throw new BusinessException(400, "通知渠道不存在");
        }
        channel.setIsEnabled(enabled);
        spcNotifyChannelMapper.updateById(channel);
    }
}
