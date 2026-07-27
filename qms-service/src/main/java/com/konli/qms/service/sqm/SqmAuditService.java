package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmAuditApproval;
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

    /** 独立事务创建审核计划(联动场景:失败不回滚调用方主事务)。 */
    SqmAuditPlan createPlanInNewTx(SqmAuditPlan plan);

    /** 确认排期：将状态从「计划中」→「待执行」。 */
    void confirmPlan(String id);

    /** 启动审核计划：将状态置为「进行中」。 */
    SqmAuditPlan startPlan(String id);

    SqmAuditPlan getPlan(String id);

    /** 按来源变更单 id 反查其联动生成的审核计划(用于变更单详情回显关联审核)。 */
    List<SqmAuditPlan> listByChangeId(String changeId);

    SqmAuditRecord getRecord(String id);

    /** 审核会签:列出某计划的会签链(不存在则按默认角色惰性初始化)。 */
    List<SqmAuditApproval> listApprovals(String auditId);

    /** 审核会签:某角色进行会签(通过/驳回)。 */
    void approve(String auditId, String approvalRole, boolean approved, String opinion);

    /** 重置某审核类型下所有计划的会签链(配置变更后调用,下次打开详情按新配置重建)。 */
    void resetApprovalsForType(String auditType);

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
