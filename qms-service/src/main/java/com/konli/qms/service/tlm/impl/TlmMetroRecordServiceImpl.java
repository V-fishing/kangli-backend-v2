package com.konli.qms.service.tlm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.tlm.entity.TlmMetroRecord;
import com.konli.qms.domain.tlm.entity.TlmTooling;
import com.konli.qms.domain.tlm.mapper.TlmMetroRecordMapper;
import com.konli.qms.domain.tlm.mapper.TlmToolingMapper;
import com.konli.qms.service.tlm.TlmMetroRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 计量数据采集记录服务实现(P2)。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TlmMetroRecordServiceImpl implements TlmMetroRecordService {

    private final TlmMetroRecordMapper metroRecordMapper;
    private final TlmToolingMapper toolingMapper;

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
        } catch (Exception e) {
            return null;
        }
    }

    private String curUser() {
        try {
            return CompanyContext.get().userId();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public PageResult<TlmMetroRecord> page(String keyword, String woNo, String judged, int page, int size) {
        LambdaQueryWrapper<TlmMetroRecord> w = new LambdaQueryWrapper<>();
        String org = curOrg();
        if (org != null) w.eq(TlmMetroRecord::getOrgId, org);
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(TlmMetroRecord::getToolNo, keyword).or().like(TlmMetroRecord::getToolName, keyword));
        }
        if (woNo != null && !woNo.isBlank()) w.like(TlmMetroRecord::getWoNo, woNo);
        if (judged != null && !judged.isBlank()) w.eq(TlmMetroRecord::getJudged, judged);
        w.orderByDesc(TlmMetroRecord::getMeasureTime);
        IPage<TlmMetroRecord> ip = metroRecordMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TlmMetroRecord create(TlmMetroRecord record) {
        if (record.getToolId() == null) {
            throw new com.konli.qms.common.exception.BusinessException("请选择计量器具");
        }
        TlmTooling t = toolingMapper.selectById(record.getToolId());
        if (t == null) {
            throw new com.konli.qms.common.exception.BusinessException("计量器具不存在");
        }
        if (!"GAUGE".equals(t.getToolCategory())) {
            throw new com.konli.qms.common.exception.BusinessException("仅计量器具(GAUGE)可录入采集数据");
        }
        if (record.getJudged() == null || record.getJudged().isBlank()) record.setJudged("合格");
        if (record.getMeasureTime() == null) record.setMeasureTime(LocalDateTime.now());
        record.setOrgId(t.getOrgId());
        record.setToolNo(t.getToolNo());
        record.setToolName(t.getToolName());
        record.setCreatedBy(curUser());
        metroRecordMapper.insert(record);
        return record;
    }
}
