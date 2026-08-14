package com.konli.qms.service.sqm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.sqm.entity.SqmSupplier;

import java.util.List;

/** 供应商档案:查询/新增/编辑/删除。sqm.supplier.* */
public interface SqmSupplierService {

    List<SqmSupplier> list();

    PageResult<SqmSupplier> listPage(String keyword, String level, String status, int page, int size);

    SqmSupplier get(String id);

    /** 按 MES 供应商编号(VEN 编号,如 VEN00417)解析供应商(供 MES 对接/重建脚本对齐供应商)。 */
    SqmSupplier findByVenCode(String venCode);

    SqmSupplier create(SqmSupplier supplier);

    void update(SqmSupplier supplier);

    void delete(String id);
}
