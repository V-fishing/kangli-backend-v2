package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.spc.entity.SpcProcess;
import com.konli.qms.domain.spc.mapper.SpcProcessMapper;
import com.konli.qms.service.spc.SpcProcessService;
import com.konli.qms.service.support.OrgIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpcProcessServiceImpl implements SpcProcessService {

    private final SpcProcessMapper spcProcessMapper;
    private final OrgIdResolver orgIdResolver;

    @Override
    public List<SpcProcess> list() {
        // 按排序升序展示,与参数分组顺序一致
        return spcProcessMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<SpcProcess>lambdaQuery()
                        .orderByAsc(SpcProcess::getSortNo, SpcProcess::getProcessName));
    }

    @Override
    public PageResult<SpcProcess> listPage(String keyword, int page, int size) {
        LambdaQueryWrapper<SpcProcess> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(k -> k.like(SpcProcess::getProcessName, keyword)
                    .or().like(SpcProcess::getProcessCode, keyword));
        }
        w.orderByAsc(SpcProcess::getSortNo, SpcProcess::getProcessName);
        IPage<SpcProcess> ip = spcProcessMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
    }

    @Override
    public SpcProcess get(String id) {
        return spcProcessMapper.selectById(id);
    }

    @Override
    @Transactional
    public SpcProcess create(SpcProcess process) {
        process.setOrgId(orgIdResolver.resolve(process.getOrgId()));
        if (process.getIsActive() == null) {
            process.setIsActive(true);
        }
        if (process.getSortNo() == null) {
            process.setSortNo(0);
        }
        spcProcessMapper.insert(process);
        return process;
    }

    @Override
    @Transactional
    public void update(SpcProcess process) {
        if (!StringUtils.hasText(process.getOrgId())) {
            SpcProcess existing = spcProcessMapper.selectById(process.getId());
            if (existing != null) {
                process.setOrgId(existing.getOrgId());
            }
        } else {
            process.setOrgId(orgIdResolver.resolve(process.getOrgId()));
        }
        spcProcessMapper.updateById(process);
    }

    @Override
    @Transactional
    public void delete(String id) {
        spcProcessMapper.deleteById(id);
    }
}
