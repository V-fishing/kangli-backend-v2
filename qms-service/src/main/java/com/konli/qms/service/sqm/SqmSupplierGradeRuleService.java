package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmSupplierGradeRule;

import java.util.List;

/** 供应商评级规则:查询/新增/编辑/删除。sqm.supplier.* */
public interface SqmSupplierGradeRuleService {

    List<SqmSupplierGradeRule> list();

    SqmSupplierGradeRule create(SqmSupplierGradeRule rule);

    void update(SqmSupplierGradeRule rule);

    void delete(String id);
}
