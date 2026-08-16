package com.konli.qms.service.qmsmgmt;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.qmsmgmt.entity.QmsAuditNc;
import com.konli.qms.domain.qmsmgmt.entity.QmsInternalAudit;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/** 内审数据管理(计划 + 不符合项)。qms-mgmt.audit.* */
public interface QmsInternalAuditService {

    PageResult<QmsInternalAudit> page(String keyword, String status, int page, int size);

    QmsInternalAudit get(String id);

    QmsInternalAudit create(QmsInternalAudit audit);

    QmsInternalAudit update(QmsInternalAudit audit);

    void delete(String id);

    /** 推进内审状态: PLANNED->ONGOING->DONE->CLOSED。 */
    void advance(String id, String status);

    /** 不符合项分页(按 auditId 过滤)。 */
    PageResult<QmsAuditNc> ncPage(String auditId, String status, int page, int size);

    QmsAuditNc getNc(String id);

    QmsAuditNc saveNc(QmsAuditNc nc);

    void deleteNc(String id);

    Map<String, Object> stats();

    /** 导出内审计划(含不符合项概览)为 CSV(GBK, 带 BOM)。 */
    void exportCsv(HttpServletResponse response, String keyword, String status);
}
