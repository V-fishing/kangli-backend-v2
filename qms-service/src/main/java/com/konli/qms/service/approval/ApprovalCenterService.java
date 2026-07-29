package com.konli.qms.service.approval;

import com.konli.qms.common.dto.PendingApprovalDTO;

import java.util.List;

/**
 * 通用审批中心:聚合当前用户的待审批事项(FIA / NCM-8D / SQM 变更与审核)。
 */
public interface ApprovalCenterService {
    /**
     * 返回当前登录用户需要审批的事项列表(按申请时间倒序)。
     */
    List<PendingApprovalDTO> myPending();
}
