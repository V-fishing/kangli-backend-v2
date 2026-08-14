package com.konli.qms.service.spc;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.spc.entity.SpcProcess;

import java.util.List;

/** SPC 工序主数据 CRUD(参数的父级分组)。spc.process.* */
public interface SpcProcessService {

    List<SpcProcess> list();

    PageResult<SpcProcess> listPage(String keyword, int page, int size);

    SpcProcess get(String id);

    SpcProcess create(SpcProcess process);

    void update(SpcProcess process);

    void delete(String id);
}
