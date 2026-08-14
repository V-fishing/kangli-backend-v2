package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcSampleTask;

import java.util.List;

/** SPC 抽样批次任务(量产监控采集载体)。spc.sample-task.* */
public interface SpcSampleTaskService {

    /** 新建抽样任务:工单×料号×工序自动带出 SpcParam,默认采集中/currentCount=0/released=false。 */
    SpcSampleTask create(String orgId, String woNo, String partNo, String procName,
                         String productName, Integer targetCount, String operatorId);

    /**
     * 批量新建抽样任务:同一 woNo(产品)下挂多个参数。
     * paramIds 非空时按勾选参数逐一建任务(共享 woNo/partNo/procName/productName);
     * paramIds 为空时回退单参数自动匹配(等价于 create)。
     * 返回本次创建的全部任务列表。
     */
    List<SpcSampleTask> createBatch(String orgId, String woNo, String partNo, String procName,
                                    String productName, Integer targetCount, List<String> paramIds,
                                    List<String> fiaStdItemIds,
                                    String triggerType, String category, String supplierId,
                                    String supplierName, Boolean isUrgent, String remark, String operatorId);

    /** 任务列表(按组织/工单/料号/状态过滤)。 */
    List<SpcSampleTask> list(String orgId, String woNo, String partNo, String status);

    /** 单任务详情(采集页用)。 */
    SpcSampleTask get(String id);

    /** 每提交一个子组 +1;达 targetCount 自动结案并触发 CPK 软告警。返回最新任务。 */
    SpcSampleTask incCount(String taskId);

    /** 算该任务下 ROUTINE 子组 CPK,跌破门槛写告警 + 标记(软告警,不卡停产)。 */
    void calcAndSetReleased(String taskId);

    /** 标准自动带出:工单×料号×工序 → SpcParam(paramId);带出失败抛异常。 */
    String resolveParamId(String orgId, String partNo, String procName);
}
