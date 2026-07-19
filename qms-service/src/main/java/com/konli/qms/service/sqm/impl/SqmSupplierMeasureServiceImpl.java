package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.sqm.entity.SqmSupplierMeasure;
import com.konli.qms.domain.sqm.mapper.SqmSupplierMeasureMapper;
import com.konli.qms.service.sqm.SqmSupplierMeasureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmSupplierMeasureServiceImpl implements SqmSupplierMeasureService {

    private final SqmSupplierMeasureMapper sqmSupplierMeasureMapper;

    @Override
    public List<SqmSupplierMeasure> list(String abnormalId) {
        LambdaQueryWrapper<SqmSupplierMeasure> w = new LambdaQueryWrapper<>();
        if (abnormalId != null && !abnormalId.isBlank()) {
            w.eq(SqmSupplierMeasure::getAbnormalId, abnormalId);
        }
        w.orderByDesc(SqmSupplierMeasure::getSubmitDate);
        return sqmSupplierMeasureMapper.selectList(w);
    }

    @Override
    public SqmSupplierMeasure get(String id) {
        return sqmSupplierMeasureMapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmSupplierMeasure create(SqmSupplierMeasure measure) {
        measure.setMeasureId("SM-" + System.currentTimeMillis());
        sqmSupplierMeasureMapper.insert(measure);
        return measure;
    }
}
