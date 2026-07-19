package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcGlobalConfig;

/** SPC 全局配置(按公司一行,无则返回默认)。spc.param.* */
public interface SpcGlobalConfigService {

    SpcGlobalConfig get(String orgId);

    void save(SpcGlobalConfig config);
}
