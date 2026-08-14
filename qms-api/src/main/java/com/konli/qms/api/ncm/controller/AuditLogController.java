package com.konli.qms.api.ncm.controller;

import com.konli.qms.api.ncm.dto.AuditLogQuery;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作轨迹查询 — 按 record_id 查询 ops.sys_audit_log,返回该 8D/缺陷报告的全流程留痕。
 * 时间倒序(最新操作在前),供 8dDetail.vue 时间线展示。
 */
@RestController
@RequestMapping("/api/v1/ncm/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final JdbcTemplate jdbc;

    private static final RowMapper<AuditLogVO> ROW_MAPPER = new RowMapper<AuditLogVO>() {
        @Override
        public AuditLogVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AuditLogVO(
                rs.getLong("id"),
                rs.getString("module"),
                rs.getString("action"),
                rs.getString("operator_id"),
                rs.getString("operator_name"),
                rs.getString("record_id"),
                rs.getString("detail"),
                rs.getString("status"),
                rs.getObject("cost_ms") != null ? rs.getInt("cost_ms") : null,
                rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null
            );
        }
    };

    @GetMapping("/log")
    @PreAuthorize("hasAuthority('ncm.8d.list')")
    public R<PageResult<AuditLogVO>> list(AuditLogQuery q) {
        if (q.getRecordId() == null || q.getRecordId().isBlank()) {
            return R.ok(new PageResult<>(List.of(), 0, q.getPage(), q.getSize()));
        }
        StringBuilder where = new StringBuilder("WHERE record_id = ?");
        List<Object> args = new java.util.ArrayList<>();
        args.add(q.getRecordId());
        if (q.getModule() != null && !q.getModule().isBlank()) {
            where.append(" AND module = ?");
            args.add(q.getModule());
        }
        if (q.getAction() != null && !q.getAction().isBlank()) {
            where.append(" AND action = ?");
            args.add(q.getAction());
        }

        Long total = jdbc.queryForObject(
            "SELECT COUNT(*) FROM ops.sys_audit_log " + where, Long.class, args.toArray());
        total = total != null ? total : 0L;

        int page = q.getPage() < 1 ? 1 : q.getPage();
        int size = q.getSize() < 1 ? 50 : q.getSize();
        int offset = (page - 1) * size;

        List<AuditLogVO> records = jdbc.query(
            "SELECT id, module, action, operator_id, operator_name, record_id, detail, status, cost_ms, created_at "
            + "FROM ops.sys_audit_log " + where
            + " ORDER BY created_at DESC LIMIT ? OFFSET ?",
            ROW_MAPPER, append(args, size, offset).toArray());

        return R.ok(new PageResult<>(records, total, page, size));
    }

    /**
     * 全系统审计日志查询 — 独立审计页使用。支持 module/action/operator(姓名或ID模糊)/recordId/状态/时间范围
     * 过滤 + 分页(时间倒序),权限码 system.audit.list。
     *
     * <p>ops.sys_audit_log 表无 org_id 列(全模块全局留痕),故不做行级 org 隔离,仅以权限码控权。</p>
     */
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('system.audit.list')")
    public R<PageResult<AuditLogVO>> listAll(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String recordId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (module != null && !module.isBlank()) {
            where.append(" AND module = ?");
            args.add(module);
        }
        if (action != null && !action.isBlank()) {
            where.append(" AND action = ?");
            args.add(action);
        }
        if (operator != null && !operator.isBlank()) {
            where.append(" AND (operator_name ILIKE ? OR operator_id = ?)");
            args.add("%" + operator + "%");
            args.add(operator);
        }
        if (recordId != null && !recordId.isBlank()) {
            where.append(" AND record_id = ?");
            args.add(recordId);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND status = ?");
            args.add(status);
        }
        if (start != null && !start.isBlank()) {
            where.append(" AND created_at >= ?");
            args.add(java.time.LocalDateTime.parse(start));
        }
        if (end != null && !end.isBlank()) {
            where.append(" AND created_at <= ?");
            args.add(java.time.LocalDateTime.parse(end));
        }

        Long total = jdbc.queryForObject(
            "SELECT COUNT(*) FROM ops.sys_audit_log " + where, Long.class, args.toArray());
        total = total != null ? total : 0L;

        int p = page < 1 ? 1 : page;
        int s = size < 1 ? 20 : size;
        int offset = (p - 1) * s;

        List<AuditLogVO> records = jdbc.query(
            "SELECT id, module, action, operator_id, operator_name, record_id, detail, status, cost_ms, created_at "
            + "FROM ops.sys_audit_log " + where
            + " ORDER BY created_at DESC LIMIT ? OFFSET ?",
            ROW_MAPPER, append(args, s, offset).toArray());

        return R.ok(new PageResult<>(records, total, p, s));
    }

    /** 已记录模块枚举 — 供前端下拉,避免硬编码散落。 */
    @GetMapping("/modules")
    @PreAuthorize("hasAuthority('system.audit.list')")
    public R<List<String>> modules() {
        List<String> mods = jdbc.queryForList(
            "SELECT DISTINCT module FROM ops.sys_audit_log WHERE module IS NOT NULL ORDER BY module",
            String.class);
        return R.ok(mods);
    }

    private List<Object> append(List<Object> base, Object... extra) {
        List<Object> out = new ArrayList<>(base);
        for (Object e : extra) out.add(e);
        return out;
    }

    /** 操作轨迹 VO — 时间线展示所需字段。 */
    public record AuditLogVO(Long id, String module, String action, String operatorId,
                             String operatorName, String recordId, String detail,
                             String status, Integer costMs, LocalDateTime createdAt) {
    }
}
