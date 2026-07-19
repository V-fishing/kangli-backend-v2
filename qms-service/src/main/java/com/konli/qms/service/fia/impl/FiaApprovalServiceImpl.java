package com.konli.qms.service.fia.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaApproval;
import com.konli.qms.domain.fia.mapper.FiaApprovalMapper;
import com.konli.qms.service.fia.FiaApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FiaApprovalServiceImpl implements FiaApprovalService {

    private final FiaApprovalMapper fiaApprovalMapper;

    @Override
    public List<FiaApproval> list() {
        return fiaApprovalMapper.selectList(null);
    }

    @Override
    public FiaApproval get(String id) {
        return fiaApprovalMapper.selectById(id);
    }

    @Override
    @Transactional
    public FiaApproval create(FiaApproval approval) {
        if (approval.getCode() == null) {
            approval.setCode("AP-" + System.currentTimeMillis());
        }
        if (approval.getStatus() == null) {
            approval.setStatus("待审批");
        }
        if (approval.getApplicantId() == null) {
            approval.setApplicantId(currentOperator());
        }
        approval.setApplyAt(LocalDateTime.now());
        fiaApprovalMapper.insert(approval);
        return approval;
    }

    @Override
    @Transactional
    public void approve(String id, String approverId, String opinion, boolean approved) {
        FiaApproval approval = fiaApprovalMapper.selectById(id);
        if (approval == null) {
            throw new BusinessException(400, "审批单不存在");
        }
        approval.setApproverId(approverId);
        approval.setApproveOpinion(opinion);
        approval.setApproveAt(LocalDateTime.now());
        approval.setStatus(approved ? "已通过" : "已驳回");
        fiaApprovalMapper.updateById(approval);
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }
}
