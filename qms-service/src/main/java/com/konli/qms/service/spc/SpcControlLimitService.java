package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcControlLimit;

import java.util.List;

/** SPC 控制限(前25子组建基线,Xbar-R 图 CL/UCL/LCL)。spc.param.* */
public interface SpcControlLimitService {

    List<SpcControlLimit> list(String paramId);

    SpcControlLimit getActive(String paramId);

    SpcControlLimit calc(String paramId);

    /** 人工覆盖:写入 manual=true 的基线并置为 active,旧基线置为非 active。优先于自动计算。 */
    SpcControlLimit saveManual(String paramId, SpcControlLimit limits);
}
