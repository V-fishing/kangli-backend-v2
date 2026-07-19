package com.konli.qms.service.fia;

import com.konli.qms.domain.fia.entity.FiaSignConfig;

public interface SignConfigService {

    /** 按 org 取签名配置;无则返回默认({password}/两级/整单/3·5min),不落库 */
    FiaSignConfig get(String orgId);

    void save(FiaSignConfig config);
}
