package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaApproval;
import com.konli.qms.domain.fia.mapper.FiaApprovalMapper;
import com.konli.qms.service.fia.FiaApprovalService;
import com.konli.qms.service.fia.FiaTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FiaApprovalServiceImpl implements FiaApprovalService {

    private final FiaApprovalMapper fiaApprovalMapper;
    private final ApplicationContext applicationContext;

    @Override
    public List<FiaApproval> list(String approvalType, String status, String keyword) {
        // 组织隔离:超管(dataScope=all)全量;普通用户仅见本组织数据 + org_id 为 NULL 的全局数据
        LambdaQueryWrapper<FiaApproval> qw = new LambdaQueryWrapper<>();
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u != null && !"all".equals(u.dataScope())) {
            String switchOrg = CompanyContext.getSwitchOrgId();
            final String effOrg = (switchOrg != null && !switchOrg.isBlank()) ? switchOrg : u.orgId();
            if (effOrg != null && !effOrg.isBlank()) {
                qw.and(w -> w.eq(FiaApproval::getOrgId, effOrg).or().isNull(FiaApproval::getOrgId));
            }
        }
        if (approvalType != null && !approvalType.isBlank()) {
            qw.eq(FiaApproval::getApprovalType, approvalType);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(FiaApproval::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(FiaApproval::getCode, keyword.trim());
        }
        qw.orderByDesc(FiaApproval::getApplyAt);
        return fiaApprovalMapper.selectList(qw);
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
        // 联动首件任务:通过=放行(归档+写SPC基准);驳回=不放行
        String taskId = approval.getTaskId();
        if (taskId != null && !taskId.isBlank()) {
            FiaTaskService fiaTaskService = applicationContext.getBean(FiaTaskService.class);
            if (approved) {
                fiaTaskService.releaseAfterApproval(taskId);
            } else {
                fiaTaskService.rejectTask(taskId);
            }
        }
    }

    @Override
    @Transactional
    public void removePendingByTask(String taskId) {
        fiaApprovalMapper.delete(
                new LambdaQueryWrapper<>(FiaApproval.class)
                        .eq(FiaApproval::getTaskId, taskId)
                        .eq(FiaApproval::getStatus, "待审批"));
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }
}
