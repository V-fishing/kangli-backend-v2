package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.spc.entity.SpcAlarm;
import com.konli.qms.domain.spc.entity.SpcCapability;
import com.konli.qms.domain.spc.entity.SpcCollectTask;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcAlarmMapper;
import com.konli.qms.domain.spc.mapper.SpcCapabilityMapper;
import com.konli.qms.domain.spc.mapper.SpcCollectTaskMapper;
import com.konli.qms.domain.spc.mapper.SpcSubgroupMapper;
import com.konli.qms.service.spc.SpcDashboardService;
import com.konli.qms.service.spc.dto.SpcDashboardVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SPC 看板:聚合多表统计(Cpk 分布 / 待确认告警 / 今日采集完成率)。
 * 注入 SpcCapabilityMapper + SpcAlarmMapper + SpcSubgroupMapper + SpcCollectTaskMapper。
 */
@Service
@RequiredArgsConstructor
public class SpcDashboardServiceImpl implements SpcDashboardService {

    private final SpcCapabilityMapper spcCapabilityMapper;
    private final SpcAlarmMapper spcAlarmMapper;
    private final SpcSubgroupMapper spcSubgroupMapper;
    private final SpcCollectTaskMapper spcCollectTaskMapper;

    @Override
    public SpcDashboardVo dashboard() {
        // 1. 最新一条 capability per paramId(按 calcAt 倒序,putIfAbsent 保留最新),统计 level 分布
        List<SpcCapability> all = spcCapabilityMapper.selectList(
                new LambdaQueryWrapper<SpcCapability>()
                        .orderByDesc(SpcCapability::getCalcAt));
        Map<String, SpcCapability> latestPerParam = new LinkedHashMap<>();
        for (SpcCapability cap : all) {
            if (cap.getParamId() == null) {
                continue;
            }
            latestPerParam.putIfAbsent(cap.getParamId(), cap);
        }
        Map<String, Long> cpkDistribution = new LinkedHashMap<>();
        cpkDistribution.put("充足", 0L);
        cpkDistribution.put("尚可", 0L);
        cpkDistribution.put("不足", 0L);
        for (SpcCapability cap : latestPerParam.values()) {
            String level = cap.getLevel();
            if (level == null || !cpkDistribution.containsKey(level)) {
                level = "不足";
            }
            cpkDistribution.merge(level, 1L, Long::sum);
        }

        // 2. 待确认告警数
        Long pendingAlarms = spcAlarmMapper.selectCount(
                new LambdaQueryWrapper<SpcAlarm>().eq(SpcAlarm::getStatus, "待确认"));

        // 3. 今日子组数(完成) vs 今日到期任务数(应完成,nextDueAt < 今日)
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        Long todaySubgroups = spcSubgroupMapper.selectCount(
                new LambdaQueryWrapper<SpcSubgroup>()
                        .ge(SpcSubgroup::getSubgroupTime, startOfToday));
        Long todayDue = spcCollectTaskMapper.selectCount(
                new LambdaQueryWrapper<SpcCollectTask>()
                        .lt(SpcCollectTask::getNextDueAt, startOfToday));

        SpcDashboardVo vo = new SpcDashboardVo();
        vo.setCpkDistribution(cpkDistribution);
        vo.setPendingAlarms(pendingAlarms);
        vo.setTodaySubgroups(todaySubgroups);
        vo.setTodayDue(todayDue);
        return vo;
    }
}
