package com.konli.qms.service.spc;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.spc.entity.SpcSpecStandard;

import java.util.List;

/** SPC 标准线管理 CRUD。spc.spec.standard.* */
public interface SpcSpecStandardService {

    List<SpcSpecStandard> list();

    PageResult<SpcSpecStandard> listPage(String keyword, int page, int size);

    SpcSpecStandard get(String id);

    SpcSpecStandard create(SpcSpecStandard standard);

    void update(SpcSpecStandard standard);

    void delete(String id);
}
