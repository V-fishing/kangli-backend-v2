package com.konli.qms.service.ncm.impl;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.ncm.entity.NcmFilterScheme;
import com.konli.qms.domain.ncm.mapper.NcmFilterSchemeMapper;
import com.konli.qms.service.ncm.NcmFilterSchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NcmFilterSchemeServiceImpl implements NcmFilterSchemeService {

    private final NcmFilterSchemeMapper ncmFilterSchemeMapper;

    @Override
    public List<NcmFilterScheme> list() {
        return ncmFilterSchemeMapper.selectList(null);
    }

    @Override
    @Transactional
    public NcmFilterScheme create(NcmFilterScheme scheme) {
        if (scheme.getOwnerId() == null) {
            scheme.setOwnerId(currentOperator());
        }
        ncmFilterSchemeMapper.insert(scheme);
        return scheme;
    }

    @Override
    @Transactional
    public void delete(String id) {
        ncmFilterSchemeMapper.deleteById(id);
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }
}
