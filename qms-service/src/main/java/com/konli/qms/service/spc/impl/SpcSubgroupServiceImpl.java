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
import com.konli.qms.service.spc.SpcNotifyChannelService;
import com.konli.qms.service.spc.SpcSubgroupService;
import com.konli.qms.service.spc.SpcGlobalConfigService;
import com.konli.qms.service.spc.SpcCapabilityService;
import com.konli.qms.service.spc.SpcCollectTaskService;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.spc.dto.ControlChartMark;
import com.konli.qms.service.spc.dto.ControlChartVo;
import com.konli.qms.service.spc.dto.SpcHistogramVo;
import com.konli.qms.service.spc.dto.SpcSubgroupVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class SpcSubgroupServiceImpl implements SpcSubgroupService {

    private final SpcSubgroupMapper spcSubgroupMapper;
    private final SpcParamMapper spcParamMapper;
    private final SpcMeasurementMapper spcMeasurementMapper;
    private final SpcRuleMapper spcRuleMapper;
    private final SpcAlarmMapper spcAlarmMapper;
    private final SpcControlLimitMapper spcControlLimitMapper;
    private final SpcNotifyChannelService spcNotifyChannelService;
    private final SpcGlobalConfigService spcGlobalConfigService;
    private final SpcCapabilityService spcCapabilityService;
    private final SpcCollectTaskService spcCollectTaskService;
    private final NotificationService notificationService;

    @Override
    public List<SpcSubgroup> list() {
        return spcSubgroupMapper.selectList(null);
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
    public ControlChartVo getControlChart(String paramId, String startTime, String endTime) {
        if (paramId == null || paramId.isBlank()) {
            throw new BusinessException(400, "paramId 不能为空");
        }
        LambdaQueryWrapper<SpcSubgroup> w = new LambdaQueryWrapper<SpcSubgroup>()
                .eq(SpcSubgroup::getParamId, paramId)
                .orderByAsc(SpcSubgroup::getSubgroupTime);
        if (startTime != null && !startTime.isBlank()) {
            w.ge(SpcSubgroup::getSubgroupTime, LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isBlank()) {
            w.le(SpcSubgroup::getSubgroupTime, LocalDateTime.parse(endTime));
        }
        List<SpcSubgroup> subgroups = spcSubgroupMapper.selectList(w);

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
        return vo;
    }

    @Override
    public SpcHistogramVo getHistogram(String paramId) {
        SpcHistogramVo vo = new SpcHistogramVo();
        vo.setBins(List.of());
        vo.setFreq(List.of());
        if (paramId == null || paramId.isBlank()) {
            return vo;
        }
        SpcParam param = spcParamMapper.selectById(paramId);
        if (param == null) {
            return vo;
        }
        List<SpcSubgroup> subgroups = spcSubgroupMapper.selectList(
                new LambdaQueryWrapper<SpcSubgroup>()
                        .eq(SpcSubgroup::getParamId, paramId)
                        .orderByAsc(SpcSubgroup::getSubgroupTime));
        List<Double> values = subgroups.stream()
                .map(SpcSubgroup::getXbar)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .toList();
        if (values.isEmpty()) {
            return vo;
        }
        // 均值与整体标准差 σ
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream().mapToDouble(v -> (v - mean) * (v - mean)).sum() / (values.size() - 1);
        double sigma = Math.sqrt(variance);
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

    @Transactional
    public SpcSubgroup create(SpcSubgroup subgroup, List<BigDecimal> values) {
        SpcParam param = spcParamMapper.selectById(subgroup.getParamId());
        if (param == null) {
            throw new BusinessException(400, "SPC 参数不存在");
        }
        // 兜底 orgId：集团管理员(dataScope=all)无归属 org 时，使用参数所属组织，
        // 避免 org_id 非空外键约束导致插入 500（前端集团总览视图 orgId 为空）。
        if (subgroup.getOrgId() == null || subgroup.getOrgId().isBlank()) {
            subgroup.setOrgId(param.getOrgId());
        }
        if (values == null || values.isEmpty()) {
            throw new BusinessException(400, "测量值不能为空");
        }

        subgroup.setSubgroupNo((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        if (subgroup.getSubgroupTime() == null) {
            subgroup.setSubgroupTime(LocalDateTime.now());
        }
        subgroup.setN(values.size());

        // 计算 xbar / rangeR
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = values.get(0);
        BigDecimal min = values.get(0);
        for (BigDecimal v : values) {
            sum = sum.add(v);
            if (v.compareTo(max) > 0) max = v;
            if (v.compareTo(min) < 0) min = v;
        }
        BigDecimal xbar = sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
        BigDecimal rangeR = max.subtract(min);
        subgroup.setXbar(xbar);
        subgroup.setRangeR(rangeR);
        if (subgroup.getDataSource() == null || subgroup.getDataSource().isBlank()) {
            subgroup.setDataSource("manual");
        }
        subgroup.setOperatorId(currentOperator());
        subgroup.setCreatedAt(LocalDateTime.now());
        subgroup.setCreatedBy(currentOperator());

        // WECO 判异(插入前检查,避免分区表 update)
        String[] triggered = checkWecRules(xbar, subgroup.getParamId(), param);
        if (triggered != null) {
            subgroup.setJudge("异常");
            subgroup.setIsOutlier(true);
            subgroup.setOutlierRule(triggered[0]);
        } else {
            subgroup.setJudge("正常");
            subgroup.setIsOutlier(false);
        }

        spcSubgroupMapper.insert(subgroup);

        // 创建测量值
        for (int i = 0; i < values.size(); i++) {
            SpcMeasurement m = new SpcMeasurement();
            m.setOrgId(subgroup.getOrgId());
            m.setSubgroupId(subgroup.getId());
            m.setSubgroupTime(subgroup.getSubgroupTime());
            m.setSeq(i + 1);
            m.setValue(values.get(i));
            spcMeasurementMapper.insert(m);
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
                alarm.setCurrentValue(xbar);
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
                // 站内信:SPC 报警推送给质量经理/SQE(失败不影响报警)
                try {
                    notificationService.notifyRoles(List.of("qmanager", "sqe"),
                        "SPC 控制图报警",
                        String.format("参数【%s】于 %s 触发 %s 级报警(规则:%s,实测值:%s)。请及时处理。",
                            param.getParamName(), alarm.getAlarmTime(), alarm.getLevel(),
                            alarm.getTriggeredRule(), alarm.getCurrentValue()),
                        "spc_alarm", alarm.getId(), "/spc/alarms", null);
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
