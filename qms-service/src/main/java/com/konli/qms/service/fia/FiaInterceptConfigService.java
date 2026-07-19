package com.konli.qms.service.fia;

import com.konli.qms.domain.fia.entity.FiaInterceptConfig;

/** 拦截配置(每公司一行:模式/多触发/SLA/升级) */
public interface FiaInterceptConfigService {

    /** 按 orgId 查拦截配置;无则返回默认(硬阻断/合并一张校验单/2.0/3),不落库 */
    FiaInterceptConfig get(String orgId);

    /** upsert by orgId:有则 updateById,无则 insert */
    void save(FiaInterceptConfig config);
}
