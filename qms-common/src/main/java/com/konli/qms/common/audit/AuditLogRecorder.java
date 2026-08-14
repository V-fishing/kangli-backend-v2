package com.konli.qms.common.audit;

import com.konli.qms.common.security.CompanyContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 审计日志落库器 — 单点写 ops.sys_audit_log,供 {@link AuditAspect}(Controller 层 @Auditable)
 * 与业务 Service(如 8D 自动归档/作废)共用,保证落库语义一致(失败不阻断)。
 *
 * <p>record_id / operator_id 均为 VARCHAR(64)(见 V37),不再做 ::uuid 强转,避免非 UUID 业务
 * 主键(如单号)写入时被 PostgreSQL 拒收导致审计静默丢失。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogRecorder {

    private final JdbcTemplate jdbc;

    /**
     * 写一条审计记录。
     *
     * @param module     模块,如 NCM/FIA/SPC
     * @param action     动作,如 CREATE/ADVANCE/APPROVE/REOPEN/ARCHIVE
     * @param method     触发方法签名(短串),可为 null
     * @param recordId   被操作记录 ID(UUID 或业务单号),可为 null
     * @param detail     人类可读摘要,可为 null
     * @param status     SUCCESS / FAIL
     * @param error      失败时的错误信息,可为 null
     * @param costMs     耗时(毫秒)
     */
    public void record(String module, String action, String method,
                       String recordId, String detail, String status, String error, long costMs) {
        String operatorName = currentOperatorName();
        String operatorId = currentOperatorId();
        try {
            jdbc.update(
                "INSERT INTO ops.sys_audit_log (module, action, method, operator_id, operator_name, "
                + "record_id, detail, status, error, cost_ms, created_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                module, action, method, operatorId, operatorName,
                recordId, detail, status, error, costMs, LocalDateTime.now());
        } catch (Exception ex) {
            log.warn("[AUDIT] 落库失败(不阻断业务): {}", ex.getMessage());
        }
        log.info("[AUDIT] {} | {} | {} | {} | {} | {}ms | {}",
                module, action, status, operatorId, operatorName, costMs, method);
    }

    private String currentOperatorName() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u != null ? u.username() : "anonymous";
    }

    private String currentOperatorId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u != null ? u.userId() : null;
    }
}
