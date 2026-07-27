package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmAuditApprovalCfg;
import com.konli.qms.service.sqm.dto.AuditorDef;

import java.util.List;

/** 审核会签配置服务:按审核类型维护默认会签人员与否决权。 */
public interface SqmAuditApprovalCfgService {
    /** 列出全部类型的会签配置。 */
    List<SqmAuditApprovalCfg> listAll();

    /** 按审核类型取配置(不存在返回 null)。 */
    SqmAuditApprovalCfg getByType(String auditType);

    /** 保存某审核类型的会签配置(upsert)。 */
    SqmAuditApprovalCfg save(String auditType, List<AuditorDef> auditors);

    /** 解析某审核类型的会签人员;无配置返回 null。 */
    List<AuditorDef> resolve(String auditType);
}
