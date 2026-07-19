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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpcCapabilityServiceImpl implements SpcCapabilityService {

    private final SpcCapabilityMapper spcCapabilityMapper;
    private final SpcParamMapper spcParamMapper;
    private final SpcSubgroupMapper spcSubgroupMapper;

    @Override
    public List<SpcCapability> list() {
        return spcCapabilityMapper.selectList(null);
    }

    @Override
    public List<SpcCapability> trend(String paramId, int months) {
        if (paramId == null || paramId.isBlank()) {
            throw new BusinessException(400, "paramId 不能为空");
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
        SpcParam param = spcParamMapper.selectById(paramId);
        if (param == null) {
            throw new BusinessException(400, "SPC 参数不存在");
        }

        List<SpcSubgroup> subgroups = spcSubgroupMapper.selectList(
                new LambdaQueryWrapper<SpcSubgroup>().eq(SpcSubgroup::getParamId, paramId).orderByDesc(SpcSubgroup::getSubgroupTime));
        if (subgroups == null || subgroups.isEmpty()) {
            throw new BusinessException(400, "无子组数据");
        }

        List<BigDecimal> xbars = subgroups.stream().map(SpcSubgroup::getXbar).filter(java.util.Objects::nonNull).toList();
        List<BigDecimal> ranges = subgroups.stream().map(SpcSubgroup::getRangeR).filter(java.util.Objects::nonNull).toList();
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
        BigDecimal cpk = calcIndex(usl, lsl, mean, sigmaWithin, 3);
        BigDecimal ppk = calcIndex(usl, lsl, mean, sigmaOverall, 3);

        String level;
        if (cpk != null && cpk.compareTo(BigDecimal.valueOf(1.33)) >= 0) {
            level = "充足";
        } else if (cpk != null && cpk.compareTo(BigDecimal.ONE) >= 0) {
            level = "尚可";
        } else {
            level = "不足";
        }

        SpcCapability cap = new SpcCapability();
        cap.setOrgId(param.getOrgId());
        cap.setParamId(paramId);
        cap.setPeriodType(periodType);
        cap.setPeriodValue(periodValue);
        cap.setCpk(cpk);
        cap.setPpk(ppk);
        cap.setLevel(level);
        cap.setSampleCount(subgroups.size());
        cap.setUsl(usl);
        cap.setLsl(lsl);
        cap.setCalcWindowDays(30);
        cap.setCalcAt(LocalDateTime.now());
        spcCapabilityMapper.insert(cap);
        return cap;
    }

    /** 计算 min(usl-mean, mean-lsl) / (3 * sigma),保留 scale 位小数;无规格限或 sigma=0 返回 null。 */
    private BigDecimal calcIndex(BigDecimal usl, BigDecimal lsl, BigDecimal mean, double sigma, int scale) {
        if (sigma == 0 || usl == null || lsl == null || mean == null) {
            return null;
        }
        double uslMean = usl.doubleValue() - mean.doubleValue();
        double meanLsl = mean.doubleValue() - lsl.doubleValue();
        double minDelta = Math.min(uslMean, meanLsl);
        double val = minDelta / (3 * sigma);
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
