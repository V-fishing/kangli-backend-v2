package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcCapability;
import com.konli.qms.service.spc.dto.SpcSupplierCpkVo;

import java.util.List;

/** SPC 过程能力指数计算(Cp/Cpk/Pp/Ppk 等,按周期聚合)。spc.capability.* */
public interface SpcCapabilityService {

    List<SpcCapability> list();

    SpcCapability calc(String paramId, String periodType, String periodValue);

    /** 能力趋势:取最近 months 个周期(按 periodValue 倒序取,返回时按时间正序)。 */
    List<SpcCapability> trend(String paramId, int months);

    /** 看板"跨参数 CPK 对比":对每个参数实时计算 CPK(无供应商维度)。 */
    List<SpcSupplierCpkVo> getSupplierCpk();
}
