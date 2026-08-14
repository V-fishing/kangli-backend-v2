package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.spc.entity.SpcSpecStandard;
import com.konli.qms.domain.spc.mapper.SpcSpecStandardMapper;
import com.konli.qms.service.spc.SpcSpecStandardService;
import com.konli.qms.service.support.OrgIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpcSpecStandardServiceImpl implements SpcSpecStandardService {

    private final SpcSpecStandardMapper spcSpecStandardMapper;
    private final OrgIdResolver orgIdResolver;

    @Override
    public List<SpcSpecStandard> list() {
        return spcSpecStandardMapper.selectList(null);
    }

    @Override
    public PageResult<SpcSpecStandard> listPage(String keyword, int page, int size) {
        LambdaQueryWrapper<SpcSpecStandard> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(k -> k.like(SpcSpecStandard::getMaterial, keyword)
                    .or().like(SpcSpecStandard::getProcName, keyword));
        }
        w.orderByDesc(SpcSpecStandard::getUpdatedAt);
        IPage<SpcSpecStandard> ip = spcSpecStandardMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
    }

    @Override
    public SpcSpecStandard get(String id) {
        return spcSpecStandardMapper.selectById(id);
    }

    @Override
    @Transactional
    public SpcSpecStandard create(SpcSpecStandard standard) {
        standard.setOrgId(orgIdResolver.resolve(standard.getOrgId()));
        spcSpecStandardMapper.insert(standard);
        return standard;
    }

    @Override
    @Transactional
    public void update(SpcSpecStandard standard) {
        spcSpecStandardMapper.updateById(standard);
    }

    @Override
    @Transactional
    public void delete(String id) {
        spcSpecStandardMapper.deleteById(id);
    }
}
