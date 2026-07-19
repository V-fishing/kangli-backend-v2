package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.fia.mapper.FiaTaskMapper;
import com.konli.qms.service.fia.FiaDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FIA 看板:聚合 ops.fia_task 统计(今日任务/完成数、合格率、超时数、状态分布、近7天趋势)。
 *
 * <p>简单计数走 {@link FiaTaskMapper}(MyBatis-Plus 自动软删除过滤 + DataScopeInterceptor 自动 org_id RLS);
 * 分组/趋势走 {@link JdbcTemplate}(不走拦截器,手工拼接 org_id 过滤,对齐 NcmDefectRecordServiceImpl 语义)。</p>
 *
 * <p>状态值用中文:待检/进行中/待复核/已完成/超时/已作废;overall_judge:合格/警告/不合格。</p>
 */
@Service
@RequiredArgsConstructor
public class FiaDashboardServiceImpl implements FiaDashboardService {

    private final FiaTaskMapper fiaTaskMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> dashboard() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();

        // 1. 今日任务数: created_at today
        long todayCount = fiaTaskMapper.selectCount(
                new LambdaQueryWrapper<FiaTask>()
                        .ge(FiaTask::getCreatedAt, startOfToday)
                        .lt(FiaTask::getCreatedAt, startOfTomorrow));

        // 2. 今日完成数: status='已完成' and submitted_at today
        long todayCompleted = fiaTaskMapper.selectCount(
                new LambdaQueryWrapper<FiaTask>()
                        .eq(FiaTask::getStatus, "已完成")
                        .ge(FiaTask::getSubmittedAt, startOfToday)
                        .lt(FiaTask::getSubmittedAt, startOfTomorrow));

        // 3. 合格率: count(overall_judge='合格') / count(status='已完成')
        long qualifiedCount = fiaTaskMapper.selectCount(
                new LambdaQueryWrapper<FiaTask>().eq(FiaTask::getOverallJudge, "合格"));
        long completedCount = fiaTaskMapper.selectCount(
                new LambdaQueryWrapper<FiaTask>().eq(FiaTask::getStatus, "已完成"));
        BigDecimal passRate = completedCount > 0
                ? BigDecimal.valueOf(qualifiedCount).divide(BigDecimal.valueOf(completedCount), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 4. 超时数: is_overdue=true and status!='已完成' and status!='已作废'
        long overdueCount = fiaTaskMapper.selectCount(
                new LambdaQueryWrapper<FiaTask>()
                        .eq(FiaTask::getIsOverdue, true)
                        .ne(FiaTask::getStatus, "已完成")
                        .ne(FiaTask::getStatus, "已作废"));

        // 5. 状态分布(待检/进行中/待复核/已完成 ...): group by status
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put("待检", 0L);
        statusDistribution.put("进行中", 0L);
        statusDistribution.put("待复核", 0L);
        statusDistribution.put("已完成", 0L);
        String distSql = "SELECT status, COUNT(*) AS cnt FROM ops.fia_task WHERE is_deleted = false "
                + orgFilter() + "GROUP BY status";
        for (Map<String, Object> row : jdbcTemplate.queryForList(distSql)) {
            String st = row.get("status") == null ? "未知" : row.get("status").toString();
            statusDistribution.merge(st, toLong(row.get("cnt")), Long::sum);
        }

        // 6. 近7天趋势: 每天的 create count + complete count
        LocalDateTime rangeStart = LocalDate.now().minusDays(6).atStartOfDay();
        List<Map<String, Object>> trend7d = buildTrend(rangeStart, startOfTomorrow);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayCount", todayCount);
        result.put("todayCompleted", todayCompleted);
        result.put("qualifiedCount", qualifiedCount);
        result.put("completedCount", completedCount);
        result.put("passRate", passRate);
        result.put("overdueCount", overdueCount);
        result.put("statusDistribution", statusDistribution);
        result.put("trend7d", trend7d);
        return result;
    }

    /** 近7天(含今日)每天创建数(按 created_at)+ 完成数(按 submitted_at 且 status='已完成')。 */
    private List<Map<String, Object>> buildTrend(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        String createSql = "SELECT to_char(date_trunc('day', created_at), 'YYYY-MM-DD') AS d, COUNT(*) AS cnt "
                + "FROM ops.fia_task WHERE is_deleted = false "
                + "AND created_at >= ? AND created_at < ? "
                + orgFilter()
                + "GROUP BY date_trunc('day', created_at)";
        Map<String, Long> createByDay = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(createSql,
                Timestamp.valueOf(rangeStart), Timestamp.valueOf(rangeEnd))) {
            createByDay.put(String.valueOf(row.get("d")), toLong(row.get("cnt")));
        }

        String completeSql = "SELECT to_char(date_trunc('day', submitted_at), 'YYYY-MM-DD') AS d, COUNT(*) AS cnt "
                + "FROM ops.fia_task WHERE is_deleted = false "
                + "AND status = '已完成' AND submitted_at >= ? AND submitted_at < ? "
                + orgFilter()
                + "GROUP BY date_trunc('day', submitted_at)";
        Map<String, Long> completeByDay = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(completeSql,
                Timestamp.valueOf(rangeStart), Timestamp.valueOf(rangeEnd))) {
            completeByDay.put(String.valueOf(row.get("d")), toLong(row.get("cnt")));
        }

        List<Map<String, Object>> trend = new ArrayList<>(7);
        for (int i = 6; i >= 0; i--) {
            String d = LocalDate.now().minusDays(i).toString(); // YYYY-MM-DD,与 to_char 输出一致
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", d);
            item.put("createCount", createByDay.getOrDefault(d, 0L));
            item.put("completeCount", completeByDay.getOrDefault(d, 0L));
            trend.add(item);
        }
        return trend;
    }

    private long toLong(Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(o.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 应用级 org_id 过滤(对齐 DataScopeInterceptor 语义,JdbcTemplate 不走 MyBatis 拦截器,需手工拼接)。
     * orgId 来自签名 JWT(可信),defensively escape。
     */
    private String orgFilter() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null || CompanyContext.isAdmin()) {
            return "";
        }
        String orgId = u.orgId();
        if (orgId == null || orgId.isBlank()) {
            return "";
        }
        String safe = orgId.replace("'", "''");
        return " AND org_id = '" + safe + "' ";
    }
}
