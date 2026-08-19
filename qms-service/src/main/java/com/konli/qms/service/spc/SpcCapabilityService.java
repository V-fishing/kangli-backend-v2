package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcCapability;
import com.konli.qms.service.spc.dto.SpcParamCpkVo;

import java.util.List;

/** SPC 过程能力指数计算(Cp/Cpk/Pp/Ppk 等,按周期聚合)。spc.capability.* */
public interface SpcCapabilityService {

    List<SpcCapability> list();

    SpcCapability calc(String paramId, String periodType, String periodValue);

    /** 能力趋势:取最近 months 个周期(按 periodValue 倒序取,返回时按时间正序)。 */
    List<SpcCapability> trend(String paramId, int months);

    /** 看板"跨参数 CPK 对比":以参数维度聚合 CPK(优先取落库值,与概览/趋势同口径)。 */
    List<SpcParamCpkVo> getParamCpk();
}
