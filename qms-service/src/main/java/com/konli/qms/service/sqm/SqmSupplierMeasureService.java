package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmSupplierMeasure;

import java.util.List;

/** 供应商改善措施:查询/新增。sqm.abnormal.* */
public interface SqmSupplierMeasureService {

    List<SqmSupplierMeasure> list(String abnormalId);

    SqmSupplierMeasure get(String id);

    SqmSupplierMeasure create(SqmSupplierMeasure measure);
}
