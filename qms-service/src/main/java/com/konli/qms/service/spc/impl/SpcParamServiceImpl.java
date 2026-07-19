package com.konli.qms.service.spc.impl;

import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.service.spc.SpcParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpcParamServiceImpl implements SpcParamService {

    private final SpcParamMapper spcParamMapper;

    @Override
    public List<SpcParam> list() {
        return spcParamMapper.selectList(null);
    }

    @Override
    public SpcParam get(String id) {
        return spcParamMapper.selectById(id);
    }

    @Override
    @Transactional
    public SpcParam create(SpcParam param) {
        if (param.getIsActive() == null) {
            param.setIsActive(true);
        }
        spcParamMapper.insert(param);
        return param;
    }

    @Override
    public void update(SpcParam param) {
        spcParamMapper.updateById(param);
    }

    @Override
    @Transactional
    public void delete(String id) {
        spcParamMapper.deleteById(id);
    }
}
