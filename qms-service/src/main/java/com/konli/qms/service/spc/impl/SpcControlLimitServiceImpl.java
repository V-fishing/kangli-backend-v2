package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcControlLimit;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcControlLimitMapper;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.domain.spc.mapper.SpcSubgroupMapper;
import com.konli.qms.service.spc.SpcControlLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SpcControlLimitServiceImpl implements SpcControlLimitService {

    private final SpcControlLimitMapper spcControlLimitMapper;
    private final SpcSubgroupMapper spcSubgroupMapper;
    private final SpcParamMapper spcParamMapper;

    @Override
    public List<SpcControlLimit> list(String paramId) {
        LambdaQueryWrapper<SpcControlLimit> w = new LambdaQueryWrapper<>();
        if (paramId != null && !paramId.isBlank()) {
            w.eq(SpcControlLimit::getParamId, paramId);
        }
        w.orderByDesc(SpcControlLimit::getCalcAt);
        return spcControlLimitMapper.selectList(w);
    }

    @Override
    public SpcControlLimit getActive(String paramId) {
        if (paramId == null || paramId.isBlank()) {
            throw new BusinessException(400, "paramId 不能为空");
        }
        return spcControlLimitMapper.selectOne(
                new LambdaQueryWrapper<SpcControlLimit>()
                        .eq(SpcControlLimit::getParamId, paramId)
                        .eq(SpcControlLimit::getIsActive, true)
                        .last("LIMIT 1"));
    }

    @Override
    @Transactional
    public SpcControlLimit calc(String paramId) {
        if (paramId == null || paramId.isBlank()) {
            throw new BusinessException(400, "paramId 不能为空");
        }
        SpcParam param = spcParamMapper.selectById(paramId);
        if (param == null) {
            throw new BusinessException(400, "SPC 参数不存在");
        }

        // 查最近 25 个子组(按时间倒序)
        List<SpcSubgroup> recent = spcSubgroupMapper.selectList(
                new LambdaQueryWrapper<SpcSubgroup>()
                        .eq(SpcSubgroup::getParamId, paramId)
                        .orderByDesc(SpcSubgroup::getSubgroupTime)
                        .last("LIMIT 25"));
        if (recent == null || recent.isEmpty()) {
            throw new BusinessException(400, "无子组数据,无法计算控制限");
        }

        List<BigDecimal> xbars = recent.stream().map(SpcSubgroup::getXbar).filter(Objects::nonNull).toList();
        List<BigDecimal> ranges = recent.stream().map(SpcSubgroup::getRangeR).filter(Objects::nonNull).toList();
        if (xbars.isEmpty() || ranges.isEmpty()) {
            throw new BusinessException(400, "子组缺少 xbar/rangeR,无法计算控制限");
        }

        int n = (param.getSubgroupSize() == null || param.getSubgroupSize() <= 0) ? 5 : param.getSubgroupSize();
        double d2 = d2Factor(n);
        double d3 = d3Factor(n);

        // Xbar 图: CL=avg(xbar), sigma=avg(R)/d2, UCL=CL+3σ, LCL=CL-3σ
        BigDecimal xbarCl = avg(xbars);
        BigDecimal avgRange = avg(ranges);
        BigDecimal sigma = avgRange.divide(BigDecimal.valueOf(d2), 6, RoundingMode.HALF_UP);
        BigDecimal threeSigma = sigma.multiply(BigDecimal.valueOf(3));
        BigDecimal xbarUcl = xbarCl.add(threeSigma).setScale(4, RoundingMode.HALF_UP);
        BigDecimal xbarLcl = xbarCl.subtract(threeSigma).setScale(4, RoundingMode.HALF_UP);

        // R 图: RCL=avg(R), RUCL=RCL+3*d3*sigma(d3=0 for n<=6), RLCL=0
        BigDecimal rCl = avgRange.setScale(4, RoundingMode.HALF_UP);
        BigDecimal rUcl = rCl.add(BigDecimal.valueOf(3 * d3).multiply(sigma)).setScale(4, RoundingMode.HALF_UP);
        BigDecimal rLcl = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);

        // 旧基线 isActive=false
        spcControlLimitMapper.update(null,
                new LambdaUpdateWrapper<SpcControlLimit>()
                        .eq(SpcControlLimit::getParamId, paramId)
                        .eq(SpcControlLimit::getIsActive, true)
                        .set(SpcControlLimit::getIsActive, false));

        // 存新基线
        SpcControlLimit cl = new SpcControlLimit();
        cl.setOrgId(param.getOrgId());
        cl.setParamId(paramId);
        cl.setChartType(param.getChartType() != null ? param.getChartType() : "Xbar-R");
        cl.setBaselineSource("前25子组动态");
        cl.setNSubgroups(xbars.size());
        cl.setXbarUcl(xbarUcl);
        cl.setXbarCl(xbarCl.setScale(4, RoundingMode.HALF_UP));
        cl.setXbarLcl(xbarLcl);
        cl.setRUcl(rUcl);
        cl.setRCl(rCl);
        cl.setRLcl(rLcl);
        cl.setCalcAt(LocalDateTime.now());
        cl.setIsActive(true);
        spcControlLimitMapper.insert(cl);
        return cl;
    }

    private BigDecimal avg(List<BigDecimal> list) {
        if (list.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : list) sum = sum.add(v);
        return sum.divide(BigDecimal.valueOf(list.size()), 6, RoundingMode.HALF_UP);
    }

    private double d2Factor(int n) {
        return switch (n) {
            case 2 -> 1.128;
            case 3 -> 1.693;
            case 4 -> 2.059;
            case 5 -> 2.326;
            case 6 -> 2.534;
            case 7 -> 2.704;
            case 8 -> 2.847;
            case 9 -> 2.970;
            case 10 -> 3.078;
            default -> 2.326;
        };
    }

    /** R 图下限因子 D3,n<=6 时为 0(对齐代码规范默认 n=5)。 */
    private double d3Factor(int n) {
        return switch (n) {
            case 7 -> 0.076;
            case 8 -> 0.136;
            case 9 -> 0.184;
            case 10 -> 0.223;
            default -> 0.0;     // n <= 6
        };
    }
}
