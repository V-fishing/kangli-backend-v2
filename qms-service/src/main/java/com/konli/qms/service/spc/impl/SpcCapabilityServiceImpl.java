package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcCapability;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcCapabilityMapper;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.domain.spc.mapper.SpcSubgroupMapper;
import com.konli.qms.service.spc.SpcCapabilityService;
import com.konli.qms.service.spc.dto.SpcSupplierCpkVo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SpcCapabilityServiceImpl implements SpcCapabilityService {

    private final SpcCapabilityMapper spcCapabilityMapper;
    private final SpcParamMapper spcParamMapper;
    private final SpcSubgroupMapper spcSubgroupMapper;
    private final JdbcTemplate jdbcTemplate;

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
    @Transactional
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
        // SPC CPK→供应商绩效联动:能力等级(充足/尚可/不足)更新供应商质量评分
        try {
            String perfPeriod = periodValue != null ? periodValue : YearMonth.now().toString();
            jdbcTemplate.update(
                    "UPDATE ops.sqm_supplier_performance SET quality_score = ? WHERE param_id = ? AND period = ?",
                    r.cpk, paramId, perfPeriod);
        } catch (Exception ignored) {}
        return cap;
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

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal x : xbars) {
            sum = sum.add(x);
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(xbars.size()), 6, RoundingMode.HALF_UP);

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

        // 样本标准差 sigmaOverall = sqrt( sum((xi-mean)^2) / (count-1) )
        double sqSum = 0;
        for (BigDecimal x : xbars) {
            double diff = x.doubleValue() - mean.doubleValue();
            sqSum += diff * diff;
        }
        double sigmaOverall = xbars.size() <= 1 ? 0 : Math.sqrt(sqSum / (xbars.size() - 1));

        BigDecimal usl = param.getSpecUpper();
        BigDecimal lsl = param.getSpecLower();
        int sampleCount = subgroups.size();

        CapResult r = new CapResult();
        r.param = param;
        r.usl = usl;
        r.lsl = lsl;
        r.sampleCount = sampleCount;

        // ---- 诊断:能力为何无法计算(前端据此向用户说明"为什么没有数据") ----
        if (usl == null || lsl == null) {
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
        // 过程潜力指数 Cp/Pp(双侧规格宽 / (2kσ),不取单侧)
        BigDecimal width = usl.subtract(lsl);
        BigDecimal cp = width.divide(BigDecimal.valueOf(2 * kVal * sigmaWithin), 4, RoundingMode.HALF_UP);
        BigDecimal pp = width.divide(BigDecimal.valueOf(2 * kVal * sigmaOverall), 4, RoundingMode.HALF_UP);

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

    /** 看板"跨参数 CPK 对比":对每个 SPC 参数实时计算 CPK(无供应商维度,以参数名作为对比项)。 */
    @Override
    public List<SpcSupplierCpkVo> getSupplierCpk() {
        List<SpcParam> params = spcParamMapper.selectList(null);
        List<SpcSupplierCpkVo> result = new ArrayList<>();
        for (SpcParam param : params) {
            CapResult r;
            try {
                r = compute(param.getId());
            } catch (Exception e) {
                // 无子组/数据不足的参数跳过,不计入对比
                continue;
            }
            if (r.cpk == null) {
                continue;
            }
            SpcSupplierCpkVo vo = new SpcSupplierCpkVo();
            vo.setSup("");
            vo.setMat(param.getParamName());
            vo.setCpk(r.cpk.doubleValue());
            vo.setLvl(r.level);
            result.add(vo);
        }
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


    /** 计算 min(usl-mean, mean-lsl) / (k * sigma),保留 scale 位小数;无规格限或 sigma=0 返回 null。 */
    private BigDecimal calcIndex(BigDecimal usl, BigDecimal lsl, BigDecimal mean, double sigma, int scale, double k) {
        if (sigma == 0 || usl == null || lsl == null || mean == null) {
            return null;
        }
        double uslMean = usl.doubleValue() - mean.doubleValue();
        double meanLsl = mean.doubleValue() - lsl.doubleValue();
        double minDelta = Math.min(uslMean, meanLsl);
        double val = minDelta / (k * sigma);
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
