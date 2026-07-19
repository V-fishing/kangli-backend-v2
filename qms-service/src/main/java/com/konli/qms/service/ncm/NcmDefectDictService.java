package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.NcmDefectDict;

import java.util.List;

/** 不良字典 CRUD。ncm.defect.* */
public interface NcmDefectDictService {

    List<NcmDefectDict> list();

    NcmDefectDict get(String id);

    NcmDefectDict create(NcmDefectDict dict);

    void update(NcmDefectDict dict);

    void delete(String id);
}
