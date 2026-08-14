package com.konli.qms.service.tlm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.tlm.entity.TlmMaintPlan;
import com.konli.qms.domain.tlm.entity.TlmMaintRecord;

import java.util.List;

/** 工装保养: 计划 + 记录。tlm.maint.* */
public interface TlmMaintService {

    PageResult<TlmMaintPlan> planPage(String toolId, int page, int size);

    TlmMaintPlan createPlan(TlmMaintPlan plan);

    TlmMaintPlan updatePlan(TlmMaintPlan plan);

    void deletePlan(String id);

    List<TlmMaintRecord> recordList(String toolId);

    TlmMaintRecord createRecord(TlmMaintRecord record);
}
