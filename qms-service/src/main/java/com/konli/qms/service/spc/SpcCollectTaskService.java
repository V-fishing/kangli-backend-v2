package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcCollectTask;

import java.util.List;

/** SPC 采集任务(计划采集频率/计划停机)。spc.subgroup.* */
public interface SpcCollectTaskService {

    List<SpcCollectTask> list();

    SpcCollectTask create(SpcCollectTask task);

    void markDowntime(String id, Boolean isPlannedDowntime, String reason);
}
