package com.konli.qms.service.spc.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.spc.entity.SpcAlarm;
import com.konli.qms.domain.spc.mapper.SpcAlarmMapper;
import com.konli.qms.service.spc.SpcAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpcAlarmServiceImpl implements SpcAlarmService {

    private final SpcAlarmMapper spcAlarmMapper;

    @Override
    public List<SpcAlarm> list() {
        return spcAlarmMapper.selectList(null);
    }

    @Override
    public void close(String id, String closeReason, String disposition) {
        SpcAlarm alarm = spcAlarmMapper.selectById(id);
        if (alarm == null) {
            throw new BusinessException(400, "告警不存在");
        }
        if (!"待确认".equals(alarm.getStatus())) {
            throw new BusinessException(400, "告警状态不允许关闭(需为待确认)");
        }
        alarm.setCloseReason(closeReason);
        alarm.setDisposition(disposition);
        alarm.setClosedBy(currentOperator());
        alarm.setClosedAt(LocalDateTime.now());
        alarm.setStatus("已关闭");
        spcAlarmMapper.updateById(alarm);
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }
}
