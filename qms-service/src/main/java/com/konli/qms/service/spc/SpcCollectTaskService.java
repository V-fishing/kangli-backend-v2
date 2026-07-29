package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcCollectTask;

import java.util.List;

/** SPC 采集任务(计划采集频率/计划停机)。spc.subgroup.* */
public interface SpcCollectTaskService {

    List<SpcCollectTask> list();

    SpcCollectTask create(SpcCollectTask task);

    /** 编辑采集任务:更新采集频率/采集模式/计划停机等可变字段。 */
    void update(SpcCollectTask task);

    /** 软删采集任务(is_deleted=true)。 */
    void delete(String id);

    void markDowntime(String id, Boolean isPlannedDowntime, String reason);

    /** SR-SPC-003:标记采集缺失 -> 置 status='缺失' + 通知班组长(计划停产不告警)。 */
    void markMissing(String id, String reason);

    /** SR-SPC-003:扫描到期未录入且未停产的采集任务,自动标记缺失并告警。返回处理条数。 */
    int scanOverdueMissing();

    /** SR-SPC-003(闭环):子组录入后回写采集任务(上次值/时间/下次到期/状态),按 param_id+org_id 全部更新。 */
    void recordCollected(String paramId, String orgId, java.math.BigDecimal lastValue, java.time.LocalDateTime lastAt);

    /** 扫描临期(距 next_due_at < 30min 且仍待采集)的采集任务并发临期提醒。返回处理条数。 */
    int scanDueSoon();
}
