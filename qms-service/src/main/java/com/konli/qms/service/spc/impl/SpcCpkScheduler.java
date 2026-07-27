package com.konli.qms.service.spc.impl;

import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.service.spc.SpcCapabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/** CPK 自动滚动更新调度:按参数 cpkPeriod(日/周) 周期性生成冻结快照,历史判定不随规格变更改变。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpcCpkScheduler {

    private final SpcParamMapper spcParamMapper;
    private final SpcCapabilityService spcCapabilityService;

    /** 每日 23:55 对 cpkPeriod=日/周 的参数滚动生成 CPK/PPK 快照(幂等 upsert)。 */
    @Scheduled(cron = "0 55 23 * * ?")
    public void rollCpkSnapshots() {
        List<SpcParam> params = spcParamMapper.selectList(null);
        if (params == null || params.isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now();
        for (SpcParam p : params) {
            String period = p.getCpkPeriod();
            if (period == null || (!"日".equals(period) && !"周".equals(period))) {
                continue;
            }
            try {
                if ("日".equals(period)) {
                    spcCapabilityService.calc(p.getId(), "日", today.toString());
                } else {
                    // 周周期:以本周一日期作为周期标识,保证每周一行冻结快照
                    LocalDate monday = today.with(DayOfWeek.MONDAY);
                    spcCapabilityService.calc(p.getId(), "周", monday.toString());
                }
            } catch (Exception e) {
                log.warn("[SPC CPK快照] 参数 {} 周期快照失败(跳过): {}", p.getParamName(), e.getMessage());
            }
        }
    }
}
