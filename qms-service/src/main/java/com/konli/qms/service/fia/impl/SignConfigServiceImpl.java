package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.fia.entity.FiaSignConfig;
import com.konli.qms.domain.fia.mapper.FiaSignConfigMapper;
import com.konli.qms.service.fia.SignConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignConfigServiceImpl implements SignConfigService {

    private final FiaSignConfigMapper fiaSignConfigMapper;

    @Override
    public FiaSignConfig get(String orgId) {
        if (orgId == null) {
            return defaultConfig(null);
        }
        FiaSignConfig c = fiaSignConfigMapper.selectOne(
                new LambdaQueryWrapper<FiaSignConfig>().eq(FiaSignConfig::getOrgId, orgId));
        return c == null ? defaultConfig(orgId) : c;
    }

    @Override
    public void save(FiaSignConfig config) {
        FiaSignConfig existing = fiaSignConfigMapper.selectOne(
                new LambdaQueryWrapper<FiaSignConfig>().eq(FiaSignConfig::getOrgId, config.getOrgId()));
        if (existing == null) {
            fiaSignConfigMapper.insert(config);
        } else {
            config.setId(existing.getId());
            fiaSignConfigMapper.updateById(config);
        }
    }

    private FiaSignConfig defaultConfig(String orgId) {
        FiaSignConfig c = new FiaSignConfig();
        c.setOrgId(orgId);
        c.setSignMethods(new String[]{"password"});
        c.setSignNodes("两级");
        c.setSignGranularity("整单签名");
        c.setLockAfterFail(3);
        c.setLockMinutes(5);
        return c;
    }
}
