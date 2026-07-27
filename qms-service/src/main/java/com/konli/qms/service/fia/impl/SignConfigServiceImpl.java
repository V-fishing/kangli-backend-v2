package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.fia.entity.FiaSignConfig;
import com.konli.qms.domain.fia.mapper.FiaSignConfigMapper;
import com.konli.qms.service.fia.SignConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignConfigServiceImpl implements SignConfigService {

    private final FiaSignConfigMapper fiaSignConfigMapper;

    @Override
    public FiaSignConfig get(String orgId) {
        if (orgId == null) {
            return defaultConfig(null);
        }
        // 超级管理员(orgId=ROOT)按全局配置(org_id IS NULL)处理，避免把 ROOT 当作 uuid 查询
        if (isRoot(orgId)) {
            List<FiaSignConfig> list = fiaSignConfigMapper.selectList(
                    new LambdaQueryWrapper<FiaSignConfig>().isNull(FiaSignConfig::getOrgId));
            return list.isEmpty() ? defaultConfig(null) : list.get(0);
        }
        FiaSignConfig c = fiaSignConfigMapper.selectOne(
                new LambdaQueryWrapper<FiaSignConfig>().eq(FiaSignConfig::getOrgId, orgId));
        return c == null ? defaultConfig(orgId) : c;
    }

    @Override
    public void save(FiaSignConfig config) {
        boolean root = isRoot(config.getOrgId());
        LambdaQueryWrapper<FiaSignConfig> w = new LambdaQueryWrapper<>();
        if (root || config.getOrgId() == null) {
            w.isNull(FiaSignConfig::getOrgId);
        } else {
            w.eq(FiaSignConfig::getOrgId, config.getOrgId());
        }
        FiaSignConfig existing = fiaSignConfigMapper.selectOne(w);
        if (existing == null) {
            if (root || config.getOrgId() == null) config.setOrgId(null);
            fiaSignConfigMapper.insert(config);
        } else {
            config.setId(existing.getId());
            config.setOrgId(existing.getOrgId());
            fiaSignConfigMapper.updateById(config);
        }
    }

    private boolean isRoot(String orgId) {
        return orgId != null && "ROOT".equals(orgId);
    }

    private FiaSignConfig defaultConfig(String orgId) {
        FiaSignConfig c = new FiaSignConfig();
        c.setOrgId(orgId);
        // 默认支持「用户名+密码(默认) / 手写笔迹 / CA证书」三种, 由会签人在前端按需选择
        c.setSignMethods(new String[]{"password", "handwriting", "ca"});
        c.setSignNodes("两级");
        c.setSignGranularity("整单签名");
        c.setLockAfterFail(3);
        c.setLockMinutes(5);
        return c;
    }
}
