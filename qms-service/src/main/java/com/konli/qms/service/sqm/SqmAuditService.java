package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmAuditNc;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.SqmAuditRecord;

import java.util.List;

/** 供应商审核:计划/记录/不符合项。sqm.audit.* */
public interface SqmAuditService {

    List<SqmAuditPlan> listPlans();

    List<SqmAuditRecord> listRecords();

    List<SqmAuditNc> listNcs();

    SqmAuditPlan createPlan(SqmAuditPlan plan);

    SqmAuditRecord createRecord(SqmAuditRecord record);

    SqmAuditNc createNc(SqmAuditNc nc);

    /** 关闭不符合项(填写验证结论)。 */
    void closeNc(String ncId, String verifyResult, String verifyComment);

    /**
     * 生成审核报告 PDF(openhtmltopdf)。
     * 查 sqm_audit_record + sqm_audit_nc(by recordId),构建 HTML 渲染为 PDF,
     * 返回 byte[](Controller 可直接输出为下载)。
     */
    byte[] generateReport(String recordId);
}
