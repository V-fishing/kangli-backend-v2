package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcAlarm;

import java.util.List;

/** SPC 告警查询与处置(关闭需状态为待确认)。spc.alarm.* */
public interface SpcAlarmService {

    List<SpcAlarm> list();

    void close(String id, String closeReason, String disposition);
}
