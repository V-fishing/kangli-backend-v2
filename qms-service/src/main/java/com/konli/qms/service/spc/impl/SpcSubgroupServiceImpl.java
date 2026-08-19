package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.spc.entity.SpcAlarm;
import com.konli.qms.domain.spc.entity.SpcGlobalConfig;
import com.konli.qms.domain.spc.entity.SpcControlLimit;
import com.konli.qms.domain.spc.entity.SpcMeasurement;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.entity.SpcRule;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcAlarmMapper;
import com.konli.qms.domain.spc.mapper.SpcControlLimitMapper;
import com.konli.qms.domain.spc.mapper.SpcMeasurementMapper;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.domain.spc.mapper.SpcRuleMapper;
import com.konli.qms.domain.spc.mapper.SpcSubgroupMapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.spc.SpcNotifyChannelService;
import com.konli.qms.service.spc.SpcSubgroupService;
import com.konli.qms.service.spc.SpcGlobalConfigService;
import com.konli.qms.service.spc.SpcCapabilityService;
import com.konli.qms.service.spc.SpcCollectTaskService;
import com.konli.qms.service.spc.SpcSampleTaskService;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.fia.FiaTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.konli.qms.service.spc.dto.ControlChartMark;
import com.konli.qms.service.spc.dto.ControlChartVo;
import com.konli.qms.service.spc.dto.CountCapabilityVo;
import com.konli.qms.service.spc.dto.CountSeries;
import com.konli.qms.service.spc.dto.SpcHistogramVo;
import com.konli.qms.service.spc.dto.SpcSubgroupVo;
import com.konli.qms.service.spc.FiaChartTypeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpcSubgroupServiceImpl implements SpcSubgroupService {

    private final SpcSubgroupMapper spcSubgroupMapper;
    private final SysUserMapper sysUserMapper;
    private final SpcParamMapper spcParamMapper;
    private final SpcMeasurementMapper spcMeasurementMapper;
    private final SpcRuleMapper spcRuleMapper;
    private final SpcAlarmMapper spcAlarmMapper;
    private final SpcControlLimitMapper spcControlLimitMapper;
    private final SpcNotifyChannelService spcNotifyChannelService;
    private final SpcGlobalConfigService spcGlobalConfigService;
    private final SpcCapabilityService spcCapabilityService;
    private final SpcCollectTaskService spcCollectTaskService;
    private final SpcSampleTaskService spcSampleTaskService;
    private final NotificationService notificationService;

    /** 回环联动需要 FIA 服务;为避免与 FiaTaskServiceImpl(其注入 SpcSubgroupService)形成构造器循环依赖,
     *  改用 @Lazy 字段注入,延迟到首次调用时解析。 */
    @Autowired
    @Lazy
    private FiaTaskService fiaTaskService;

    private final PlatformTransactionManager transactionManager;

    @Override
    public List<SpcSubgroup> list() {
        List<SpcSubgroup> list = spcSubgroupMapper.selectList(null);
        fillOperatorNames(list);
        return list;
    }

    /** 批量解析子组的录入人/创建人姓名(operator_id/created_by 为用户 UUID),单次 IN 查询无 N+1。 */
    private void fillOperatorNames(List<SpcSubgroup> subgroups) {
        if (subgroups == null || subgroups.isEmpty()) return;
        List<String> ids = new ArrayList<>();
        for (SpcSubgroup sg : subgroups) {
            if (StringUtils.hasText(sg.getOperatorId()) && !ids.contains(sg.getOperatorId())) ids.add(sg.getOperatorId());
            if (StringUtils.hasText(sg.getCreatedBy()) && !ids.contains(sg.getCreatedBy())) ids.add(sg.getCreatedBy());
        }
        if (ids.isEmpty()) return;
        Map<String, String> nameMap = sysUserMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
        for (SpcSubgroup sg : subgroups) {
            if (StringUtils.hasText(sg.getOperatorId())) sg.setOperatorName(nameMap.get(sg.getOperatorId()));
            if (StringUtils.hasText(sg.getCreatedBy())) sg.setCreatedByName(nameMap.get(sg.getCreatedBy()));
        }
    }

    @Override
    public SpcSubgroupVo get(String id) {
        SpcSubgroupVo vo = new SpcSubgroupVo();
        vo.setSubgroup(spcSubgroupMapper.selectById(id));
        vo.setMeasurements(spcMeasurementMapper.selectList(
                new LambdaQueryWrapper<SpcMeasurement>().eq(SpcMeasurement::getSubgroupId, id).orderByAsc(SpcMeasurement::getSeq)));
        return vo;
    }

    @Override
    public ControlChartVo getControlChart(String paramId, String startTime, String endTime, String stage, String sampleTaskId) {
        if (paramId == null || paramId.isBlank()) {
            throw new BusinessException(400, "paramId 不能为空");
        }
        LambdaQueryWrapper<SpcSubgroup> w = new LambdaQueryWrapper<SpcSubgroup>()
                .eq(SpcSubgroup::getParamId, paramId)
                .orderByAsc(SpcSubgroup::getSubgroupTime);
        if (stage != null && !stage.isBlank() && !"ALL".equalsIgnoreCase(stage)) {
            w.eq(SpcSubgroup::getStage, stage);
        }
        if (sampleTaskId != null && !sampleTaskId.isBlank()) {
            w.eq(SpcSubgroup::getSampleTaskId, sampleTaskId);
        }
        if (startTime != null && !startTime.isBlank()) {
            w.ge(SpcSubgroup::getSubgroupTime, LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isBlank()) {
            w.le(SpcSubgroup::getSubgroupTime, LocalDateTime.parse(endTime));
        }
        List<SpcSubgroup> subgroups = spcSubgroupMapper.selectList(w);

        // 批量解析录入人/创建人姓名(operator_id/created_by 存的是用户 UUID),避免前端依赖用户字典
        fillOperatorNames(subgroups);

        // 当前激活控制限:优先人工覆盖(manual=true),其次自动基线;active 且最新一条
        SpcControlLimit limit = spcControlLimitMapper.selectOne(
                new LambdaQueryWrapper<SpcControlLimit>()
                        .eq(SpcControlLimit::getParamId, paramId)
                        .eq(SpcControlLimit::getIsActive, true)
                        .orderByDesc(SpcControlLimit::getManual)
                        .orderByDesc(SpcControlLimit::getCalcAt)
                        .last("LIMIT 1"));

        // 异常点 marks:遍历子组,把 create 时已落库的判异结果(is_outlier/outlier_rule)映射为
        // 前端"异常点与判异规则命中"列表/控制图着色所需结构
        Map<String, String> ruleLevelMap = spcRuleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SpcRule::getRuleCode, SpcRule::getLevel, (a, b) -> a));
        List<ControlChartMark> marks = new ArrayList<>();
        for (int idx = 0; idx < subgroups.size(); idx++) {
            SpcSubgroup sg = subgroups.get(idx);
            if (Boolean.TRUE.equals(sg.getIsOutlier()) && sg.getOutlierRule() != null) {
                ControlChartMark mk = new ControlChartMark();
                mk.setI(idx);
                mk.setRule(sg.getOutlierRule());
                mk.setLevel(ruleLevelMap.getOrDefault(sg.getOutlierRule(), "预警"));
                marks.add(mk);
            }
        }

        ControlChartVo vo = new ControlChartVo();
        vo.setSubgroups(subgroups);
        vo.setLimit(limit);
        vo.setMarks(marks);
        // 计数型序列(P/NP/C/U):仅当子集含计数数据(noncforming/defectCount 非空)且参数 chartCandidates 含该类型时构建。
        vo.setCountSeries(buildCountSeries(paramId, subgroups));
        return vo;
    }

    /** 按参数配置的 chartCandidates 中计数型图类型(P/NP/C/U),由子组 nonconforming/inspectN/defectCount 构建控制图序列。 */
    private List<CountSeries> buildCountSeries(String paramId, List<SpcSubgroup> subgroups) {
        if (subgroups.isEmpty()) return null;
        SpcParam param = spcParamMapper.selectById(paramId);
        if (param == null || !StringUtils.hasText(param.getChartCandidates())) return null;
        List<String> types = FiaChartTypeResolver.parse(param.getChartCandidates());
        List<CountSeries> result = new ArrayList<>();
        // 子集是否含计数数据
        boolean hasCount = subgroups.stream().anyMatch(s ->
                s.getNonconforming() != null || s.getDefectCount() != null);
        if (!hasCount) return null;
        for (String t : types) {
            if (!List.of("P", "NP", "C", "U").contains(t)) continue;
            CountSeries cs = switch (t) {
                case "P" -> buildP(subgroups);
                case "NP" -> buildNp(subgroups);
                case "C" -> buildC(subgroups);
                case "U" -> buildU(subgroups);
                default -> null;
            };
            if (cs != null) result.add(cs);
        }
        return result.isEmpty() ? null : result;
    }

    private CountSeries buildP(List<SpcSubgroup> subgroups) {
        BigDecimal sumNon = BigDecimal.ZERO, sumN = BigDecimal.ZERO;
        int k = 0;
        for (SpcSubgroup s : subgroups) {
            if (s.getNonconforming() == null || s.getInspectN() == null || s.getInspectN() == 0) continue;
            sumNon = sumNon.add(BigDecimal.valueOf(s.getNonconforming()));
            sumN = sumN.add(BigDecimal.valueOf(s.getInspectN()));
            k++;
        }
        if (k == 0 || sumN.signum() == 0) return null;
        BigDecimal pbar = sumNon.divide(sumN, 6, RoundingMode.HALF_UP);
        List<BigDecimal> vals = new ArrayList<>(), ucl = new ArrayList<>(), cl = new ArrayList<>(), lcl = new ArrayList<>();
        for (SpcSubgroup s : subgroups) {
            if (s.getNonconforming() == null || s.getInspectN() == null || s.getInspectN() == 0) {
                vals.add(null); ucl.add(null); cl.add(null); lcl.add(null); continue;
            }
            BigDecimal n = BigDecimal.valueOf(s.getInspectN());
            BigDecimal p = BigDecimal.valueOf(s.getNonconforming()).divide(n, 6, RoundingMode.HALF_UP);
            BigDecimal sd = sqrt(pbar.multiply(BigDecimal.ONE.subtract(pbar)).divide(n, 6, RoundingMode.HALF_UP));
            vals.add(p); cl.add(pbar);
            ucl.add(pbar.add(sd.multiply(THREE)));
            lcl.add(maxZero(pbar.subtract(sd.multiply(THREE))));
        }
        return series("P", vals, ucl, cl, lcl);
    }

    private CountSeries buildNp(List<SpcSubgroup> subgroups) {
        BigDecimal sumNon = BigDecimal.ZERO;
        int k = 0;
        for (SpcSubgroup s : subgroups) {
            if (s.getNonconforming() == null) continue;
            sumNon = sumNon.add(BigDecimal.valueOf(s.getNonconforming()));
            k++;
        }
        if (k == 0) return null;
        BigDecimal npbar = sumNon.divide(BigDecimal.valueOf(k), 6, RoundingMode.HALF_UP);
        BigDecimal sd = sqrt(npbar.multiply(BigDecimal.ONE.subtract(npbar.divide(BigDecimal.valueOf(k), 6, RoundingMode.HALF_UP))));
        List<BigDecimal> vals = new ArrayList<>(), ucl = new ArrayList<>(), cl = new ArrayList<>(), lcl = new ArrayList<>();
        for (SpcSubgroup s : subgroups) {
            if (s.getNonconforming() == null) { vals.add(null); ucl.add(null); cl.add(null); lcl.add(null); continue; }
            vals.add(BigDecimal.valueOf(s.getNonconforming()));
            cl.add(npbar); ucl.add(npbar.add(sd.multiply(THREE))); lcl.add(maxZero(npbar.subtract(sd.multiply(THREE))));
        }
        return series("NP", vals, ucl, cl, lcl);
    }

    private CountSeries buildC(List<SpcSubgroup> subgroups) {
        BigDecimal sumDef = BigDecimal.ZERO;
        int k = 0;
        for (SpcSubgroup s : subgroups) {
            if (s.getDefectCount() == null) continue;
            sumDef = sumDef.add(BigDecimal.valueOf(s.getDefectCount()));
            k++;
        }
        if (k == 0) return null;
        BigDecimal cbar = sumDef.divide(BigDecimal.valueOf(k), 6, RoundingMode.HALF_UP);
        BigDecimal sd = sqrt(cbar);
        List<BigDecimal> vals = new ArrayList<>(), ucl = new ArrayList<>(), cl = new ArrayList<>(), lcl = new ArrayList<>();
        for (SpcSubgroup s : subgroups) {
            if (s.getDefectCount() == null) { vals.add(null); ucl.add(null); cl.add(null); lcl.add(null); continue; }
            vals.add(BigDecimal.valueOf(s.getDefectCount()));
            cl.add(cbar); ucl.add(cbar.add(sd.multiply(THREE))); lcl.add(maxZero(cbar.subtract(sd.multiply(THREE))));
        }
        return series("C", vals, ucl, cl, lcl);
    }

    private CountSeries buildU(List<SpcSubgroup> subgroups) {
        BigDecimal sumDef = BigDecimal.ZERO, sumN = BigDecimal.ZERO;
        int k = 0;
        for (SpcSubgroup s : subgroups) {
            if (s.getDefectCount() == null || s.getInspectN() == null || s.getInspectN() == 0) continue;
            sumDef = sumDef.add(BigDecimal.valueOf(s.getDefectCount()));
            sumN = sumN.add(BigDecimal.valueOf(s.getInspectN()));
            k++;
        }
        if (k == 0 || sumN.signum() == 0) return null;
        BigDecimal ubar = sumDef.divide(sumN, 6, RoundingMode.HALF_UP);
        List<BigDecimal> vals = new ArrayList<>(), ucl = new ArrayList<>(), cl = new ArrayList<>(), lcl = new ArrayList<>();
        for (SpcSubgroup s : subgroups) {
            if (s.getDefectCount() == null || s.getInspectN() == null || s.getInspectN() == 0) {
                vals.add(null); ucl.add(null); cl.add(null); lcl.add(null); continue;
            }
            BigDecimal n = BigDecimal.valueOf(s.getInspectN());
            BigDecimal u = BigDecimal.valueOf(s.getDefectCount()).divide(n, 6, RoundingMode.HALF_UP);
            BigDecimal sd = sqrt(ubar.divide(n, 6, RoundingMode.HALF_UP));
            vals.add(u); cl.add(ubar);
            ucl.add(ubar.add(sd.multiply(THREE)));
            lcl.add(maxZero(ubar.subtract(sd.multiply(THREE))));
        }
        return series("U", vals, ucl, cl, lcl);
    }

    private CountSeries series(String type, List<BigDecimal> vals, List<BigDecimal> ucl, List<BigDecimal> cl, List<BigDecimal> lcl) {
        CountSeries cs = new CountSeries();
        cs.setChartType(type); cs.setValues(vals); cs.setUcl(ucl); cs.setCl(cl); cs.setLcl(lcl);
        return cs;
    }

    private static final BigDecimal THREE = BigDecimal.valueOf(3);

    private BigDecimal sqrt(BigDecimal v) {
        if (v.signum() < 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(Math.sqrt(v.doubleValue()));
    }

    private BigDecimal maxZero(BigDecimal v) {
        return v.signum() < 0 ? BigDecimal.ZERO : v;
    }

    @Override
    public SpcHistogramVo getHistogram(String paramId, String stage, String sampleTaskId) {        SpcHistogramVo vo = new SpcHistogramVo();
        vo.setBins(List.of());
        vo.setFreq(List.of());
        if (paramId == null || paramId.isBlank()) {
            return vo;
        }
        SpcParam param = spcParamMapper.selectById(paramId);
        if (param == null) {
            return vo;
        }
        LambdaQueryWrapper<SpcSubgroup> hw = new LambdaQueryWrapper<SpcSubgroup>()
                .eq(SpcSubgroup::getParamId, paramId)
                .orderByAsc(SpcSubgroup::getSubgroupTime);
        if (stage != null && !stage.isBlank() && !"ALL".equalsIgnoreCase(stage)) {
            hw.eq(SpcSubgroup::getStage, stage);
        }
        if (sampleTaskId != null && !sampleTaskId.isBlank()) {
            hw.eq(SpcSubgroup::getSampleTaskId, sampleTaskId);
        }
        List<SpcSubgroup> subgroups = spcSubgroupMapper.selectList(hw);
        // 直方图应基于"原始测量值"而非子组均值 xbar:个体值分布才能与规格限(USL/LSL)对齐,
        // 均值/整体标准差 σ 也据此计算(与能力分析 compute() 口径一致)。
        List<String> subgroupIds = subgroups.stream().map(SpcSubgroup::getId).filter(Objects::nonNull).toList();
        List<Double> values = subgroupIds.isEmpty() ? Collections.emptyList()
                : spcMeasurementMapper.selectList(new LambdaQueryWrapper<SpcMeasurement>()
                        .in(SpcMeasurement::getSubgroupId, subgroupIds))
                .stream().map(SpcMeasurement::getValue)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .toList();
        if (values.isEmpty()) {
            return vo;
        }
        // 均值与整体标准差 σ
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        // 样本数 < 2 时无法估计标准差(除以 0 会得 NaN,Jackson 会序列化为 "NaN" 字符串导致前端崩溃),置 null
        Double sigma = null;
        if (values.size() > 1) {
            double variance = values.stream().mapToDouble(v -> (v - mean) * (v - mean)).sum() / (values.size() - 1);
            sigma = Math.sqrt(variance);
        }
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        int binCount = 12;
        if (min == max || sigma == 0) {
            vo.setBins(List.of(mean));
            vo.setFreq(List.of((long) values.size()));
            vo.setNormalFreq(List.of((double) values.size()));
        } else {
            double width = (max - min) / binCount;
            List<Double> bins = new ArrayList<>();
            List<Long> freq = new ArrayList<>(Collections.nCopies(binCount, 0L));
            for (int i = 0; i < binCount; i++) {
                bins.add(min + (i + 0.5) * width);
            }
            for (double v : values) {
                int idx = (int) ((v - min) / width);
                if (idx >= binCount) idx = binCount - 1;
                if (idx < 0) idx = 0;
                freq.set(idx, freq.get(idx) + 1);
            }
            vo.setBins(bins);
            vo.setFreq(freq);
            // 正态拟合曲线(频次量级):normalFreq_i = pdf(bin_i) * total * binWidth
            double total = values.size();
            List<Double> normalFreq = new ArrayList<>();
            for (Double b : bins) {
                double pdf = Math.exp(-0.5 * Math.pow((b - mean) / sigma, 2)) / (sigma * Math.sqrt(2 * Math.PI));
                normalFreq.add(pdf * total * width);
            }
            vo.setNormalFreq(normalFreq);
        }
        vo.setMean(mean);
        vo.setSigma(sigma);
        vo.setUsl(param.getSpecUpper() != null ? param.getSpecUpper().doubleValue() : null);
        vo.setLsl(param.getSpecLower() != null ? param.getSpecLower().doubleValue() : null);
        return vo;
    }

    /** 独立事务创建子组:联动场景(FIA->SPC)失败时不回滚调用方主事务。 */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SpcSubgroup createInNewTx(SpcSubgroup subgroup, List<BigDecimal> values) {
        return create(subgroup, values);
    }

    @Override
    public CountCapabilityVo getCountCapability(String paramId) {
        CountCapabilityVo vo = new CountCapabilityVo();
        vo.setCountType(false);
        vo.setSampleCount(0);
        if (paramId == null || paramId.isBlank()) {
            return vo;
        }
        SpcParam param = spcParamMapper.selectById(paramId);
        if (param == null) {
            return vo;
        }
        // 判定是否为计数型:优先 dataType,否则解析 chartCandidates
        boolean count = false;
        String chartKind = null;
        if ("ATTRIBUTE".equals(param.getDataType())) {
            count = true;
            // 从 chartCandidates 推导主计数图类型(P/NP/C/U),供前端卡片按类型渲染
            if (StringUtils.hasText(param.getChartCandidates())) {
                List<String> cs = java.util.Arrays.stream(param.getChartCandidates().split(","))
                        .map(String::trim).filter(StringUtils::hasText).collect(java.util.stream.Collectors.toList());
                chartKind = cs.stream().filter(c -> List.of("P", "NP", "C", "U").contains(c)).findFirst().orElse(null);
            }
        } else if (!"VARIABLE".equals(param.getDataType()) && StringUtils.hasText(param.getChartCandidates())) {
            List<String> cs = java.util.Arrays.stream(param.getChartCandidates().split(","))
                    .map(String::trim).filter(StringUtils::hasText).collect(java.util.stream.Collectors.toList());
            count = cs.stream().anyMatch(c -> List.of("P", "NP", "C", "U").contains(c));
            if (count) chartKind = cs.stream().filter(c -> List.of("P", "NP", "C", "U").contains(c)).findFirst().orElse(null);
        }
        if (!count) {
            return vo;
        }
        vo.setCountType(true);
        vo.setChartKind(chartKind);

        // 聚合所有计数子组(非计数子组 nonconforming/defectCount 均为 null,自动忽略)
        List<SpcSubgroup> subs = spcSubgroupMapper.selectList(
                new LambdaQueryWrapper<SpcSubgroup>().eq(SpcSubgroup::getParamId, paramId));
        if (subs.isEmpty()) {
            return vo;
        }
        boolean isCU = "C".equals(chartKind) || "U".equals(chartKind);
        BigDecimal sumNon = BigDecimal.ZERO, sumN = BigDecimal.ZERO, sumDef = BigDecimal.ZERO;
        int countSub = 0;
        for (SpcSubgroup s : subs) {
            if (s.getNonconforming() != null || s.getDefectCount() != null) {
                countSub++;
                if (s.getNonconforming() != null) sumNon = sumNon.add(BigDecimal.valueOf(s.getNonconforming()));
                if (s.getDefectCount() != null) sumDef = sumDef.add(BigDecimal.valueOf(s.getDefectCount()));
                if (s.getInspectN() != null) sumN = sumN.add(BigDecimal.valueOf(s.getInspectN()));
            }
        }
        vo.setSampleCount(countSub);
        if (countSub == 0 || sumN.signum() == 0) {
            return vo;
        }
        if (isCU) {
            BigDecimal uBar = sumDef.divide(sumN, 6, RoundingMode.HALF_UP);
            vo.setUBar(uBar);
            vo.setDpu(uBar);
        } else {
            BigDecimal pBar = sumNon.divide(sumN, 6, RoundingMode.HALF_UP);
            BigDecimal ppm = pBar.multiply(BigDecimal.valueOf(1_000_000)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal yieldRate = BigDecimal.ONE.subtract(pBar).setScale(6, RoundingMode.HALF_UP);
            vo.setPBar(pBar);
            vo.setPpm(ppm);
            vo.setYieldRate(yieldRate);
        }
        return vo;
    }

    @Transactional
    public SpcSubgroup create(SpcSubgroup subgroup, List<BigDecimal> values) {
        SpcParam param = spcParamMapper.selectById(subgroup.getParamId());
        if (param == null) {
            throw new BusinessException(400, "SPC 参数不存在");
        }
        BigDecimal xbar = null; // 计量型子组均值;计数型子组保持 null
        // 兜底 orgId：集团管理员(dataScope=all)无归属 org 时，使用参数所属组织，
        // 避免 org_id 非空外键约束导致插入 500（前端集团总览视图 orgId 为空）。
        if (subgroup.getOrgId() == null || subgroup.getOrgId().isBlank()) {
            subgroup.setOrgId(param.getOrgId());
        }
        // 计数型子组(P/NP/C/U):values 为空但提供了 nonconforming/defectCount;
        // 计量型子组:values 必填。两者互斥, 否则 400。
        boolean countType = subgroup.getNonconforming() != null || subgroup.getDefectCount() != null;
        if ((values == null || values.isEmpty()) && !countType) {
            throw new BusinessException(400, "测量值不能为空(计量型)或未提供计数型字段(不合格数/缺陷数)");
        }

        subgroup.setSubgroupNo((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        if (subgroup.getSubgroupTime() == null) {
            subgroup.setSubgroupTime(LocalDateTime.now());
        }
        if (subgroup.getDataSource() == null || subgroup.getDataSource().isBlank()) {
            subgroup.setDataSource("manual");
        }
        // stage 兜底:未传则归为量产监控(ROUTINE),首件验证由 FIA 联动显式置 FIRST
        if (subgroup.getStage() == null || subgroup.getStage().isBlank()) {
            subgroup.setStage("ROUTINE");
        }
        subgroup.setOperatorId(currentOperator());
        subgroup.setCreatedAt(LocalDateTime.now());
        subgroup.setCreatedBy(currentOperator());

        String[] triggered = null;
        if (countType) {
            // 计数型:仅登记 nonconforming/inspectN/defectCount, 不计算 xbar/rangeR、不落 measurement;
            // 但同样做计数控制图(±3σ)判异, 超 UCL/LCL 时标记异常并建告警(SR-SPC-018)。
            subgroup.setN(subgroup.getInspectN() != null ? subgroup.getInspectN() : 1);
            triggered = checkCountRules(subgroup, param);
            if (triggered != null) {
                subgroup.setJudge("异常");
                subgroup.setIsOutlier(true);
                subgroup.setOutlierRule(triggered[0]);
            } else {
                subgroup.setJudge("正常");
                subgroup.setIsOutlier(false);
            }
        } else {
            // 计量型:计算 xbar / rangeR / stdDev
            subgroup.setN(values.size());
            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal max = values.get(0);
            BigDecimal min = values.get(0);
            for (BigDecimal v : values) {
                sum = sum.add(v);
                if (v.compareTo(max) > 0) max = v;
                if (v.compareTo(min) < 0) min = v;
            }
            xbar = sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
            BigDecimal rangeR = max.subtract(min);
            subgroup.setXbar(xbar);
            subgroup.setRangeR(rangeR);
            // 子组标准差(计量型,支持 Xbar-S 图的 S 图):总体标准差 std = sqrt(Σ(xi-xbar)²/n)
            if (values.size() > 1) {
                BigDecimal sqSum = BigDecimal.ZERO;
                for (BigDecimal v : values) {
                    BigDecimal d = v.subtract(xbar);
                    sqSum = sqSum.add(d.multiply(d));
                }
                BigDecimal variance = sqSum.divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
                subgroup.setStdDev(BigDecimal.valueOf(Math.sqrt(variance.doubleValue())));
            }

            // WECO 判异(插入前检查,避免分区表 update)
            triggered = checkWecRules(xbar, subgroup.getParamId(), param);
            if (triggered != null) {
                subgroup.setJudge("异常");
                subgroup.setIsOutlier(true);
                subgroup.setOutlierRule(triggered[0]);
            } else {
                subgroup.setJudge("正常");
                subgroup.setIsOutlier(false);
            }
        }

        spcSubgroupMapper.insert(subgroup);

        // 创建测量值(仅计量型子组落原始测量值,计数型无 values)
        if (values != null && !values.isEmpty()) {
        for (int i = 0; i < values.size(); i++) {
            SpcMeasurement m = new SpcMeasurement();
            m.setOrgId(subgroup.getOrgId());
            m.setSubgroupId(subgroup.getId());
            m.setSubgroupTime(subgroup.getSubgroupTime());
            m.setSeq(i + 1);
            m.setValue(values.get(i));
            spcMeasurementMapper.insert(m);
        }
        }

        // 命中规则 -> 生成告警(SR-SPC-018:30分钟内同参数已报过则抑制,仅控制图标记)
        if (triggered != null) {
            int suppressMin = 30;
            try {
                SpcGlobalConfig gc = spcGlobalConfigService.get(subgroup.getOrgId());
                if (gc != null && gc.getSuppressMinutes() != null) {
                    suppressMin = gc.getSuppressMinutes();
                }
            } catch (Exception ignored) {
            }
            LocalDateTime since = LocalDateTime.now().minusMinutes(suppressMin);
            // SR-SPC-018:仅"同级"重复抑制;级别升级(如预警->报警)视为新告警,不抑制
            Long recent = spcAlarmMapper.selectCount(
                    new LambdaQueryWrapper<SpcAlarm>()
                            .eq(SpcAlarm::getParamId, subgroup.getParamId())
                            .eq(SpcAlarm::getLevel, triggered[1])
                            .ge(SpcAlarm::getAlarmTime, since));
            if (recent != null && recent > 0) {
                // 抑制重复报警:不建新 alarm、不推送(子组已标记 is_outlier,控制图可见)
                log.info("[SPC抑制] 参数 {} 近 {} 分钟已报过同级({})告警,抑制重复报警(子组已标记触发点)",
                        param.getParamName(), suppressMin, triggered[1]);
            } else {
                // 首次/超出抑制窗 -> 建 alarm + 推送通知
                SpcAlarm alarm = new SpcAlarm();
                alarm.setOrgId(subgroup.getOrgId());
                alarm.setCode("AL-" + System.currentTimeMillis());
                alarm.setParamId(subgroup.getParamId());
                alarm.setParamName(param.getParamName());
                // 计量型落 xbar; 计数型 xbar 为 null, 改用当前计数点(不合格数/缺陷数)作为实测值
                alarm.setCurrentValue(xbar != null ? xbar : countCurrentPoint(subgroup));
                alarm.setTriggeredRule(triggered[0]);
                alarm.setLevel(triggered[1]);
                alarm.setAlarmTime(LocalDateTime.now());
                alarm.setStatus("待确认");
                alarm.setWoNo(subgroup.getWoNo());
                alarm.setBatchNo(subgroup.getBatchNo());
                spcAlarmMapper.insert(alarm);
                // 报警触发后推送通知(按启用渠道生成推送记录,失败不影响报警本身)
                try {
                    spcNotifyChannelService.send(alarm);
                } catch (Exception ignored) {
                    // 通知异常不回滚报警
                }
                // 回环联动:SPC 量产监控(ROUTINE)报警级异常 -> 停线整改后重新开工,自动触发新一轮首件检验
                if ("ROUTINE".equals(subgroup.getStage()) && "报警".equals(triggered[1])) {
                    try {
                        final BigDecimal xbarSnap = xbar;
                        final String[] trigSnap = triggered;
                        TransactionTemplate tt = new TransactionTemplate(transactionManager);
                        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                        tt.execute(status -> {
                            fiaTaskService.createFromSetup(
                                    subgroup.getOrgId(),
                                    subgroup.getWoNo(),
                                    subgroup.getProductCode(),
                                    param.getProcName(),
                                    subgroup.getProductCode(),
                                    null,
                                    "SPC 报警级异常(规则 " + trigSnap[0] + ",实测 " + xbarSnap + ")触发停线整改后自动重开首件");
                            return null;
                        });
                        log.info("[SPC回环] 量产监控报警级异常已触发首件重开: 参数={}, woNo={}, partNo={}",
                                param.getParamName(), subgroup.getWoNo(), subgroup.getProductCode());
                    } catch (Exception e) {
                        log.warn("[SPC回环] 自动创建首件任务失败(忽略): {}", e.getMessage());
                    }
                }
                // 站内信:SPC 报警推送给质量经理/SQE(失败不影响报警)
                try {
                    notificationService.notifyRoles(List.of("qmanager", "sqe"),
                        "SPC 控制图报警",
                        String.format("参数【%s】于 %s 触发 %s 级报警(规则:%s,实测值:%s)。请及时处理。",
                            param.getParamName(), alarm.getAlarmTime(), alarm.getLevel(),
                            alarm.getTriggeredRule(), alarm.getCurrentValue()),
                        "spc_alarm", alarm.getId(), alarm.getCode(), "/spc/alarms", null, alarm.getOrgId());
                } catch (Exception ignored) {
                    // 站内信异常不回滚报警
                }
            }
        }

        // 批次周期 CPK 自动滚动:新 batchNo 出现时,对刚结束的批次生成 CPK 快照(历史判定冻结)
        try {
            if ("批次".equals(param.getCpkPeriod()) && subgroup.getBatchNo() != null && !subgroup.getBatchNo().isBlank()) {
                SpcSubgroup prev = spcSubgroupMapper.selectOne(
                        new LambdaQueryWrapper<SpcSubgroup>()
                                .eq(SpcSubgroup::getParamId, param.getId())
                                .ne(SpcSubgroup::getId, subgroup.getId())
                                .orderByDesc(SpcSubgroup::getSubgroupTime)
                                .last("LIMIT 1"));
                if (prev != null && prev.getBatchNo() != null
                        && !prev.getBatchNo().equals(subgroup.getBatchNo())) {
                    spcCapabilityService.calc(param.getId(), "批次", prev.getBatchNo());
                }
            }
        } catch (Exception e) {
            log.warn("[SPC CPK快照] 批次边界快照计算失败(忽略): {}", e.getMessage());
        }

        // 回写采集任务:录入成功后刷新上次值/时间/下次到期/状态为"已采集",形成闭环
        try {
            spcCollectTaskService.recordCollected(
                    subgroup.getParamId(), subgroup.getOrgId(),
                    subgroup.getXbar(), subgroup.getSubgroupTime());
        } catch (Exception e) {
            log.warn("[SPC子组] 回写采集任务失败(忽略): {}", e.getMessage());
        }

        // 抽样任务归集:绑定 sampleTaskId 则 +1,录满目标数自动结案并触发 CPK 软告警
        try {
            if (subgroup.getSampleTaskId() != null && !subgroup.getSampleTaskId().isBlank()) {
                spcSampleTaskService.incCount(subgroup.getSampleTaskId());
            }
        } catch (Exception e) {
            log.warn("[SPC子组] 回写抽样任务计数失败(忽略): {}", e.getMessage());
        }

        return subgroup;
    }

    /**
     * WECO 判异检查(规则①-⑧,插入前调用)。
     * 返回 [ruleCode, level] 或 null。
     */
    private String[] checkWecRules(BigDecimal currentXbar, String paramId, SpcParam param) {
        // 查最近 14 个子组(不含当前,尚未插入);规则⑦需 14 历史+当前=15
        List<SpcSubgroup> recent = spcSubgroupMapper.selectList(
                new LambdaQueryWrapper<SpcSubgroup>()
                        .eq(SpcSubgroup::getParamId, paramId)
                        .orderByDesc(SpcSubgroup::getSubgroupTime)
                        .last("LIMIT 14"));
        if (recent.isEmpty()) {
            return null;
        }

        // 计算 CL / UCL / LCL(从历史子组)
        List<BigDecimal> xbars = recent.stream().map(SpcSubgroup::getXbar).filter(Objects::nonNull).toList();
        List<BigDecimal> ranges = recent.stream().map(SpcSubgroup::getRangeR).filter(Objects::nonNull).toList();
        if (xbars.isEmpty() || ranges.isEmpty()) {
            return null;
        }
        BigDecimal cl = avg(xbars);
        int n = param.getSubgroupSize() != null ? param.getSubgroupSize() : 5;
        double d2 = d2Factor(n);
        BigDecimal sigma = avg(ranges).divide(BigDecimal.valueOf(d2), 6, RoundingMode.HALF_UP);
        BigDecimal ucl = cl.add(sigma.multiply(BigDecimal.valueOf(3)));
        BigDecimal lcl = cl.subtract(sigma.multiply(BigDecimal.valueOf(3)));

        // 查启用的规则
        List<SpcRule> rules = spcRuleMapper.selectList(
                new LambdaQueryWrapper<SpcRule>().eq(SpcRule::getIsEnabled, true));

        // 按时间正序(oldest -> newest)排列历史子组
        List<SpcSubgroup> chrono = new ArrayList<>(recent);
        Collections.reverse(chrono);

        // SR-SPC-015:同时触发预警和报警时仅展示报警(高级别)。先记预警,遇报警立即返回。
        String[] winner = null;

        for (SpcRule rule : rules) {
            String code = rule.getRuleCode();
            boolean triggered = false;

            if ("①".equals(code)) {
                // 1点超出3σ
                triggered = currentXbar.compareTo(ucl) > 0 || currentXbar.compareTo(lcl) < 0;

            } else if ("②".equals(code)) {
                // 连续3点中2点在A区(2σ外同侧):2历史+当前=3
                BigDecimal sigma2 = cl.add(sigma.multiply(BigDecimal.valueOf(2)));
                BigDecimal sigma_2 = cl.subtract(sigma.multiply(BigDecimal.valueOf(2)));
                if (chrono.size() >= 2) {
                    int aboveCount = 0;
                    int belowCount = 0;
                    boolean valid = true;
                    for (int i = chrono.size() - 2; i < chrono.size(); i++) {
                        BigDecimal xb = chrono.get(i).getXbar();
                        if (xb == null) { valid = false; break; }
                        if (xb.compareTo(sigma2) > 0) aboveCount++;
                        else if (xb.compareTo(sigma_2) < 0) belowCount++;
                    }
                    if (valid) {
                        if (currentXbar.compareTo(sigma2) > 0) aboveCount++;
                        else if (currentXbar.compareTo(sigma_2) < 0) belowCount++;
                        triggered = aboveCount >= 2 || belowCount >= 2;
                    }
                }

            } else if ("③".equals(code)) {
                // 连续5点中4点在B区外(1σ外同侧):4历史+当前=5
                BigDecimal sigma1 = cl.add(sigma);
                BigDecimal sigma_1 = cl.subtract(sigma);
                if (chrono.size() >= 4) {
                    int aboveCount = 0;
                    int belowCount = 0;
                    boolean valid = true;
                    for (int i = chrono.size() - 4; i < chrono.size(); i++) {
                        BigDecimal xb = chrono.get(i).getXbar();
                        if (xb == null) { valid = false; break; }
                        if (xb.compareTo(sigma1) > 0) aboveCount++;
                        else if (xb.compareTo(sigma_1) < 0) belowCount++;
                    }
                    if (valid) {
                        if (currentXbar.compareTo(sigma1) > 0) aboveCount++;
                        else if (currentXbar.compareTo(sigma_1) < 0) belowCount++;
                        triggered = aboveCount >= 4 || belowCount >= 4;
                    }
                }

            } else if ("④".equals(code)) {
                // 连续8点在中心线一侧(7历史+当前=8)
                if (chrono.size() >= 7) {
                    boolean allAbove = currentXbar.compareTo(cl) > 0;
                    boolean allBelow = currentXbar.compareTo(cl) < 0;
                    for (int i = chrono.size() - 7; i < chrono.size(); i++) {
                        BigDecimal xb = chrono.get(i).getXbar();
                        if (xb == null) { allAbove = allBelow = false; break; }
                        if (xb.compareTo(cl) > 0) allBelow = false;
                        else if (xb.compareTo(cl) < 0) allAbove = false;
                        else { allAbove = allBelow = false; break; }
                    }
                    triggered = allAbove || allBelow;
                }

            } else if ("⑤".equals(code)) {
                // 连续6点递增或递减(5历史+当前=6)
                if (chrono.size() >= 5) {
                    List<BigDecimal> seq = new ArrayList<>();
                    for (int i = chrono.size() - 5; i < chrono.size(); i++) {
                        if (chrono.get(i).getXbar() != null) {
                            seq.add(chrono.get(i).getXbar());
                        }
                    }
                    seq.add(currentXbar);
                    if (seq.size() >= 6) {
                        boolean increasing = true;
                        boolean decreasing = true;
                        for (int i = 1; i < seq.size(); i++) {
                            if (seq.get(i).compareTo(seq.get(i - 1)) <= 0) increasing = false;
                            if (seq.get(i).compareTo(seq.get(i - 1)) >= 0) decreasing = false;
                        }
                        triggered = increasing || decreasing;
                    }
                }

            } else if ("⑥".equals(code)) {
                // 连续14点交替上下(13历史+当前=14)
                if (chrono.size() >= 13) {
                    List<BigDecimal> seq = new ArrayList<>();
                    boolean valid = true;
                    for (int i = chrono.size() - 13; i < chrono.size(); i++) {
                        BigDecimal xb = chrono.get(i).getXbar();
                        if (xb == null) { valid = false; break; }
                        seq.add(xb);
                    }
                    if (valid) {
                        seq.add(currentXbar);
                        boolean allAlternate = true;
                        Boolean prevUp = null;
                        for (int i = 1; i < seq.size(); i++) {
                            int cmp = seq.get(i).compareTo(seq.get(i - 1));
                            if (cmp == 0) { allAlternate = false; break; }
                            boolean up = cmp > 0;
                            if (prevUp != null && prevUp.equals(up)) { allAlternate = false; break; }
                            prevUp = up;
                        }
                        triggered = allAlternate;
                    }
                }

            } else if ("⑦".equals(code)) {
                // 连续15点在C区(1σ内,14历史+当前=15)
                if (chrono.size() >= 14) {
                    boolean allInC = true;
                    for (int i = chrono.size() - 14; i < chrono.size(); i++) {
                        BigDecimal xb = chrono.get(i).getXbar();
                        if (xb == null || xb.subtract(cl).abs().compareTo(sigma) >= 0) {
                            allInC = false;
                            break;
                        }
                    }
                    if (allInC && currentXbar.subtract(cl).abs().compareTo(sigma) < 0) {
                        triggered = true;
                    }
                }

            } else if ("⑧".equals(code)) {
                // 连续8点在B区外(1σ外,7历史+当前=8)
                if (chrono.size() >= 7) {
                    boolean allOutB = true;
                    for (int i = chrono.size() - 7; i < chrono.size(); i++) {
                        BigDecimal xb = chrono.get(i).getXbar();
                        if (xb == null || xb.subtract(cl).abs().compareTo(sigma) <= 0) {
                            allOutB = false;
                            break;
                        }
                    }
                    if (allOutB && currentXbar.subtract(cl).abs().compareTo(sigma) > 0) {
                        triggered = true;
                    }
                }
            }

            if (triggered) {
                // SR-SPC-015:报警(高级别)立即返回;预警仅记录,继续找报警
                if ("报警".equals(rule.getLevel())) {
                    return new String[]{code, rule.getLevel()};
                }
                if (winner == null) {
                    winner = new String[]{code, rule.getLevel()};
                }
            }
        }
        return winner;
    }

    /**
     * 计数型(P/NP/C/U)判异检查: 依据参数 chartCandidates 第一个计数图类型,
     * 聚合历史同类子组计算 CL ±3σ 控制限, 当前点超出 UCL/LCL 即判异(规则①, 报警级)。
     * 返回 [ruleCode, level] 或 null。要求至少 2 个历史同类子组, 避免样本不足误报。
     */
    private String[] checkCountRules(SpcSubgroup subgroup, SpcParam param) {
        if (param == null || !StringUtils.hasText(param.getChartCandidates())) {
            return null;
        }
        List<String> types = FiaChartTypeResolver.parse(param.getChartCandidates());
        // 取首个计数图类型作为判异基准(参数通常单一计数类)
        String primary = types.stream().filter(t -> List.of("P", "NP", "C", "U").contains(t)).findFirst().orElse(null);
        if (primary == null) {
            return null;
        }
        boolean cu = primary.equals("C") || primary.equals("U");

        // 聚合历史同类子组(不含当前, 尚未插入)
        List<SpcSubgroup> hist = spcSubgroupMapper.selectList(
                new LambdaQueryWrapper<SpcSubgroup>()
                        .eq(SpcSubgroup::getParamId, subgroup.getParamId())
                        .orderByDesc(SpcSubgroup::getSubgroupTime)
                        .last("LIMIT 30"));
        // 筛选同类计数子组
        List<SpcSubgroup> same = hist.stream().filter(s ->
                cu ? (s.getDefectCount() != null && s.getInspectN() != null && s.getInspectN() != 0)
                   : (s.getNonconforming() != null)).toList();
        if (same.size() < 2) {
            // 历史同类样本不足, 不判异(避免首点误报)
            return null;
        }

        // 计算 CL
        BigDecimal cl;
        if (cu) {
            BigDecimal sumDef = BigDecimal.ZERO, sumN = BigDecimal.ZERO;
            for (SpcSubgroup s : same) {
                sumDef = sumDef.add(BigDecimal.valueOf(s.getDefectCount()));
                sumN = sumN.add(BigDecimal.valueOf(s.getInspectN()));
            }
            if (sumN.signum() == 0) return null;
            cl = sumDef.divide(sumN, 6, RoundingMode.HALF_UP);
        } else {
            BigDecimal sumNon = BigDecimal.ZERO;
            for (SpcSubgroup s : same) sumNon = sumNon.add(BigDecimal.valueOf(s.getNonconforming()));
            cl = sumNon.divide(BigDecimal.valueOf(same.size()), 6, RoundingMode.HALF_UP);
        }

        // 计算当前点 + 当前子组控制限
        BigDecimal current;
        BigDecimal ucl;
        if (cu) {
            if (subgroup.getDefectCount() == null || subgroup.getInspectN() == null
                    || subgroup.getInspectN() == 0) {
                return null;
            }
            BigDecimal n = BigDecimal.valueOf(subgroup.getInspectN());
            current = BigDecimal.valueOf(subgroup.getDefectCount()).divide(n, 6, RoundingMode.HALF_UP);
            BigDecimal sd = sqrt(cl.divide(n, 6, RoundingMode.HALF_UP));
            ucl = cl.add(sd.multiply(THREE));
            BigDecimal lcl = maxZero(cl.subtract(sd.multiply(THREE)));
            if (current.compareTo(ucl) > 0 || current.compareTo(lcl) < 0) {
                return new String[]{"①", "报警"};
            }
        } else {
            if (subgroup.getNonconforming() == null) return null;
            current = BigDecimal.valueOf(subgroup.getNonconforming());
            if (primary.equals("P")) {
                if (subgroup.getInspectN() == null || subgroup.getInspectN() == 0) return null;
                BigDecimal n = BigDecimal.valueOf(subgroup.getInspectN());
                BigDecimal p = current.divide(n, 6, RoundingMode.HALF_UP);
                BigDecimal sd = sqrt(cl.multiply(BigDecimal.ONE.subtract(cl)).divide(n, 6, RoundingMode.HALF_UP));
                ucl = cl.add(sd.multiply(THREE));
                BigDecimal lcl = maxZero(cl.subtract(sd.multiply(THREE)));
                if (p.compareTo(ucl) > 0 || p.compareTo(lcl) < 0) {
                    return new String[]{"①", "报警"};
                }
            } else { // NP: 固定样本量, 控制限不随 n 变
                BigDecimal npbar = cl;
                BigDecimal sd = sqrt(npbar.multiply(BigDecimal.ONE.subtract(npbar.divide(BigDecimal.valueOf(same.size()), 6, RoundingMode.HALF_UP))));
                ucl = npbar.add(sd.multiply(THREE));
                BigDecimal lcl = maxZero(npbar.subtract(sd.multiply(THREE)));
                if (current.compareTo(ucl) > 0 || current.compareTo(lcl) < 0) {
                    return new String[]{"①", "报警"};
                }
            }
        }
        return null;
    }

    /**
     * 计数型子组当前点值(不合格数 / 缺陷数), 用于告警 currentValue。
     */
    private BigDecimal countCurrentPoint(SpcSubgroup subgroup) {
        if (subgroup.getDefectCount() != null) return BigDecimal.valueOf(subgroup.getDefectCount());
        if (subgroup.getNonconforming() != null) return BigDecimal.valueOf(subgroup.getNonconforming());
        return null;
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

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }
}
