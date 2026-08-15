package com.konli.qms.service.cs.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.cs.entity.CsWorkOrder;
import com.konli.qms.domain.cs.mapper.CsWorkOrderMapper;
import com.konli.qms.service.cs.CsWorkOrderService;
import com.konli.qms.service.notify.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsWorkOrderServiceImpl implements CsWorkOrderService {

    private final CsWorkOrderMapper mapper;
    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
        } catch (Exception e) {
            return null;
        }
    }

    /** ROOT/未切换组织时,兜底取 sys_org 中第一个有效组织,确保 org_id 外键成立。 */
    private String defaultOrgId() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM ops.sys_org WHERE is_deleted = false ORDER BY created_at LIMIT 1", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String curUser() {
        try {
            return CompanyContext.get().userId();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public PageResult<CsWorkOrder> page(String keyword, String woType, String status,
                                        String priority, int page, int size) {
        LambdaQueryWrapper<CsWorkOrder> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(CsWorkOrder::getOrderNo, keyword)
                    .or().like(CsWorkOrder::getCustomerName, keyword)
                    .or().like(CsWorkOrder::getProductName, keyword));
        }
        if (woType != null && !woType.isBlank()) w.eq(CsWorkOrder::getWoType, woType);
        if (status != null && !status.isBlank()) w.eq(CsWorkOrder::getStatus, status);
        if (priority != null && !priority.isBlank()) w.eq(CsWorkOrder::getPriority, priority);
        w.orderByDesc(CsWorkOrder::getCreatedAt);
        IPage<CsWorkOrder> p = mapper.selectPage(new Page<>(page, size), w);
        return new PageResult<CsWorkOrder>(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public CsWorkOrder get(String id) {
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public CsWorkOrder create(CsWorkOrder order) {
        if (order.getOrgId() == null) {
            String o = curOrg();
            order.setOrgId(o != null ? o : defaultOrgId());
        }
        if (order.getStatus() == null || order.getStatus().isBlank()) order.setStatus("PENDING");
        if (order.getWoType() == null || order.getWoType().isBlank()) order.setWoType("REPAIR");
        if (order.getPriority() == null || order.getPriority().isBlank()) order.setPriority("NORMAL");
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            order.setOrderNo("WO-" + System.currentTimeMillis());
        }
        String u = curUser();
        order.setCreatedBy(u);
        order.setUpdatedBy(u);
        mapper.insert(order);
        try {
            notificationService.notify("cs", "cs_wo_created", "售后新工单待处理",
                    "新工单 " + order.getOrderNo() + " 已创建,客户:" + order.getCustomerName()
                            + ",请及时派单处理。", "cs_work_order", order.getId(), "/cs/work-orders");
        } catch (Exception e) {
            log.warn("[CS] 新工单通知发送失败: {}", e.getMessage());
        }
        return order;
    }

    @Override
    @Transactional
    public CsWorkOrder update(CsWorkOrder order) {
        CsWorkOrder exist = mapper.selectById(order.getId());
        if (exist == null) throw new com.konli.qms.common.exception.BusinessException("工单不存在");
        // 仅允许编辑基础信息(已派单/完成后不回退状态)
        exist.setCustomerName(order.getCustomerName());
        exist.setCustomerContact(order.getCustomerContact());
        exist.setWoType(order.getWoType());
        exist.setPriority(order.getPriority());
        exist.setProductName(order.getProductName());
        exist.setFaultDesc(order.getFaultDesc());
        exist.setExpectTime(order.getExpectTime());
        exist.setAddress(order.getAddress());
        exist.setUpdatedBy(curUser());
        mapper.updateById(exist);
        return exist;
    }

    @Override
    @Transactional
    public void delete(String id) {
        mapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assign(String id, String responsibleId, String responsibleName) {
        CsWorkOrder o = mapper.selectById(id);
        if (o == null) throw new com.konli.qms.common.exception.BusinessException("工单不存在");
        if (!"PENDING".equals(o.getStatus())) {
            throw new com.konli.qms.common.exception.BusinessException("仅待派单工单可派单");
        }
        o.setStatus("ASSIGNED");
        o.setOwnerId(responsibleId);
        o.setOwnerName(responsibleName);
        o.setAssignAt(LocalDateTime.now());
        o.setUpdatedBy(curUser());
        mapper.updateById(o);
        try {
            notificationService.notifyUser(responsibleId, "售后工单已指派",
                    "工单 " + o.getOrderNo() + " 已指派给您(" + responsibleName
                            + "),客户:" + o.getCustomerName() + ",请及时处理。",
                    "cs_work_order", o.getId(), "/cs/work-orders");
        } catch (Exception e) {
            log.warn("[CS] 工单指派通知发送失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void complete(String id, String handleDetail) {
        CsWorkOrder o = mapper.selectById(id);
        if (o == null) throw new com.konli.qms.common.exception.BusinessException("工单不存在");
        if (!"ASSIGNED".equals(o.getStatus())) {
            throw new com.konli.qms.common.exception.BusinessException("仅处理中工单可标记完成");
        }
        o.setStatus("DONE");
        o.setHandleDetail(handleDetail);
        o.setHandleAt(LocalDateTime.now());
        o.setUpdatedBy(curUser());
        mapper.updateById(o);
    }

    @Override
    @Transactional
    public void close(String id, Integer satisfaction, String satisfactionComment) {
        CsWorkOrder o = mapper.selectById(id);
        if (o == null) throw new com.konli.qms.common.exception.BusinessException("工单不存在");
        if (!"DONE".equals(o.getStatus())) {
            throw new com.konli.qms.common.exception.BusinessException("仅已完成工单可评价闭环");
        }
        o.setStatus("CLOSED");
        o.setSatisfaction(satisfaction);
        o.setSatisfactionComment(satisfactionComment);
        o.setCloseAt(LocalDateTime.now());
        o.setUpdatedBy(curUser());
        mapper.updateById(o);
    }

    /**
     * SR-CS: 每天 8:05 扫描售后工单超时预警。
     * 条件: status IN ('PENDING','ASSIGNED') 且 expect_time 非空且 < now() (超过期望时间仍未闭环)。
     * 去重: 同一工单当天已发过 cs_wo_overdue 则跳过, 避免每日重复刷屏。
     * 接收人: 走 notify 配置(sqe 角色); 若已指派(ASSIGNED)额外点对点推送给负责人。
     */
    @Scheduled(cron = "0 5 8 * * ?")
    public void scanOverdue() {
        try {
            List<CsWorkOrder> overdue = mapper.selectList(new LambdaQueryWrapper<CsWorkOrder>()
                    .in(CsWorkOrder::getStatus, "PENDING", "ASSIGNED")
                    .isNotNull(CsWorkOrder::getExpectTime)
                    .lt(CsWorkOrder::getExpectTime, LocalDateTime.now()));
            if (overdue.isEmpty()) return;
            String today = java.time.LocalDate.now().toString();
            for (CsWorkOrder o : overdue) {
                // 当天去重: 同 biz_id + 标题 + 当天已存在则跳过
                Integer cnt = jdbcTemplate.queryForObject(
                        "SELECT COUNT(1) FROM ops.sys_notification WHERE biz_id = ? AND title = '售后工单超时预警' "
                                + "AND to_char(created_at, 'YYYY-MM-DD') = ?",
                        Integer.class, o.getId(), today);
                if (cnt != null && cnt > 0) continue;
                String detail = "工单 " + o.getOrderNo() + " 客户:" + o.getCustomerName()
                        + " 已超过期望时间仍未闭环(当前状态:" + statusText(o.getStatus()) + "),请尽快处理。";
                notificationService.notify("cs", "cs_wo_overdue", "售后工单超时预警", detail,
                        "cs_work_order", o.getId(), "/cs/work-orders");
                if ("ASSIGNED".equals(o.getStatus()) && o.getOwnerId() != null) {
                    notificationService.notifyUser(o.getOwnerId(), "售后工单超时预警", detail,
                            "cs_work_order", o.getId(), "/cs/work-orders");
                }
            }
            log.info("[CS] 超时工单扫描完成, 命中 {} 条", overdue.size());
        } catch (Exception e) {
            log.warn("[CS] 超时工单扫描异常: {}", e.getMessage());
        }
    }

    private String statusText(String s) {
        return switch (s) {
            case "PENDING" -> "待派单";
            case "ASSIGNED" -> "处理中";
            case "DONE" -> "已完成";
            case "CLOSED" -> "已闭环";
            default -> s;
        };
    }

    @Override
    public Map<String, Object> dashboard() {
        String org = curOrg();
        LambdaQueryWrapper<CsWorkOrder> w = new LambdaQueryWrapper<>();
        if (org != null) w.eq(CsWorkOrder::getOrgId, org);
        List<CsWorkOrder> all = mapper.selectList(w);
        Map<String, Object> stat = new LinkedHashMap<>();
        long pending = 0, assigned = 0, done = 0, closed = 0, urgentPending = 0;
        for (CsWorkOrder o : all) {
            switch (o.getStatus()) {
                case "PENDING": pending++; if ("URGENT".equals(o.getPriority())) urgentPending++; break;
                case "ASSIGNED": assigned++; break;
                case "DONE": done++; break;
                case "CLOSED": closed++; break;
            }
        }
        stat.put("pending", pending);
        stat.put("assigned", assigned);
        stat.put("done", done);
        stat.put("closed", closed);
        stat.put("urgentPending", urgentPending);
        stat.put("total", pending + assigned + done + closed);

        // 月度趋势(最近 12 个月新建/完成工单量, 需求 2.4.1.3 服务记录趋势分析)
        try {
            List<Map<String, Object>> monthly = jdbcTemplate.queryForList(
                "SELECT TO_CHAR(created_at, 'YYYY-MM') AS month, " +
                "COUNT(*) AS created, " +
                "COUNT(*) FILTER (WHERE status = 'CLOSED') AS closed " +
                "FROM ops.cs_work_order WHERE is_deleted = false" + csOrgCond() +
                " AND created_at >= NOW() - INTERVAL '12 months' " +
                "GROUP BY TO_CHAR(created_at, 'YYYY-MM') ORDER BY month");
            stat.put("monthly", monthly);
        } catch (Exception e) {
            log.warn("[CS] 工单趋势查询失败: {}", e.getMessage());
            stat.put("monthly", java.util.Collections.emptyList());
        }
        return stat;
    }

    /** 组织过滤拼接(复用 dashboard 的 org 解析, 避免重复代码)。 */
    private String csOrgCond() {
        String org = curOrg();
        return (org != null) ? " AND org_id = '" + org + "'" : "";
    }


    @Override
    public Map<String, Object> satisfactionStats() {
        String org = curOrg();
        String orgCond = (org != null) ? " AND org_id = '" + org + "'" : "";
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            // 平均分 / 已评数
            Map<String, Object> agg = jdbcTemplate.queryForMap(
                "SELECT COALESCE(ROUND(AVG(satisfaction)::numeric, 2), 0) AS avg_score, " +
                "COUNT(*) AS rated FROM ops.cs_work_order " +
                "WHERE is_deleted = false AND status = 'CLOSED' AND satisfaction IS NOT NULL" + orgCond);
            res.put("avgScore", agg.get("avg_score"));
            res.put("rated", ((Number) agg.get("rated")).longValue());

            // 评分分布(1~5)
            List<Map<String, Object>> dist = jdbcTemplate.queryForList(
                "SELECT satisfaction AS score, COUNT(*) AS cnt FROM ops.cs_work_order " +
                "WHERE is_deleted = false AND status = 'CLOSED' AND satisfaction IS NOT NULL" + orgCond +
                " GROUP BY satisfaction ORDER BY satisfaction");
            Map<Integer, Long> distMap = new LinkedHashMap<>();
            for (int i = 1; i <= 5; i++) distMap.put(i, 0L);
            for (Map<String, Object> d : dist) {
                int score = ((Number) d.get("score")).intValue();
                distMap.put(score, ((Number) d.get("cnt")).longValue());
            }
            res.put("distribution", distMap);

            // 月度趋势(最近 12 个月)
            List<Map<String, Object>> monthly = jdbcTemplate.queryForList(
                "SELECT TO_CHAR(close_at, 'YYYY-MM') AS month, " +
                "COALESCE(ROUND(AVG(satisfaction)::numeric, 2), 0) AS avg_score, COUNT(*) AS cnt " +
                "FROM ops.cs_work_order WHERE is_deleted = false AND status = 'CLOSED' " +
                "AND satisfaction IS NOT NULL" + orgCond + " " +
                "AND close_at >= NOW() - INTERVAL '12 months' " +
                "GROUP BY TO_CHAR(close_at, 'YYYY-MM') ORDER BY month");
            res.put("monthly", monthly);

            // 客户反馈评分(需求 2.4.2.1 数据源补全): 聚合 cs_feedback.satisfaction 1~5
            Map<String, Object> fbAgg = jdbcTemplate.queryForMap(
                "SELECT COALESCE(ROUND(AVG(satisfaction)::numeric, 2), 0) AS avg_score, " +
                "COUNT(*) AS rated FROM ops.cs_feedback " +
                "WHERE is_deleted = false AND satisfaction IS NOT NULL" + orgCond);
            res.put("fbAvgScore", fbAgg.get("avg_score"));
            res.put("fbRated", ((Number) fbAgg.get("rated")).longValue());

            // 低分诱因维度统计(需求 2.4.2.2): 按 cause 分组计数
            List<Map<String, Object>> causeDist = jdbcTemplate.queryForList(
                "SELECT cause AS cause, COUNT(*) AS cnt FROM ops.cs_feedback " +
                "WHERE is_deleted = false AND cause IS NOT NULL" + orgCond +
                " GROUP BY cause ORDER BY cnt DESC");
            Map<String, Long> causeMap = new LinkedHashMap<>();
            for (Map<String, Object> c : causeDist) {
                String cause = c.get("cause") != null ? c.get("cause").toString() : "OTHER";
                causeMap.put(cause, ((Number) c.get("cnt")).longValue());
            }
            res.put("causeDist", causeMap);
        } catch (Exception e) {
            log.warn("[CS] 满意度统计查询失败: {}", e.getMessage());
            res.put("avgScore", 0);
            res.put("rated", 0L);
            Map<Integer, Long> distMap = new LinkedHashMap<>();
            for (int i = 1; i <= 5; i++) distMap.put(i, 0L);
            res.put("distribution", distMap);
            res.put("monthly", java.util.Collections.emptyList());
        }
        return res;
    }
}
