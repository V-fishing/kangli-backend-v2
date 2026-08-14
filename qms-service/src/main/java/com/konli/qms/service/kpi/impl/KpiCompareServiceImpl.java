package com.konli.qms.service.kpi.impl;

import com.konli.qms.service.kpi.KpiCompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分公司 KPI 对比实现。
 *
 * <p>直接按 org_id 分别聚合各模块核心指标(绕过当前用户上下文/数据权限,实现跨公司并列对比),
 * 指标口径与现有看板一致:
 * <ul>
 *   <li>8D 闭环率 = status='已闭环' / 总数</li>
 *   <li>CAPA 关闭率 = status='已关闭' / 总数</li>
 *   <li>FIA 合格率 = overall_judge='合格' / status='已完成'</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class KpiCompareServiceImpl implements KpiCompareService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> compare() {
        List<Map<String, Object>> rawOrgs = jdbcTemplate.queryForList(
                "SELECT id::text AS orgId, org_code AS orgCode, org_name AS orgName "
                        + "FROM ops.sys_org WHERE org_code IN ('MZ','SZ') ORDER BY org_code");

        // 显式映射为驼峰字段:PostgreSQL 未加双引号的别名会被折叠为小写,而 JdbcTemplate 的
        // LinkedCaseInsensitiveMap 序列化时输出原始(小写)键,导致前端按 orgId/orgCode/orgName 取不到值。
        List<Map<String, Object>> orgs = new ArrayList<>();
        for (Map<String, Object> raw : rawOrgs) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("orgId", raw.get("orgId"));
            o.put("orgCode", raw.get("orgCode"));
            o.put("orgName", raw.get("orgName"));
            orgs.add(o);
        }

        // 每个分公司(MZ/SZ)的原始指标值
        Map<String, Map<String, Object>> perOrg = new LinkedHashMap<>();
        for (Map<String, Object> org : orgs) {
            String orgId = String.valueOf(org.get("orgId"));
            perOrg.put(String.valueOf(org.get("orgCode")), metricsForOrg(orgId));
        }

        // 指标元数据: key / 展示名 / 类型(count 整数 / rate 百分比) / 对应原始值字段
        List<Map<String, String>> meta = new ArrayList<>();
        meta.add(meta("8d.total", "8D 总数", "count", "8dTotal"));
        meta.add(meta("8d.closedRate", "8D 闭环率(%)", "rate", "8dClosedRate"));
        meta.add(meta("capa.total", "CAPA 总数", "count", "capaTotal"));
        meta.add(meta("capa.closedRate", "CAPA 关闭率(%)", "rate", "capaClosedRate"));
        meta.add(meta("defect.total", "不良记录数", "count", "defectTotal"));
        meta.add(meta("spc.alarm.total", "SPC 异常告警数", "count", "spcAlarmTotal"));
        meta.add(meta("spc.alarm.pending", "SPC 待处理告警", "count", "spcAlarmPending"));
        meta.add(meta("fia.task.total", "FIA 任务数", "count", "fiaTaskTotal"));
        meta.add(meta("fia.passRate", "FIA 合格率(%)", "rate", "fiaPassRate"));
        meta.add(meta("patrol.task.total", "巡检任务数", "count", "patrolTaskTotal"));
        meta.add(meta("patrol.abnormal.total", "巡检异常数", "count", "patrolAbnormalTotal"));

        // 转置:每个指标一行,values 按 orgCode 排列,便于前端图表并列对比
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, String> m : meta) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", m.get("key"));
            row.put("name", m.get("name"));
            row.put("type", m.get("type"));
            Map<String, Object> values = new LinkedHashMap<>();
            for (String code : perOrg.keySet()) {
                values.put(code, perOrg.get(code).get(m.get("sourceKey")));
            }
            row.put("values", values);
            items.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgs", orgs);
        result.put("items", items);
        return result;
    }

    private Map<String, Object> metricsForOrg(String orgId) {
        long d8Total = cnt("ops.qms_8d_report", orgId);
        long d8Closed = cnt("ops.qms_8d_report", orgId, "status", "已闭环");
        long capaTotal = cnt("ops.qms_capa", orgId);
        long capaClosed = cnt("ops.qms_capa", orgId, "status", "已关闭");
        long defectTotal = cnt("ops.ncm_defect_record", orgId);
        long spcAlarmTotal = cnt("ops.spc_alarm", orgId);
        long spcAlarmPending = cnt("ops.spc_alarm", orgId, "status", "待确认");
        long fiaTaskTotal = cnt("ops.fia_task", orgId);
        long fiaCompleted = cnt("ops.fia_task", orgId, "status", "已完成");
        long fiaQualified = cnt("ops.fia_task", orgId, "overall_judge", "合格");
        long patrolTaskTotal = cnt("ops.patl_task", orgId);
        long patrolAbnormalTotal = cnt("ops.patl_abnormal", orgId);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("8dTotal", d8Total);
        m.put("8dClosedRate", rate(d8Closed, d8Total));
        m.put("capaTotal", capaTotal);
        m.put("capaClosedRate", rate(capaClosed, capaTotal));
        m.put("defectTotal", defectTotal);
        m.put("spcAlarmTotal", spcAlarmTotal);
        m.put("spcAlarmPending", spcAlarmPending);
        m.put("fiaTaskTotal", fiaTaskTotal);
        m.put("fiaPassRate", rate(fiaQualified, fiaCompleted));
        m.put("patrolTaskTotal", patrolTaskTotal);
        m.put("patrolAbnormalTotal", patrolAbnormalTotal);
        return m;
    }

    private long cnt(String table, String orgId) {
        return cnt(table, orgId, null, null);
    }

    private long cnt(String table, String orgId, String col, String val) {
        String sql = "SELECT count(*) FROM " + table
                + " WHERE is_deleted = false AND org_id = ?"
                + (col != null ? " AND " + col + " = ?" : "");
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        if (col != null) {
            args.add(val);
        }
        try {
            Long r = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
            return r == null ? 0L : r;
        } catch (Exception e) {
            return 0L;
        }
    }

    private double rate(long num, long den) {
        if (den <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(num).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(den), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Map<String, String> meta(String key, String name, String type, String sourceKey) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("key", key);
        m.put("name", name);
        m.put("type", type);
        m.put("sourceKey", sourceKey);
        return m;
    }
}
