package com.konli.qms.service.qmsmgmt.impl;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.notify.NotificationService;
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
 * 健康度评分: 四维度加权(目标达成率 30% / 内审 NC 关闭率 30% / 不良事件处理率 20% / 反馈处理率 20%),
 * 低于阈值(默认 80)触发预警推送(需求 2.7.5)。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QmsComplianceBoardServiceImpl implements QmsComplianceBoardService {

    private final JdbcTemplate jdbcTemplate;
    private final QmsQualityGoalServiceImpl goalService;
    private final QmsInternalAuditServiceImpl auditService;
    private final QmsAdverseEventServiceImpl adverseService;
    private final NotificationService notificationService;

    /** 健康度预警阈值: 综合得分低于该值推送预警。 */
    private static final double HEALTH_WARN_THRESHOLD = 80.0;

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
                            + "COALESCE(ROUND(AVG(satisfaction)::numeric, 2), 0) AS avg_score, "
                            + "COUNT(*) FILTER (WHERE satisfaction IS NOT NULL) AS rated, "
                            + "COUNT(*) FILTER (WHERE satisfaction IS NOT NULL AND satisfaction >= 4) AS reach "
                            + "FROM ops.cs_feedback WHERE is_deleted = false" + orgFilter, args);
            long total = ((Number) agg.get("total")).longValue();
            long done = ((Number) agg.get("done")).longValue();
            long rated = ((Number) agg.get("rated")).longValue();
            long reach = ((Number) agg.get("reach")).longValue();
            fb.put("total", total);
            fb.put("done", done);
            fb.put("handleRate", total == 0 ? 0 : Math.round(done * 100.0 / total));
            fb.put("avgScore", agg.get("avg_score"));
            // 满意度达标率(需求 2.4.2.4: 满意度数据纳入质量管理考核体系): 已评反馈中评分>=4 占比
            fb.put("rated", rated);
            fb.put("satisfactionReachRate", rated == 0 ? 0 : Math.round(reach * 100.0 / rated));

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

        // 健康度综合评分(需求 2.7.5 + 2.4.2.4): 五维度加权
        // 目标达成率 25% / 内审 NC 关闭率 25% / 不良事件处理率 20% / 反馈处理率 15% / 满意度达标率 15%
        double goalRate = toDouble(((Map<?, ?>) res.get("goal")).get("overallRate"));
        double ncRate = toDouble(((Map<?, ?>) res.get("audit")).get("ncCloseRate"));
        double adverseRate = toDouble(((Map<?, ?>) res.get("adverse")).get("processRate"));
        double fbRate = toDouble(fb.get("handleRate"));
        double satReach = toDouble(fb.get("satisfactionReachRate"));
        double health = goalRate * 0.25 + ncRate * 0.25 + adverseRate * 0.20 + fbRate * 0.15 + satReach * 0.15;
        res.put("healthScore", Math.round(health * 100.0) / 100.0);
        res.put("healthLevel", health >= 90 ? "优" : health >= HEALTH_WARN_THRESHOLD ? "良" : "预警");

        // 健康度预警推送(低于阈值时)
        if (health < HEALTH_WARN_THRESHOLD) {
            try {
                notificationService.notify("qms-mgmt", "qms_health_warn", "体系健康度预警",
                        String.format("当前体系健康度得分 %.1f,低于阈值 %.0f,请关注目标达成/内审闭环/不良事件/客户反馈。",
                                health, HEALTH_WARN_THRESHOLD),
                        "qms_compliance_board", null, "/qms-mgmt/board");
            } catch (Exception e) {
                log.warn("[QMS-MGMT] 健康度预警推送失败: {}", e.getMessage());
            }
        }
        return res;
    }

    private double toDouble(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0.0; }
    }
}
