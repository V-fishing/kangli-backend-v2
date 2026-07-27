package com.konli.qms.service.fia;

import com.konli.qms.domain.fia.entity.FiaApproval;

import java.util.List;

/** 首件审批(豁免/紧急放行/让步接收) */
public interface FiaApprovalService {

    List<FiaApproval> list();

    FiaApproval get(String id);

    FiaApproval create(FiaApproval approval);

    /** 审批:approved=true->已通过, false->已驳回 */
    void approve(String id, String approverId, String opinion, boolean approved);

    /** 清理某任务下所有待审批审批单(重建处置前去重,避免孤儿/重复单) */
    void removePendingByTask(String taskId);
}
