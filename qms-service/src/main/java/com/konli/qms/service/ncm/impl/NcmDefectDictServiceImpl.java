package com.konli.qms.service.ncm.impl;

import com.konli.qms.domain.ncm.entity.NcmDefectDict;
import com.konli.qms.domain.ncm.mapper.NcmDefectDictMapper;
import com.konli.qms.service.ncm.NcmDefectDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NcmDefectDictServiceImpl implements NcmDefectDictService {

    private final NcmDefectDictMapper ncmDefectDictMapper;

    @Override
    public List<NcmDefectDict> list() {
        return ncmDefectDictMapper.selectList(null);
    }

    @Override
    public NcmDefectDict get(String id) {
        return ncmDefectDictMapper.selectById(id);
    }

    @Override
    @Transactional
    public NcmDefectDict create(NcmDefectDict dict) {
        if (dict.getStatus() == null) {
            dict.setStatus("启用");
        }
        if (dict.getReferenceCount() == null) {
            dict.setReferenceCount(0);
        }
        ncmDefectDictMapper.insert(dict);
        return dict;
    }

    @Override
    public void update(NcmDefectDict dict) {
        ncmDefectDictMapper.updateById(dict);
    }

    @Override
    @Transactional
    public void delete(String id) {
        ncmDefectDictMapper.deleteById(id);
    }
}
