package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.fia.entity.FiaInterceptConfig;
import com.konli.qms.domain.fia.mapper.FiaInterceptConfigMapper;
import com.konli.qms.service.fia.FiaInterceptConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FiaInterceptConfigServiceImpl implements FiaInterceptConfigService {

    private final FiaInterceptConfigMapper fiaInterceptConfigMapper;

    @Override
    public FiaInterceptConfig get(String orgId) {
        if (orgId == null) {
            return defaultConfig(null);
        }
        FiaInterceptConfig c = fiaInterceptConfigMapper.selectOne(
                new LambdaQueryWrapper<FiaInterceptConfig>().eq(FiaInterceptConfig::getOrgId, orgId));
        return c == null ? defaultConfig(orgId) : c;
    }

    @Override
    public void save(FiaInterceptConfig config) {
        FiaInterceptConfig existing = fiaInterceptConfigMapper.selectOne(
                new LambdaQueryWrapper<FiaInterceptConfig>().eq(FiaInterceptConfig::getOrgId, config.getOrgId()));
        if (existing == null) {
            fiaInterceptConfigMapper.insert(config);
        } else {
            config.setId(existing.getId());
            fiaInterceptConfigMapper.updateById(config);
        }
    }

    private FiaInterceptConfig defaultConfig(String orgId) {
        FiaInterceptConfig c = new FiaInterceptConfig();
        c.setOrgId(orgId);
        c.setInterceptMode("硬阻断");
        c.setMultiTriggerMode("合并一张校验单");
        c.setSlaHours(new BigDecimal("2.0"));
        c.setEscalateFailCount(3);
        return c;
    }
}
