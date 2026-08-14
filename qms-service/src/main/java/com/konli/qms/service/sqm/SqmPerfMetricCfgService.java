package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmPerfMetricCfg;

import java.util.List;

/** 供应商绩效指标配置:读写指标权重/阈值。 */
public interface SqmPerfMetricCfgService {

    List<SqmPerfMetricCfg> list();

    SqmPerfMetricCfg get(String id);

    SqmPerfMetricCfg save(SqmPerfMetricCfg cfg);
}
