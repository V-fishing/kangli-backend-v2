package com.konli.qms.service.qmsmgmt.impl;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.qmsmgmt.QmsComplianceBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 体系合规监控看板: 跨模块聚合分析。
 * 质量目标达成率 / 内审不符合项关闭率 / 不良事件处理率 来自本模块统计接口;
 * 顾客反馈分析复用 CS 反馈数据(满意度/类型分布/处理率)。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QmsComplianceBoardServiceImpl implements QmsComplianceBoardService {

    private final JdbcTemplate jdbcTemplate;
    private final QmsQualityGoalServiceImpl goalService;
    private final QmsInternalAuditServiceImpl auditService;
    private final QmsAdverseEventServiceImpl adverseService;

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> board() {
        String org = curOrg();
        Map<String, Object> res = new LinkedHashMap<>();
        // 本模块三大块
        res.put("goal", goalService.stats());
        res.put("audit", auditService.stats());
        res.put("adverse", adverseService.stats());

        // 顾客反馈分析(复用 CS 数据) — 参数化查询,避免 org 字符串拼接。
        Map<String, Object> fb = new LinkedHashMap<>();
        try {
            String orgFilter = (org != null) ? " AND org_id = ?" : "";
            Object[] args = (org != null) ? new Object[]{ org } : new Object[0];
            Map<String, Object> agg = jdbcTemplate.queryForMap(
                    "SELECT COUNT(*) AS total, "
                            + "COUNT(*) FILTER (WHERE status = 'DONE') AS done, "
                            + "COALESCE(ROUND(AVG(satisfaction)::numeric, 2), 0) AS avg_score "
                            + "FROM ops.cs_feedback WHERE is_deleted = false" + orgFilter, args);
            long total = ((Number) agg.get("total")).longValue();
            long done = ((Number) agg.get("done")).longValue();
            fb.put("total", total);
            fb.put("done", done);
            fb.put("handleRate", total == 0 ? 0 : Math.round(done * 100.0 / total));
            fb.put("avgScore", agg.get("avg_score"));

            // 类型分布
            java.util.List<Map<String, Object>> typeDist = jdbcTemplate.queryForList(
                    "SELECT fb_type AS type, COUNT(*) AS cnt FROM ops.cs_feedback "
                            + "WHERE is_deleted = false" + orgFilter + " GROUP BY fb_type ORDER BY cnt DESC", args);
            fb.put("typeDist", typeDist);

            // 状态分布
            java.util.List<Map<String, Object>> statusDist = jdbcTemplate.queryForList(
                    "SELECT status, COUNT(*) AS cnt FROM ops.cs_feedback "
                            + "WHERE is_deleted = false" + orgFilter + " GROUP BY status", args);
            fb.put("statusDist", statusDist);
        } catch (Exception e) {
            log.warn("[QMS-MGMT] 顾客反馈分析查询失败: {}", e.getMessage());
            fb.put("total", 0);
            fb.put("done", 0);
            fb.put("handleRate", 0);
            fb.put("avgScore", 0);
            fb.put("typeDist", java.util.Collections.emptyList());
            fb.put("statusDist", java.util.Collections.emptyList());
        }
        res.put("feedback", fb);
        return res;
    }
}
