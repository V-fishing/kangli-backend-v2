package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcControlLimit;
import com.konli.qms.domain.spc.entity.SpcMeasurement;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcControlLimitMapper;
import com.konli.qms.domain.spc.mapper.SpcMeasurementMapper;
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

/** SPC 控制限(前25子组建基线,Xbar-R 图 CL/UCL/LCL)。spc.param.* */
@Service
@RequiredArgsConstructor
public class SpcControlLimitServiceImpl implements SpcControlLimitService {

    private final SpcControlLimitMapper spcControlLimitMapper;
    private final SpcParamMapper spcParamMapper;
    private final SpcSubgroupMapper spcSubgroupMapper;
    private final SpcMeasurementMapper spcMeasurementMapper;

    @Override
    public List<SpcControlLimit> list(String paramId) {
        return spcControlLimitMapper.selectList(
                new LambdaQueryWrapper<SpcControlLimit>()
                        .eq(SpcControlLimit::getParamId, paramId)
                        .orderByDesc(SpcControlLimit::getCalcAt));
    }

    @Override
    public SpcControlLimit getActive(String paramId) {
        // 优先返回人工覆盖(manual=true),其次自动计算;同组内最新一条。
        return spcControlLimitMapper.selectOne(
                new LambdaQueryWrapper<SpcControlLimit>()
                        .eq(SpcControlLimit::getParamId, paramId)
                        .eq(SpcControlLimit::getIsActive, true)
                        .orderByDesc(SpcControlLimit::getManual)
                        .orderByDesc(SpcControlLimit::getCalcAt)
                        .last("LIMIT 1"));
    }

    @Override
    public SpcControlLimit calc(String paramId) {
        SpcParam param = spcParamMapper.selectById(paramId);
        if (param == null) {
            throw new BusinessException(400, "SPC 参数不存在");
        }

        int n = (param.getSubgroupSize() == null || param.getSubgroupSize() <= 0) ? 5 : param.getSubgroupSize();
        // σ 倍数 k:默认 3,可配 2 / 2.5 等。
        BigDecimal k = param.getSigmaK() != null ? param.getSigmaK() : BigDecimal.valueOf(3);
        double kVal = k.doubleValue();

        List<SpcSubgroup> recent = spcSubgroupMapper.selectList(
                new LambdaQueryWrapper<SpcSubgroup>()
                        .eq(SpcSubgroup::getParamId, paramId)
                        .orderByDesc(SpcSubgroup::getSubgroupTime)
                        .last("LIMIT 25"));

        if (recent.isEmpty()) {
            throw new BusinessException(400, "无子组数据,无法计算控制限");
        }

        double avgXbar = recent.stream().filter(s -> s.getXbar() != null)
                .mapToDouble(s -> s.getXbar().doubleValue()).average().orElse(0.0);
        double avgRange = recent.stream().filter(s -> s.getRangeR() != null)
                .mapToDouble(s -> s.getRangeR().doubleValue()).average().orElse(0.0);

        double d2 = d2Factor(n);
        double d3 = d3Factor(n);

        // Xbar 所用 σ:within(组内)=R̄/d2;overall(整体)=全体测量值标准差/√n。
        BigDecimal sigma;
        if ("overall".equals(param.getSigmaMethod())) {
            BigDecimal overall = overallSigma(recent, n);
            sigma = (overall != null && overall.compareTo(BigDecimal.ZERO) > 0) ? overall : BigDecimal.valueOf(avgRange / d2);
        } else {
            sigma = BigDecimal.valueOf(d2 == 0 ? 0 : avgRange / d2);
        }

        double kSigma = sigma.doubleValue() * kVal;

        SpcControlLimit cl = new SpcControlLimit();
        cl.setOrgId(param.getOrgId());
        cl.setParamId(paramId);
        cl.setChartType(param.getChartType() != null ? param.getChartType() : "Xbar");
        // Xbar 图控制限
        cl.setXbarCl(BigDecimal.valueOf(avgXbar).setScale(4, RoundingMode.HALF_UP));
        cl.setXbarUcl(BigDecimal.valueOf(avgXbar + kSigma).setScale(4, RoundingMode.HALF_UP));
        cl.setXbarLcl(BigDecimal.valueOf(avgXbar - kSigma).setScale(4, RoundingMode.HALF_UP));
        // R 图控制限(基于组内 σ,使用 d3)
        double rCl = avgRange;
        double rSigma = (d2 == 0 ? 0 : avgRange / d2) * d3;
        cl.setRcl(BigDecimal.valueOf(rCl).setScale(4, RoundingMode.HALF_UP));
        cl.setRucl(BigDecimal.valueOf(rCl + kVal * rSigma).setScale(4, RoundingMode.HALF_UP));
        cl.setRlcl(BigDecimal.valueOf(Math.max(0, rCl - kVal * rSigma)).setScale(4, RoundingMode.HALF_UP));

        cl.setNSubgroups(recent.size());
        cl.setBaselineSource("前" + recent.size() + "子组");
        cl.setManual(false);
        cl.setCalcAt(LocalDateTime.now());
        cl.setIsActive(true);
        spcControlLimitMapper.insert(cl);
        return cl;
    }

    @Override
    @Transactional
    public SpcControlLimit saveManual(String paramId, SpcControlLimit limits) {
        SpcParam param = spcParamMapper.selectById(paramId);
        if (param == null) {
            throw new BusinessException(400, "SPC 参数不存在");
        }
        if (limits.getXbarUcl() == null || limits.getXbarCl() == null || limits.getXbarLcl() == null) {
            throw new BusinessException(400, "Xbar 图 UCL/CL/LCL 均为必填");
        }
        if (limits.getXbarUcl().compareTo(limits.getXbarCl()) <= 0
                || limits.getXbarCl().compareTo(limits.getXbarLcl()) <= 0) {
            throw new BusinessException(400, "控制限必须满足 UCL > CL > LCL");
        }
        if (limits.getRucl() != null && limits.getRlcl() != null
                && limits.getRucl().compareTo(limits.getRlcl()) < 0) {
            throw new BusinessException(400, "R 图控制限必须满足 UCL ≥ LCL");
        }
        // 旧基线(自动+人工)全部置为非 active
        spcControlLimitMapper.update(null,
                new LambdaUpdateWrapper<SpcControlLimit>()
                        .eq(SpcControlLimit::getParamId, paramId)
                        .eq(SpcControlLimit::getIsActive, true)
                        .set(SpcControlLimit::getIsActive, false));

        SpcControlLimit cl = new SpcControlLimit();
        cl.setOrgId(param.getOrgId());
        cl.setParamId(paramId);
        cl.setChartType(limits.getChartType() != null ? limits.getChartType() : (param.getChartType() != null ? param.getChartType() : "Xbar"));
        cl.setXbarUcl(limits.getXbarUcl());
        cl.setXbarCl(limits.getXbarCl());
        cl.setXbarLcl(limits.getXbarLcl());
        cl.setRucl(limits.getRucl());
        cl.setRcl(limits.getRcl());
        cl.setRlcl(limits.getRlcl());
        // n_subgroups 数据库非空:人工覆盖非基于子组统计,未传时记 0。
        cl.setNSubgroups(limits.getNSubgroups() != null ? limits.getNSubgroups() : 0);
        cl.setBaselineSource("人工覆盖");
        cl.setManual(true);
        cl.setCalcAt(LocalDateTime.now());
        cl.setIsActive(true);
        spcControlLimitMapper.insert(cl);
        return cl;
    }

    /** 整体 σ 估计:全体测量值样本标准差 / √n(仅 when sigmaMethod=overall)。 */
    private BigDecimal overallSigma(List<SpcSubgroup> recent, int n) {
        List<String> ids = recent.stream().map(SpcSubgroup::getId).toList();
        if (ids.isEmpty()) {
            return null;
        }
        List<SpcMeasurement> ms = spcMeasurementMapper.selectList(
                new LambdaQueryWrapper<SpcMeasurement>().in(SpcMeasurement::getSubgroupId, ids));
        if (ms == null || ms.isEmpty()) {
            return null;
        }
        double mean = ms.stream().filter(m -> m.getValue() != null)
                .mapToDouble(m -> m.getValue().doubleValue()).average().orElse(0.0);
        double sq = ms.stream().filter(m -> m.getValue() != null)
                .mapToDouble(m -> {
                    double d = m.getValue().doubleValue() - mean;
                    return d * d;
                }).sum();
        if (ms.size() <= 1) {
            return null;
        }
        double sd = Math.sqrt(sq / (ms.size() - 1));
        return BigDecimal.valueOf(sd / Math.sqrt(n)).setScale(6, RoundingMode.HALF_UP);
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

    private double d3Factor(int n) {
        switch (n) {
            case 2: return 0.853;
            case 3: return 0.888;
            case 4: return 0.880;
            case 5: return 0.864;
            case 6: return 0.848;
            case 7: return 0.833;
            case 8: return 0.820;
            case 9: return 0.808;
            case 10: return 0.797;
            default: return 0.864;
        }
    }
}
