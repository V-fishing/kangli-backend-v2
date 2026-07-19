package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmSupplierGradeRule;
import com.konli.qms.domain.sqm.entity.SqmSupplierPerformance;
import com.konli.qms.domain.sqm.mapper.SqmIncomingLotMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierGradeRuleMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierPerformanceMapper;
import com.konli.qms.service.sqm.SqmSupplierPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmSupplierPerformanceServiceImpl implements SqmSupplierPerformanceService {

    private final SqmSupplierPerformanceMapper sqmSupplierPerformanceMapper;
    private final SqmIncomingLotMapper sqmIncomingLotMapper;
    private final SqmSupplierGradeRuleMapper gradeRuleMapper;

    @Override
    public List<SqmSupplierPerformance> list(String supplierId) {
        LambdaQueryWrapper<SqmSupplierPerformance> w = new LambdaQueryWrapper<>();
        if (supplierId != null && !supplierId.isBlank()) {
            w.eq(SqmSupplierPerformance::getSupplierId, supplierId);
        }
        w.orderByDesc(SqmSupplierPerformance::getPeriod);
        return sqmSupplierPerformanceMapper.selectList(w);
    }

    @Override
    public SqmSupplierPerformance get(String id) {
        return sqmSupplierPerformanceMapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmSupplierPerformance create(SqmSupplierPerformance performance) {
        if (performance.getLevel() == null && performance.getScore() != null) {
            performance.setLevel(levelOf(performance.getScore()));
        }
        if (performance.getObserveFlag() == null) {
            performance.setObserveFlag(false);
        }
        if (performance.getDataMissingFlag() == null) {
            performance.setDataMissingFlag(false);
        }
        sqmSupplierPerformanceMapper.insert(performance);
        return performance;
    }

    @Override
    @Transactional
    public SqmSupplierPerformance calc(String supplierId, String period) {
        if (supplierId == null || supplierId.isBlank()) {
            throw new BusinessException(400, "supplierId 不能为空");
        }
        if (period == null || period.isBlank()) {
            throw new BusinessException(400, "period 不能为空");
        }

        // 查该供应商全部来料批次
        List<SqmIncomingLot> lots = sqmIncomingLotMapper.selectList(
                new LambdaQueryWrapper<SqmIncomingLot>()
                        .eq(SqmIncomingLot::getSupplierId, supplierId));

        // 来料合格率 = count(iqcPass=true) / count(*)
        BigDecimal incomingPassRate;
        if (lots == null || lots.isEmpty()) {
            incomingPassRate = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            long passCount = lots.stream().filter(l -> Boolean.TRUE.equals(l.getIqcPass())).count();
            incomingPassRate = BigDecimal.valueOf(passCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(lots.size()), 2, RoundingMode.HALF_UP);
        }

        // 交付及时率:sqm_incoming_lot 无 due_date 字段 -> 100%
        BigDecimal deliveryTimelyRate = new BigDecimal("100.00");

        // score = (incomingPassRate + deliveryTimelyRate) / 2
        BigDecimal score = incomingPassRate.add(deliveryTimelyRate)
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        String orgId = currentOrgId();

        // 删除该供应商该周期旧记录(UNIQUE(supplier_id, period)),再插入
        sqmSupplierPerformanceMapper.delete(
                new LambdaQueryWrapper<SqmSupplierPerformance>()
                        .eq(SqmSupplierPerformance::getSupplierId, supplierId)
                        .eq(SqmSupplierPerformance::getPeriod, period));

        // 绩效分级挂钩:用 sqm_supplier_grade_rule 区间匹配,覆盖简单算法的 level
        String level = levelByRule(score);

        SqmSupplierPerformance p = new SqmSupplierPerformance();
        p.setOrgId(orgId);
        p.setSupplierId(supplierId);
        p.setPeriod(period);
        p.setScore(score);
        p.setIncomingPassRate(incomingPassRate);
        p.setDeliveryTimelyRate(deliveryTimelyRate);
        p.setLevel(level);
        p.setObserveFlag(false);
        p.setDataMissingFlag(lots == null || lots.isEmpty());
        sqmSupplierPerformanceMapper.insert(p);
        return p;
    }

    /**
     * 按 sqm_supplier_grade_rule 评级规则匹配 level(scoreMin <= score < scoreMax),无匹配返回 D。
     * 规则表查询失败时回退到简单算法 levelOf。
     */
    private String levelByRule(BigDecimal score) {
        try {
            List<SqmSupplierGradeRule> rules = gradeRuleMapper.selectList(null);
            if (rules != null && !rules.isEmpty()) {
                return rules.stream()
                        .filter(r -> score.compareTo(r.getScoreMin()) >= 0
                                && score.compareTo(r.getScoreMax()) < 0)
                        .map(SqmSupplierGradeRule::getLevel)
                        .findFirst()
                        .orElse("D");
            }
        } catch (Exception ignore) {
            // 规则未配置或查询异常,回退到简单算法
        }
        return levelOf(score);
    }

    private String levelOf(BigDecimal score) {
        if (score == null) return "D";
        double s = score.doubleValue();
        if (s >= 90) return "A";
        if (s >= 80) return "B";
        if (s >= 70) return "C";
        return "D";
    }

    private String currentOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? null : u.orgId();
    }
}
