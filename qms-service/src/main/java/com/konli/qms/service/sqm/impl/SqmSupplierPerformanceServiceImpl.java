package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmSupplier;
import com.konli.qms.domain.sqm.entity.SqmSupplierGradeRule;
import com.konli.qms.domain.sqm.entity.SqmSupplierPerformance;
import com.konli.qms.domain.sqm.mapper.SqmIncomingLotMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierGradeRuleMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierPerformanceMapper;
import com.konli.qms.service.sqm.SqmSupplierPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqmSupplierPerformanceServiceImpl implements SqmSupplierPerformanceService {

    private final SqmSupplierPerformanceMapper sqmSupplierPerformanceMapper;
    private final SqmIncomingLotMapper sqmIncomingLotMapper;
    private final SqmSupplierGradeRuleMapper gradeRuleMapper;
    private final SqmSupplierMapper sqmSupplierMapper;

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

        // orgId 优先取当前登录用户;当为空/超管哨兵(ROOT)/非法 UUID 时,回退取供应商自身 orgId
        String orgId = currentOrgId();
        if (!isValidUuid(orgId)) {
            SqmSupplier supplier = sqmSupplierMapper.selectById(supplierId);
            if (supplier == null) {
                throw new BusinessException(404, "供应商不存在");
            }
            orgId = supplier.getOrgId();
        }

        // 删除该供应商该周期旧记录(UNIQUE(supplier_id, period)),再插入
        // 先保留 SPC CPK 联动写入的质量分,避免重算来料绩效时将其覆盖清空
        SqmSupplierPerformance prev = sqmSupplierPerformanceMapper.selectOne(
                new LambdaQueryWrapper<SqmSupplierPerformance>()
                        .eq(SqmSupplierPerformance::getSupplierId, supplierId)
                        .eq(SqmSupplierPerformance::getPeriod, period));
        BigDecimal prevQualityScore = prev != null ? prev.getQualityScore() : null;
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
        p.setQualityScore(prevQualityScore);
        p.setObserveFlag(false);
        p.setDataMissingFlag(lots == null || lots.isEmpty());
        sqmSupplierPerformanceMapper.insert(p);
        return p;
    }

    /**
     * SPC CPK→供应商质量分联动:按 (supplier_id, period) upsert 质量分,
     * 并按权重(质量 0.4 / 来料 0.3 / 交付 0.3)重算综合分与 A/B/C/D 等级。
     * 记录已存在则更新质量分并重算;不存在则新建(缺失来料数据时 dataMissingFlag=true)。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applySpcQualityScore(String supplierId, String period, BigDecimal qualityScore) {
        if (supplierId == null || supplierId.isBlank() || period == null || period.isBlank() || qualityScore == null) {
            return;
        }
        LambdaQueryWrapper<SqmSupplierPerformance> w = new LambdaQueryWrapper<>();
        w.eq(SqmSupplierPerformance::getSupplierId, supplierId).eq(SqmSupplierPerformance::getPeriod, period);
        SqmSupplierPerformance existing = sqmSupplierPerformanceMapper.selectOne(w);

        // 缺省语义与 calc 一致:来料合格率未知按 0、交付及时率按 100
        BigDecimal incoming = (existing != null && existing.getIncomingPassRate() != null)
                ? existing.getIncomingPassRate() : BigDecimal.ZERO;
        BigDecimal delivery = (existing != null && existing.getDeliveryTimelyRate() != null)
                ? existing.getDeliveryTimelyRate() : new BigDecimal("100.00");
        BigDecimal score = qualityScore.multiply(new BigDecimal("0.4"))
                .add(incoming.multiply(new BigDecimal("0.3")))
                .add(delivery.multiply(new BigDecimal("0.3")))
                .setScale(2, RoundingMode.HALF_UP);
        String level = levelByRule(score);

        if (existing != null) {
            existing.setQualityScore(qualityScore);
            existing.setScore(score);
            existing.setLevel(level);
            sqmSupplierPerformanceMapper.updateById(existing);
        } else {
            SqmSupplier supplier = sqmSupplierMapper.selectById(supplierId);
            if (supplier == null) {
                log.warn("[供应商绩效] SPC 联动失败:供应商不存在 supplierId={}", supplierId);
                return;
            }
            SqmSupplierPerformance p = new SqmSupplierPerformance();
            p.setOrgId(supplier.getOrgId());
            p.setSupplierId(supplierId);
            p.setPeriod(period);
            p.setQualityScore(qualityScore);
            p.setScore(score);
            p.setLevel(level);
            p.setIncomingPassRate(incoming);
            p.setDeliveryTimelyRate(delivery);
            p.setDataMissingFlag(true);
            p.setObserveFlag(false);
            sqmSupplierPerformanceMapper.insert(p);
        }
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

    /** 判断字符串是否为合法 UUID(排除 null/空/超管哨兵 ROOT 等)。 */
    private static boolean isValidUuid(String s) {
        if (s == null || s.isBlank()) {
            return false;
        }
        try {
            java.util.UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
