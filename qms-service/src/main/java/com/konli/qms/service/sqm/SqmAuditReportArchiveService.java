package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmAuditReportArchive;

import java.util.List;

/**
 * 审核报告归档:sqm.audit.* 权限。
 *
 * <p>归档逻辑参考 {@code FiaTaskServiceImpl.generatePdf}:
 * 查 sqm_audit_record + sqm_audit_nc(by recordId),构建 HTML,openhtmltopdf 渲染 PDF,
 * 计算 SHA-256 hash,retentionUntil = now + 15 年,落 sqm_audit_report_archive。</p>
 */
public interface SqmAuditReportArchiveService {

    /** 归档列表(可按 recordId 过滤)。 */
    List<SqmAuditReportArchive> list(String recordId);

    /** 单条归档。 */
    SqmAuditReportArchive get(String id);

    /** 直接创建归档记录(一般不直接用,用 generatePdf 触发)。 */
    SqmAuditReportArchive create(SqmAuditReportArchive archive);

    /**
     * 生成审核报告 PDF 并归档。
     * <p>查 sqm_audit_record + sqm_audit_nc(by recordId),构建 HTML 渲染 PDF,
     * 存 logs/reports/audit-{recordNo}.pdf,计算 SHA-256 hash,
     * retentionUntil = now + 15 年,落 sqm_audit_report_archive,返回归档记录。</p>
     */
    SqmAuditReportArchive generatePdf(String recordId);
}
