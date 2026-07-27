package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.fia.entity.FiaInterceptConfig;
import com.konli.qms.domain.fia.mapper.FiaInterceptConfigMapper;
import com.konli.qms.service.fia.FiaInterceptConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FiaInterceptConfigServiceImpl implements FiaInterceptConfigService {

    private final FiaInterceptConfigMapper fiaInterceptConfigMapper;

    @Override
    public FiaInterceptConfig get(String orgId) {
        if (orgId == null) {
            return defaultConfig(null);
        }
        // 超级管理员(orgId=ROOT)按全局配置(org_id IS NULL)处理，避免把 ROOT 当作 uuid 查询
        if (isRoot(orgId)) {
            List<FiaInterceptConfig> list = fiaInterceptConfigMapper.selectList(
                    new LambdaQueryWrapper<FiaInterceptConfig>().isNull(FiaInterceptConfig::getOrgId));
            return list.isEmpty() ? defaultConfig(null) : list.get(0);
        }
        FiaInterceptConfig c = fiaInterceptConfigMapper.selectOne(
                new LambdaQueryWrapper<FiaInterceptConfig>().eq(FiaInterceptConfig::getOrgId, orgId));
        return c == null ? defaultConfig(orgId) : c;
    }

    @Override
    public void save(FiaInterceptConfig config) {
        boolean root = isRoot(config.getOrgId());
        LambdaQueryWrapper<FiaInterceptConfig> w = new LambdaQueryWrapper<>();
        if (root || config.getOrgId() == null) {
            w.isNull(FiaInterceptConfig::getOrgId);
        } else {
            w.eq(FiaInterceptConfig::getOrgId, config.getOrgId());
        }
        FiaInterceptConfig existing = fiaInterceptConfigMapper.selectOne(w);
        if (existing == null) {
            if (root || config.getOrgId() == null) config.setOrgId(null);
            fiaInterceptConfigMapper.insert(config);
        } else {
            config.setId(existing.getId());
            config.setOrgId(existing.getOrgId());
            fiaInterceptConfigMapper.updateById(config);
        }
    }

    private boolean isRoot(String orgId) {
        return orgId != null && "ROOT".equals(orgId);
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
