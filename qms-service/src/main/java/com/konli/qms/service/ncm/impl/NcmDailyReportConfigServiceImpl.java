package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.ncm.entity.NcmDailyReportConfig;
import com.konli.qms.domain.ncm.mapper.NcmDailyReportConfigMapper;
import com.konli.qms.service.ncm.NcmDailyReportConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NcmDailyReportConfigServiceImpl implements NcmDailyReportConfigService {

    private final NcmDailyReportConfigMapper ncmDailyReportConfigMapper;

    @Override
    public List<NcmDailyReportConfig> list() {
        return ncmDailyReportConfigMapper.selectList(null);
    }

    @Override
    @Transactional
    public NcmDailyReportConfig save(NcmDailyReportConfig config) {
        // upsert by orgId:org_id 为 null 时匹配全局配置(isNull)
        LambdaQueryWrapper<NcmDailyReportConfig> qw = new LambdaQueryWrapper<>();
        if (config.getOrgId() != null) {
            qw.eq(NcmDailyReportConfig::getOrgId, config.getOrgId());
        } else {
            qw.isNull(NcmDailyReportConfig::getOrgId);
        }
        NcmDailyReportConfig existing = ncmDailyReportConfigMapper.selectOne(qw);
        if (existing == null) {
            if (config.getEnabled() == null) {
                config.setEnabled(true);
            }
            ncmDailyReportConfigMapper.insert(config);
            return config;
        }
        // 更新已有记录
        existing.setPushTime(config.getPushTime());
        existing.setReceivers(config.getReceivers());
        if (config.getEnabled() != null) {
            existing.setEnabled(config.getEnabled());
        }
        ncmDailyReportConfigMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void toggle(String id, Boolean enabled) {
        NcmDailyReportConfig config = ncmDailyReportConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(404, "日报配置不存在");
        }
        NcmDailyReportConfig upd = new NcmDailyReportConfig();
        upd.setId(id);
        upd.setEnabled(enabled);
        ncmDailyReportConfigMapper.updateById(upd);
    }
}
