package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.NcmFilterScheme;

import java.util.List;

/** 分析方案 CRUD。ncm.record.list */
public interface NcmFilterSchemeService {

    List<NcmFilterScheme> list();

    NcmFilterScheme create(NcmFilterScheme scheme);

    void delete(String id);
}
