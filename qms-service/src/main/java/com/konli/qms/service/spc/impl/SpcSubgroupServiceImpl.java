package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.spc.entity.SpcAlarm;
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
import com.konli.qms.service.spc.SpcSubgroupService;
import com.konli.qms.service.spc.dto.ControlChartVo;
import com.konli.qms.service.spc.dto.SpcSubgroupVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SpcSubgroupServiceImpl implements SpcSubgroupService {

    private final SpcSubgroupMapper spcSubgroupMapper;
    private final SpcParamMapper spcParamMapper;
    private final SpcMeasurementMapper spcMeasurementMapper;
    private final SpcRuleMapper spcRuleMapper;
    private final SpcAlarmMapper spcAlarmMapper;
    private final SpcControlLimitMapper spcControlLimitMapper;

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

        // 当前激活控制限(无则返回 null,前端自适应)
        SpcControlLimit limit = spcControlLimitMapper.selectOne(
                new LambdaQueryWrapper<SpcControlLimit>()
                        .eq(SpcControlLimit::getParamId, paramId)
                        .eq(SpcControlLimit::getIsActive, true)
                        .last("LIMIT 1"));

        ControlChartVo vo = new ControlChartVo();
        vo.setSubgroups(subgroups);
        vo.setLimit(limit);
        return vo;
    }

    @Override
    @Transactional
    public SpcSubgroup create(SpcSubgroup subgroup, List<BigDecimal> values) {
        SpcParam param = spcParamMapper.selectById(subgroup.getParamId());
        if (param == null) {
            throw new BusinessException(400, "SPC 参数不存在");
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

        // 命中规则 -> 生成告警
        if (triggered != null) {
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
                return new String[]{code, rule.getLevel()};
            }
        }
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
