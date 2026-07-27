package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcCollectTask;

import java.util.List;

/** SPC 采集任务(计划采集频率/计划停机)。spc.subgroup.* */
public interface SpcCollectTaskService {

    List<SpcCollectTask> list();

    SpcCollectTask create(SpcCollectTask task);

    void markDowntime(String id, Boolean isPlannedDowntime, String reason);

    /** SR-SPC-003:标记采集缺失 -> 置 status='缺失' + 通知班组长(计划停产不告警)。 */
    void markMissing(String id, String reason);

    /** SR-SPC-003:扫描到期未录入且未停产的采集任务,自动标记缺失并告警。返回处理条数。 */
    int scanOverdueMissing();
}
