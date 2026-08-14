package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.spc.entity.SpcAlarm;
import com.konli.qms.domain.spc.mapper.SpcAlarmMapper;
import com.konli.qms.service.spc.SpcAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    public PageResult<SpcAlarm> listPage(String keyword, String status, String level, int page, int size) {
        LambdaQueryWrapper<SpcAlarm> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(k -> k.like(SpcAlarm::getCode, keyword)
                    .or().like(SpcAlarm::getParamName, keyword)
                    .or().like(SpcAlarm::getWoNo, keyword)
                    .or().like(SpcAlarm::getBatchNo, keyword));
        }
        if (StringUtils.hasText(status)) {
            w.eq(SpcAlarm::getStatus, status);
        }
        if (StringUtils.hasText(level)) {
            w.eq(SpcAlarm::getLevel, level);
        }
        w.orderByDesc(SpcAlarm::getAlarmTime);
        IPage<SpcAlarm> ip = spcAlarmMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
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
        // SR-SPC-017:关闭必须填写关闭原因与处置措施,否则阻止关闭
        if (closeReason == null || closeReason.isBlank()) {
            throw new BusinessException(400, "关闭原因不能为空");
        }
        if (disposition == null || disposition.isBlank()) {
            throw new BusinessException(400, "处置措施不能为空");
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
