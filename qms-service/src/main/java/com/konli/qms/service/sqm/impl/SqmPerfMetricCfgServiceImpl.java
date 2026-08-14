package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmPerfMetricCfg;
import com.konli.qms.domain.sqm.mapper.SqmPerfMetricCfgMapper;
import com.konli.qms.service.sqm.SqmPerfMetricCfgService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmPerfMetricCfgServiceImpl implements SqmPerfMetricCfgService {

    private final SqmPerfMetricCfgMapper mapper;

    @Override
    public List<SqmPerfMetricCfg> list() {
        return mapper.selectList(new LambdaQueryWrapper<SqmPerfMetricCfg>()
                .eq(SqmPerfMetricCfg::getIsDeleted, false)
                .orderByAsc(SqmPerfMetricCfg::getMetricCode));
    }

    @Override
    public SqmPerfMetricCfg get(String id) {
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmPerfMetricCfg save(SqmPerfMetricCfg cfg) {
        if (cfg.getId() == null) {
            mapper.insert(cfg);
        } else {
            if (mapper.selectById(cfg.getId()) == null) {
                throw new BusinessException(404, "指标配置不存在");
            }
            mapper.updateById(cfg);
        }
        return cfg;
    }
}
