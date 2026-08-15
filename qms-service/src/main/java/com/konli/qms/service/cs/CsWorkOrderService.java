package com.konli.qms.service.cs;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.cs.entity.CsWorkOrder;

import java.util.List;
import java.util.Map;

/** 售后工单流程控制。cs.workorder.* */
public interface CsWorkOrderService {

    /** 分页查询(支持 关键词/类型/状态/优先级 过滤, 组织隔离由 DataScopeInterceptor 处理)。 */
    PageResult<CsWorkOrder> page(String keyword, String woType, String status,
                                 String priority, int page, int size);

    CsWorkOrder get(String id);

    CsWorkOrder create(CsWorkOrder order);

    CsWorkOrder update(CsWorkOrder order);

    void delete(String id);

    /** 派单: PENDING -> ASSIGNED, 写 owner/assignAt。responsibleId 为负责人 user id。 */
    void assign(String id, String responsibleId, String responsibleName);

    /** 标记完成(填写处理过程): ASSIGNED -> DONE, 写 handleDetail/handleAt。 */
    void complete(String id, String handleDetail);

    /** 评价闭环: DONE -> CLOSED, 写 satisfaction/satisfactionComment/closeAt。 */
    void close(String id, Integer satisfaction, String satisfactionComment);

    /** 看板统计: 各状态数量 + 紧急待派单数。 */
    Map<String, Object> dashboard();

    /** 满意度统计(基于已闭环工单的 satisfaction 评分): 平均分/已评数/分布/月度趋势。 */
    Map<String, Object> satisfactionStats();
}
