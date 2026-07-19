package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcParam;

import java.util.List;

/** SPC 参数主数据 CRUD(规格限/子组大小/图表类型)。spc.param.* */
public interface SpcParamService {

    List<SpcParam> list();

    SpcParam get(String id);

    SpcParam create(SpcParam param);

    void update(SpcParam param);

    void delete(String id);
}
