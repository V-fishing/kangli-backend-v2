package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcControlLimit;

import java.util.List;

/** SPC 控制限(前25子组建基线,Xbar-R 图 CL/UCL/LCL)。spc.param.* */
public interface SpcControlLimitService {

    List<SpcControlLimit> list(String paramId);

    SpcControlLimit getActive(String paramId);

    SpcControlLimit calc(String paramId);
}
