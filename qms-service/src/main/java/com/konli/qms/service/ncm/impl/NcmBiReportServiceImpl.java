package com.konli.qms.service.ncm.impl;

import com.konli.qms.domain.ncm.entity.NcmBiReport;
import com.konli.qms.domain.ncm.mapper.NcmBiReportMapper;
import com.konli.qms.service.ncm.NcmBiReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NcmBiReportServiceImpl implements NcmBiReportService {

    private final NcmBiReportMapper ncmBiReportMapper;

    @Override
    public List<NcmBiReport> list() {
        return ncmBiReportMapper.selectList(null);
    }

    @Override
    public NcmBiReport get(String id) {
        return ncmBiReportMapper.selectById(id);
    }

    @Override
    @Transactional
    public NcmBiReport create(NcmBiReport report) {
        report.setReportNo("BI-" + System.currentTimeMillis());
        report.setStatus("生成中");
        report.setGeneratedAt(LocalDateTime.now());
        ncmBiReportMapper.insert(report);
        return report;
    }
}
