package com.konli.qms.service.tlm;

import com.konli.qms.domain.tlm.entity.TlmToolVersion;

import java.util.List;

/** 工装版本变更履历(留痕)。tlm.tooling.* */
public interface TlmToolVersionService {

    /** 按工装查询版本记录, 按变更日期倒序。 */
    List<TlmToolVersion> listByTool(String toolId);

    /** 新增版本记录: 自动补 org_id, 版本号按工装自动递增(V1, V2, V3...)。 */
    TlmToolVersion create(TlmToolVersion version);
}
