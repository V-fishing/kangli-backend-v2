package com.konli.qms.service.uop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.uop.entity.SysDict;
import com.konli.qms.domain.uop.mapper.SysDictMapper;
import com.konli.qms.service.uop.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictMapper sysDictMapper;

    @Override
    public List<SysDict> listByType(String type) {
        return sysDictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, type)
                .eq(SysDict::getEnabled, true)
                .orderByAsc(SysDict::getSortOrder));
    }

    @Override
    public List<SysDict> listAll() {
        return sysDictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getEnabled, true)
                .orderByAsc(SysDict::getDictType)
                .orderByAsc(SysDict::getSortOrder));
    }
}
