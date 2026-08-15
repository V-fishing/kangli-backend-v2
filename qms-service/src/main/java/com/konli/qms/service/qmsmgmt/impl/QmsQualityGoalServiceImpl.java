package com.konli.qms.service.qmsmgmt.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.qmsmgmt.entity.QmsQualityGoal;
import com.konli.qms.domain.qmsmgmt.mapper.QmsQualityGoalMapper;
import com.konli.qms.service.qmsmgmt.QmsQualityGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QmsQualityGoalServiceImpl implements QmsQualityGoalService {

    private final QmsQualityGoalMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
        } catch (Exception e) {
            return null;
        }
    }

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
    public PageResult<QmsQualityGoal> page(String keyword, String goalType, String period, int page, int size) {
        LambdaQueryWrapper<QmsQualityGoal> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(QmsQualityGoal::getGoalName, keyword)
                    .or().like(QmsQualityGoal::getOwner, keyword));
        }
        if (goalType != null && !goalType.isBlank()) w.eq(QmsQualityGoal::getGoalType, goalType);
        if (period != null && !period.isBlank()) w.eq(QmsQualityGoal::getPeriod, period);
        w.orderByDesc(QmsQualityGoal::getCreatedAt);
        IPage<QmsQualityGoal> p = mapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public QmsQualityGoal get(String id) {
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public QmsQualityGoal create(QmsQualityGoal goal) {
        if (goal.getOrgId() == null) {
            String o = curOrg();
            goal.setOrgId(o != null ? o : defaultOrgId());
        }
        if (goal.getGoalType() == null || goal.getGoalType().isBlank()) goal.setGoalType("QUALITY");
        if (goal.getUnit() == null || goal.getUnit().isBlank()) goal.setUnit("%");
        if (goal.getTargetValue() == null) goal.setTargetValue(BigDecimal.ZERO);
        if (goal.getActualValue() == null) goal.setActualValue(BigDecimal.ZERO);
        String u = curUser();
        goal.setCreatedBy(u);
        goal.setUpdatedBy(u);
        mapper.insert(goal);
        return goal;
    }

    @Override
    @Transactional
    public QmsQualityGoal update(QmsQualityGoal goal) {
        QmsQualityGoal exist = mapper.selectById(goal.getId());
        if (exist == null) throw new com.konli.qms.common.exception.BusinessException("目标不存在");
        exist.setGoalName(goal.getGoalName());
        exist.setGoalType(goal.getGoalType());
        exist.setPeriod(goal.getPeriod());
        exist.setTargetValue(goal.getTargetValue());
        exist.setActualValue(goal.getActualValue());
        exist.setUnit(goal.getUnit());
        exist.setOwner(goal.getOwner());
        exist.setDeadline(goal.getDeadline());
        exist.setRemark(goal.getRemark());
        exist.setUpdatedBy(curUser());
        mapper.updateById(exist);
        return exist;
    }

    @Override
    @Transactional
    public void delete(String id) {
        mapper.deleteById(id);
    }

    /** 达成率=实际/目标(目标为0时兜底100%)。 */
    private BigDecimal rate(QmsQualityGoal g) {
        if (g.getTargetValue() == null || g.getTargetValue().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        return g.getActualValue().divide(g.getTargetValue(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Map<String, Object> stats() {
        String org = curOrg();
        LambdaQueryWrapper<QmsQualityGoal> w = new LambdaQueryWrapper<>();
        if (org != null) w.eq(QmsQualityGoal::getOrgId, org);
        List<QmsQualityGoal> all = mapper.selectList(w);
        Map<String, Object> res = new LinkedHashMap<>();
        long total = all.size();
        long notReached = 0;
        double sumRate = 0;
        Map<String, Integer> byType = new LinkedHashMap<>();
        for (QmsQualityGoal g : all) {
            BigDecimal r = rate(g);
            if (r.compareTo(BigDecimal.valueOf(100)) < 0) notReached++;
            sumRate += r.doubleValue();
            String t = g.getGoalType() == null ? "OTHER" : g.getGoalType();
            byType.merge(t, 1, Integer::sum);
        }
        res.put("total", total);
        res.put("notReached", notReached);
        res.put("overallRate", total == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(sumRate / total).setScale(2, RoundingMode.HALF_UP));
        res.put("byType", byType);
        return res;
    }
}
