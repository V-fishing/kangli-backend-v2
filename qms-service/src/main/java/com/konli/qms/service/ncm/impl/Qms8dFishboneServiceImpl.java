package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.ncm.entity.Qms8dFishbone;
import com.konli.qms.domain.ncm.mapper.Qms8dFishboneMapper;
import com.konli.qms.service.ncm.Qms8dFishboneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Qms8dFishboneServiceImpl implements Qms8dFishboneService {

    private final Qms8dFishboneMapper qms8dFishboneMapper;

    @Override
    public List<Qms8dFishbone> list(String d8Id) {
        return qms8dFishboneMapper.selectList(
                new LambdaQueryWrapper<Qms8dFishbone>()
                        .eq(Qms8dFishbone::getD8Id, d8Id)
                        .orderByAsc(Qms8dFishbone::getSortOrder));
    }

    @Override
    public long count(String d8Id) {
        return qms8dFishboneMapper.selectCount(
                new LambdaQueryWrapper<Qms8dFishbone>()
                        .eq(Qms8dFishbone::getD8Id, d8Id));
    }

    @Override
    @Transactional
    public Qms8dFishbone create(Qms8dFishbone fishbone) {
        qms8dFishboneMapper.insert(fishbone);
        return fishbone;
    }

    @Override
    public void update(Qms8dFishbone fishbone) {
        qms8dFishboneMapper.updateById(fishbone);
    }

    @Override
    @Transactional
    public void delete(String id) {
        qms8dFishboneMapper.deleteById(id);
    }
}
