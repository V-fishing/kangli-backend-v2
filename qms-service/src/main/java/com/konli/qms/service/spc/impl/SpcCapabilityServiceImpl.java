package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcCapability;
import com.konli.qms.domain.spc.entity.SpcMeasurement;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcCapabilityMapper;
import com.konli.qms.domain.spc.mapper.SpcMeasurementMapper;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.domain.spc.mapper.SpcSubgroupMapper;
import com.konli.qms.service.spc.SpcCapabilityService;
import com.konli.qms.service.spc.dto.SpcParamCpkVo;
import com.konli.qms.service.sqm.SqmSupplierPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpcCapabilityServiceImpl implements SpcCapabilityService {

    private final SpcCapabilityMapper spcCapabilityMapper;
    private final SpcParamMapper spcParamMapper;
    private final SpcSubgroupMapper spcSubgroupMapper;
    private final SpcMeasurementMapper spcMeasurementMapper;
    private final JdbcTemplate jdbcTemplate;
    private final SqmSupplierPerformanceService sqmSupplierPerformanceService;

    @Override
    public List<SpcCapability> list() {
        return spcCapabilityMapper.selectList(null);
    }

    @Override
    public List<SpcCapability> trend(String paramId, int months) {
        if (paramId == null || paramId.isBlank()) {
            // 未指定参数时返回空列表(前端能力分析页默认无选中参数),避免 400/500
            return List.of();
        }
        if (months <= 0) {
            months = 12;
        }
        // 按 periodValue 倒序取最近 months 条,返回时反转为时间正序(oldest -> newest)
        List<SpcCapability> desc = spcCapabilityMapper.selectList(
                new LambdaQueryWrapper<SpcCapability>()
                        .eq(SpcCapability::getParamId, paramId)
                        .orderByDesc(SpcCapability::getPeriodValue)
                        .last("LIMIT " + months));
        Collections.reverse(desc);
        return desc;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SpcCapability calc(String paramId, String periodType, String periodValue) {
        CapResult r = compute(paramId);

        // 控制图页通常仅传 paramId,periodType/periodValue 可能为 null;
        // spc_capability 这两列均非空,缺省时给合理默认,避免插入报 NOT NULL 约束 500
        if (periodType == null || periodType.isBlank()) {
            periodType = "OVERALL";
        }
        if (periodValue == null || periodValue.isBlank()) {
            periodValue = YearMonth.now().toString();
        }

        SpcCapability cap = new SpcCapability();
        cap.setOrgId(r.param.getOrgId());
        cap.setParamId(paramId);
        cap.setPeriodType(periodType);
        cap.setPeriodValue(periodValue);
        cap.setCpk(r.cpk);
        cap.setPpk(r.ppk);
        cap.setCp(r.cp);
        cap.setPp(r.pp);
        cap.setCalcNote(r.calcNote);
        cap.setLevel(r.level);
        cap.setSampleCount(r.sampleCount);
        cap.setUsl(r.usl);
        cap.setLsl(r.lsl);
        cap.setCalcWindowDays(30);
        cap.setCalcAt(LocalDateTime.now());
        // B4 修复:同(paramId,periodType,periodValue)已存在则 update,避免唯一键重复 500
        SpcCapability existing = spcCapabilityMapper.selectOne(
                new LambdaQueryWrapper<SpcCapability>()
                        .eq(SpcCapability::getParamId, paramId)
                        .eq(SpcCapability::getPeriodType, periodType)
                        .eq(SpcCapability::getPeriodValue, periodValue));
        if (existing != null) {
            cap.setId(existing.getId());
            spcCapabilityMapper.updateById(cap);
        } else {
            spcCapabilityMapper.insert(cap);
        }
        // SPC CPK→供应商绩效联动:按能力等级(充足/尚可/不足)折算 0-100 质量分,
        // 写入 sqm_supplier_performance.quality_score 并加权重算总分与等级。
        // 放到 afterCommit 执行:联动失败不影响主事务(CPK 落库)提交。
        final String supplierId = r.param.getSupplierId();
        final BigDecimal qualityScore = levelToQualityScore(r.level);
        final String perfPeriod = periodValue != null ? periodValue : YearMonth.now().toString();
        if (supplierId != null && !supplierId.isBlank() && qualityScore != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        sqmSupplierPerformanceService.applySpcQualityScore(supplierId, perfPeriod, qualityScore);
                    } catch (Exception e) {
                        log.warn("SPC CPK→供应商绩效联动失败(已忽略), supplierId={}, period={}: {}",
                                supplierId, perfPeriod, e.getMessage());
                    }
                }
            });
        }
        return cap;
    }

    /** 能力等级 → 质量分:充足 90 / 尚可 75 / 不足 60;其他(无法计算/样本过少等)返回 null 表示不联动。 */
    private static BigDecimal levelToQualityScore(String level) {
        if ("充足".equals(level)) {
            return new BigDecimal("90");
        }
        if ("尚可".equals(level)) {
            return new BigDecimal("75");
        }
        if ("不足".equals(level)) {
            return new BigDecimal("60");
        }
        return null;
    }

    /**
     * 只读计算能力指数(不落库、不更新供应商绩效),供看板"跨参数 CPK 对比"聚合使用。
     * 复用与 {@link #calc} 完全一致的数学口径。
     */
    private CapResult compute(String paramId) {
        SpcParam param = spcParamMapper.selectById(paramId);
        if (param == null) {
            throw new BusinessException(400, "SPC 参数不存在");
        }

        List<SpcSubgroup> subgroups = spcSubgroupMapper.selectList(
                new LambdaQueryWrapper<SpcSubgroup>().eq(SpcSubgroup::getParamId, paramId).orderByDesc(SpcSubgroup::getSubgroupTime));
        if (subgroups == null || subgroups.isEmpty()) {
            throw new BusinessException(400, "无子组数据");
        }

        List<BigDecimal> xbars = subgroups.stream().map(SpcSubgroup::getXbar).filter(Objects::nonNull).toList();
        List<BigDecimal> ranges = subgroups.stream().map(SpcSubgroup::getRangeR).filter(Objects::nonNull).toList();
        if (xbars.isEmpty()) {
            throw new BusinessException(400, "无子组数据");
        }

        // 展开该参数所有子组的原始测量值(计量型),据此计算整体总均值与整体标准差 σ_overall。
        // 修复此前用"子组均值 xbar 的标准差"当整体 σ 的缺陷:子组均值会把个体差异平滑掉,
        // 导致 σ_overall 被系统性低估、Ppk/PP 被高估(甚至出现 σ_within > σ_overall 的反直觉结果)。
        List<String> subgroupIds = subgroups.stream().map(SpcSubgroup::getId).filter(Objects::nonNull).toList();
        List<BigDecimal> rawValues = subgroupIds.isEmpty() ? Collections.emptyList()
                : spcMeasurementMapper.selectList(new LambdaQueryWrapper<SpcMeasurement>()
                        .in(SpcMeasurement::getSubgroupId, subgroupIds))
                .stream().map(SpcMeasurement::getValue).filter(Objects::nonNull).toList();

        BigDecimal mean;
        double sigmaOverall;
        if (!rawValues.isEmpty()) {
            BigDecimal rawSum = BigDecimal.ZERO;
            for (BigDecimal v : rawValues) {
                rawSum = rawSum.add(v);
            }
            mean = rawSum.divide(BigDecimal.valueOf(rawValues.size()), 8, RoundingMode.HALF_UP);
            double m = mean.doubleValue();
            double sq = 0;
            for (BigDecimal v : rawValues) {
                double d = v.doubleValue() - m;
                sq += d * d;
            }
            sigmaOverall = rawValues.size() <= 1 ? 0 : Math.sqrt(sq / (rawValues.size() - 1));
        } else {
            // 兜底:历史数据无原始测量值时,回退到子组均值口径(旧逻辑)
            BigDecimal sum = BigDecimal.ZERO;
            for (BigDecimal x : xbars) {
                sum = sum.add(x);
            }
            mean = sum.divide(BigDecimal.valueOf(xbars.size()), 6, RoundingMode.HALF_UP);
            double m = mean.doubleValue();
            double sq = 0;
            for (BigDecimal x : xbars) {
                double d = x.doubleValue() - m;
                sq += d * d;
            }
            sigmaOverall = xbars.size() <= 1 ? 0 : Math.sqrt(sq / (xbars.size() - 1));
        }

        int n = (param.getSubgroupSize() == null || param.getSubgroupSize() <= 0) ? 5 : param.getSubgroupSize();
        double d2 = d2Factor(n);
        // σ 倍数 k:默认 3,可配 2 / 2.5 等
        BigDecimal k = param.getSigmaK() != null ? param.getSigmaK() : BigDecimal.valueOf(3);
        double kVal = k.doubleValue();

        BigDecimal avgRange = BigDecimal.ZERO;
        for (BigDecimal r : ranges) {
            avgRange = avgRange.add(r);
        }
        avgRange = ranges.isEmpty() ? BigDecimal.ZERO : avgRange.divide(BigDecimal.valueOf(ranges.size()), 6, RoundingMode.HALF_UP);
        double sigmaWithin = avgRange.doubleValue() / d2;

        BigDecimal usl = param.getSpecUpper();
        BigDecimal lsl = param.getSpecLower();
        int sampleCount = subgroups.size();

        CapResult r = new CapResult();
        r.param = param;
        r.usl = usl;
        r.lsl = lsl;
        r.sampleCount = sampleCount;

        // ---- 诊断:能力为何无法计算(前端据此向用户说明"为什么没有数据") ----
        // 单侧规格(仅下限如 ≥95 或仅上限如 ≤xx)仍可算单侧指数 PPU/PPL;仅当两侧都缺失才无法计算
        if (usl == null && lsl == null) {
            r.calcNote = "未配置规格上下限(检验标准库 SpecUpper/SpecLower),无法计算过程能力指数";
            r.level = "无法计算";
            return r;
        }
        if (sigmaWithin == 0.0 || sigmaOverall == 0.0) {
            r.calcNote = "数据无变异(σ=0),无法计算过程能力指数";
            r.level = "无法计算";
            return r;
        }
        if (sampleCount < 10) {
            r.calcNote = "样本子组不足(近30天仅 " + sampleCount + " 组,<10),暂无法可靠计算";
            r.level = "样本过少,无法计算";
            return r;
        }

        // Cpk 所用 σ:overall(整体)模式用 sigmaOverall,否则用 within(组内)
        double sigmaForCpk = "overall".equals(param.getSigmaMethod()) ? sigmaOverall : sigmaWithin;

        // 能力指数 Cpk/Ppk(取单侧 min)。Cpk 用所选 σ;Ppk 恒用 overall σ
        BigDecimal cpk = calcIndex(usl, lsl, mean, sigmaForCpk, 4, kVal);
        BigDecimal ppk = calcIndex(usl, lsl, mean, sigmaOverall, 4, kVal);
        // 过程潜力指数 Cp/Pp(仅双侧规格才有意义;单侧规格无"规格宽度"概念,置 null)
        BigDecimal cp = null;
        BigDecimal pp = null;
        if (usl != null && lsl != null) {
            BigDecimal width = usl.subtract(lsl);
            cp = width.divide(BigDecimal.valueOf(2 * kVal * sigmaWithin), 4, RoundingMode.HALF_UP);
            pp = width.divide(BigDecimal.valueOf(2 * kVal * sigmaOverall), 4, RoundingMode.HALF_UP);
        }

        // SR-SPC-013:样本不足标注 -- <10 上方已拦截;10~24 组标"数据量不足,CPK仅供参考"(仍算);>=25 正常分级
        String level;
        if (sampleCount < 25) {
            level = "数据量不足，CPK仅供参考";
        } else if (cpk.compareTo(BigDecimal.valueOf(1.33)) >= 0) {
            level = "充足";
        } else if (cpk.compareTo(BigDecimal.ONE) >= 0) {
            level = "尚可";
        } else {
            level = "不足";
        }

        r.cpk = cpk;
        r.ppk = ppk;
        r.cp = cp;
        r.pp = pp;
        r.level = level;
        return r;
    }

    /** 看板"跨参数 CPK 对比":以参数维度聚合 CPK。优先取最近一条落库值(与概览/趋势同口径),无落库时实时计算兜底。 */
    @Override
    public List<SpcParamCpkVo> getParamCpk() {
        List<SpcParam> params = spcParamMapper.selectList(null);
        List<SpcParamCpkVo> result = new ArrayList<>();
        for (SpcParam param : params) {
            // 仅计量型(VARIABLE)参数参与 CPK 对比;计数型(ATTRIBUTE,无规格限)不适用
            if (!"VARIABLE".equalsIgnoreCase(param.getDataType())) {
                continue;
            }
            SpcParamCpkVo vo = new SpcParamCpkVo();
            vo.setParamId(param.getId());
            vo.setParamName(param.getParamName());
            vo.setProcName(param.getProcName());
            vo.setSrcWoNo(param.getSrcWoNo());

            // 优先取最近落库值(与概览列表、趋势图同口径,避免实时重算导致两处 CPK 不一致)
            SpcCapability cap = spcCapabilityMapper.selectOne(
                    new LambdaQueryWrapper<SpcCapability>()
                            .eq(SpcCapability::getParamId, param.getId())
                            .orderByDesc(SpcCapability::getCalcAt)
                            .last("LIMIT 1"));
            if (cap != null) {
                vo.setCpk(cap.getCpk() != null ? cap.getCpk().doubleValue() : null);
                vo.setLevel(cap.getLevel());
                vo.setSampleCount(cap.getSampleCount());
                vo.setCalcNote(cap.getCalcNote());
            } else {
                // 无落库记录:实时计算兜底
                try {
                    CapResult r = compute(param.getId());
                    vo.setCpk(r.cpk != null ? r.cpk.doubleValue() : null);
                    vo.setLevel(r.level);
                    vo.setSampleCount(r.sampleCount);
                    vo.setCalcNote(r.calcNote);
                } catch (Exception e) {
                    vo.setLevel("无法计算");
                    vo.setCalcNote(e.getMessage());
                }
            }
            result.add(vo);
        }
        // 按 CPK 升序(能力最差的排前,便于快速定位风险参数);无 CPK 值的排最后
        result.sort((a, b) -> {
            if (a.getCpk() == null && b.getCpk() == null) return 0;
            if (a.getCpk() == null) return 1;
            if (b.getCpk() == null) return -1;
            return Double.compare(a.getCpk(), b.getCpk());
        });
        return result;
    }

    /** 能力计算中间结果(只读,不落库)。 */
    private static class CapResult {
        SpcParam param;
        BigDecimal cpk;
        BigDecimal ppk;
        BigDecimal cp;
        BigDecimal pp;
        String calcNote;
        String level;
        int sampleCount;
        BigDecimal usl;
        BigDecimal lsl;
    }


    /** 计算能力指数 / (k * sigma),保留 scale 位小数;无规格限或 sigma=0 返回 null。
     *  双侧规格取 min(usl-mean, mean-lsl);单侧规格仅算对应一侧(≥下限→PPU,≤上限→PPL)。 */
    private BigDecimal calcIndex(BigDecimal usl, BigDecimal lsl, BigDecimal mean, double sigma, int scale, double k) {
        if (sigma == 0 || mean == null || (usl == null && lsl == null)) {
            return null;
        }
        double delta;
        if (usl != null && lsl != null) {
            delta = Math.min(usl.doubleValue() - mean.doubleValue(), mean.doubleValue() - lsl.doubleValue());
        } else if (usl != null) {
            delta = usl.doubleValue() - mean.doubleValue();
        } else {
            delta = mean.doubleValue() - lsl.doubleValue();
        }
        double val = delta / (k * sigma);
        return BigDecimal.valueOf(val).setScale(scale, RoundingMode.HALF_UP);
    }

    private double d2Factor(int n) {
        switch (n) {
            case 2: return 1.128;
            case 3: return 1.693;
            case 4: return 2.059;
            case 5: return 2.326;
            case 6: return 2.534;
            case 7: return 2.704;
            case 8: return 2.847;
            case 9: return 2.970;
            case 10: return 3.078;
            default: return 2.326;
        }
    }
}
