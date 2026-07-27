package com.konli.qms.bootstrap.schedule;

import com.konli.qms.service.sqm.SqmAbnormalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 重复问题自动升级定时任务。
 * <p>
 * 每天凌晨 3:00 扫描近 30 天 sqm_incoming_abnormal,
 * 对同一供应商+物料出现 >=2 次异常的自动:
 * <ol>
 *   <li>创建 sqm_supplier_escalation 升级记录(观察中/升级中)</li>
 *   <li>自动增加审核频次(suggestedAction=增加审核频次)</li>
 *   <li>观察中供应商自动降低采购份额(5% 下限)</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RepeatEscalationJob {

    private final SqmAbnormalService sqmAbnormalService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void scanRepeatEscalation() {
        log.info("=== 定时任务启动: 重复问题自动升级 ===");
        try {
            sqmAbnormalService.checkRepeatEscalation();
            log.info("=== 定时任务完成: 重复问题自动升级 ===");
        } catch (Exception e) {
            log.error("定时任务异常(不阻断): {}", e.getMessage(), e);
        }
    }
}
