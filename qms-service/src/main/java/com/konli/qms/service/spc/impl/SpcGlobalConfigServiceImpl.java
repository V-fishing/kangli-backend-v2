package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.spc.entity.SpcGlobalConfig;
import com.konli.qms.domain.spc.mapper.SpcGlobalConfigMapper;
import com.konli.qms.service.spc.SpcGlobalConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SpcGlobalConfigServiceImpl implements SpcGlobalConfigService {

    private final SpcGlobalConfigMapper spcGlobalConfigMapper;

    @Override
    public SpcGlobalConfig get(String orgId) {
        if (orgId == null || orgId.isBlank()) {
            return defaultConfig(null);
        }
        SpcGlobalConfig c = spcGlobalConfigMapper.selectOne(
                new LambdaQueryWrapper<SpcGlobalConfig>().eq(SpcGlobalConfig::getOrgId, orgId));
        return c == null ? defaultConfig(orgId) : c;
    }

    @Override
    @Transactional
    public void save(SpcGlobalConfig config) {
        SpcGlobalConfig existing = (config.getOrgId() == null || config.getOrgId().isBlank())
                ? spcGlobalConfigMapper.selectOne(
                        new LambdaQueryWrapper<SpcGlobalConfig>().isNull(SpcGlobalConfig::getOrgId))
                : spcGlobalConfigMapper.selectOne(
                        new LambdaQueryWrapper<SpcGlobalConfig>().eq(SpcGlobalConfig::getOrgId, config.getOrgId()));
        if (existing == null) {
            spcGlobalConfigMapper.insert(config);
        } else {
            config.setId(existing.getId());
            spcGlobalConfigMapper.updateById(config);
        }
    }

    /** 默认配置(无记录时返回,不入库)。 */
    private SpcGlobalConfig defaultConfig(String orgId) {
        SpcGlobalConfig c = new SpcGlobalConfig();
        c.setOrgId(orgId);
        c.setBaselineMode("前25子组动态");
        c.setDefaultSubgroupSize(5);
        c.setChartAutoRules("①,④,⑤");
        c.setCpkPeriod("month");
        c.setCpkSufficient(new BigDecimal("1.33"));
        c.setCpkAcceptable(new BigDecimal("1.00"));
        c.setSpecSource("检验标准库");
        c.setAlertLevel("提醒");
        c.setSuppressMinutes(30);
        return c;
    }
}
