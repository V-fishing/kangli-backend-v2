package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.mapper.FiaInspStdMapper;
import com.konli.qms.domain.sqm.entity.SqmChangeApproval;
import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import com.konli.qms.domain.sqm.mapper.SqmChangeApprovalMapper;
import com.konli.qms.domain.sqm.mapper.SqmChangeOrderMapper;
import com.konli.qms.service.sqm.SqmChangeService;
import com.konli.qms.service.sqm.dto.SqmChangeOrderVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqmChangeServiceImpl implements SqmChangeService {

    private final SqmChangeOrderMapper sqmChangeOrderMapper;
    private final SqmChangeApprovalMapper sqmChangeApprovalMapper;
    private final FiaInspStdMapper fiaInspStdMapper;

    @Override
    public List<SqmChangeOrder> list() {
        return sqmChangeOrderMapper.selectList(null);
    }

    @Override
    public SqmChangeOrderVo get(String id) {
        SqmChangeOrder order = sqmChangeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "变更单不存在");
        }
        SqmChangeOrderVo vo = new SqmChangeOrderVo();
        vo.setOrder(order);
        vo.setApprovals(sqmChangeApprovalMapper.selectList(
                new LambdaQueryWrapper<SqmChangeApproval>()
                        .eq(SqmChangeApproval::getChangeId, id)
                        .orderByAsc(SqmChangeApproval::getSeqOrder)));
        return vo;
    }

    @Override
    @Transactional
    public SqmChangeOrder create(SqmChangeOrder order) {
        order.setChangeNo("ECN-" + System.currentTimeMillis());
        order.setStatus("待申请");
        sqmChangeOrderMapper.insert(order);
        // 预建三方并行会签记录(quality/purchase/rd)
        String[][] roles = {{"quality", "质量"}, {"purchase", "采购"}, {"rd", "研发"}};
        for (String[] role : roles) {
            SqmChangeApproval ap = new SqmChangeApproval();
            ap.setOrgId(order.getOrgId());
            ap.setChangeId(order.getId());
            ap.setApprovalRole(role[0]);
            ap.setRoleLabel(role[1]);
            ap.setStatus("pending");
            ap.setHasVeto("quality".equals(role[0]));
            ap.setSeqOrder(0);
            sqmChangeApprovalMapper.insert(ap);
        }
        return order;
    }

    @Override
    @Transactional
    public void submit(String id) {
        SqmChangeOrder order = sqmChangeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "变更单不存在");
        }
        if (!"待申请".equals(order.getStatus())) {
            throw new BusinessException(400, "变更单当前状态为 " + order.getStatus() + ",无法提交");
        }
        SqmChangeOrder upd = new SqmChangeOrder();
        upd.setId(id);
        upd.setStatus("审批中");
        sqmChangeOrderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void approve(String id, String approvalRole, boolean approved, String opinion) {
        SqmChangeOrder order = sqmChangeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "变更单不存在");
        }
        if (!"审批中".equals(order.getStatus())) {
            throw new BusinessException(400, "变更单当前状态为 " + order.getStatus() + ",无法审批");
        }
        // 查当前角色的会签记录
        SqmChangeApproval approval = sqmChangeApprovalMapper.selectOne(
                new LambdaQueryWrapper<SqmChangeApproval>()
                        .eq(SqmChangeApproval::getChangeId, id)
                        .eq(SqmChangeApproval::getApprovalRole, approvalRole));
        if (approval == null) {
            throw new BusinessException(404, "未找到该角色的会签记录:" + approvalRole);
        }

        // 更新会签记录
        approval.setStatus(approved ? "done" : "rejected");
        approval.setOperator(currentOperator());
        approval.setOperateDate(LocalDateTime.now());
        approval.setOpinion(opinion);
        sqmChangeApprovalMapper.updateById(approval);

        // 重新查所有会签,判断整体结论
        List<SqmChangeApproval> all = sqmChangeApprovalMapper.selectList(
                new LambdaQueryWrapper<SqmChangeApproval>()
                        .eq(SqmChangeApproval::getChangeId, id));

        boolean anyRejectedWithVeto = all.stream()
                .anyMatch(a -> "rejected".equals(a.getStatus()) && Boolean.TRUE.equals(a.getHasVeto()));
        boolean allDone = all.stream().allMatch(a -> "done".equals(a.getStatus()));

        SqmChangeOrder upd = new SqmChangeOrder();
        upd.setId(id);
        if (anyRejectedWithVeto) {
            // 任一 rejected 且 hasVeto=true -> 已驳回
            upd.setStatus("已驳回");
            sqmChangeOrderMapper.updateById(upd);
        } else if (allDone) {
            // 全部 done -> 已批准
            upd.setStatus("已批准");
            sqmChangeOrderMapper.updateById(upd);
            // 变更批准后联动 FIA 检验标准(旧标准停用 + 新版本草稿,待质量审核后生效)
            syncFiaStd(order);
        }
        // 其余情况(任一 rejected 且 hasVeto=false 或仍在等待)继续等其他角色,order.status 保持"审批中"
    }

    @Override
    @Transactional
    public void close(String id) {
        SqmChangeOrder order = sqmChangeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "变更单不存在");
        }
        SqmChangeOrder upd = new SqmChangeOrder();
        upd.setId(id);
        upd.setStatus("已关闭");
        sqmChangeOrderMapper.updateById(upd);
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }

    /**
     * 变更批准后联动 FIA 检验标准:
     * 匹配 material = partNo 且 status='生效' 的标准,旧标准置为'停用',同时新建一条草稿版本
     * (std_version 递增 v2->v3,prev_version_id 指向旧 id),待质量审核后生效。
     * 异常不阻断主流程。
     */
    private void syncFiaStd(SqmChangeOrder order) {
        try {
            if (order.getPartNo() == null || order.getPartNo().isBlank()) {
                return;
            }
            List<FiaInspStd> stds = fiaInspStdMapper.selectList(
                    new LambdaQueryWrapper<FiaInspStd>()
                            .eq(FiaInspStd::getMaterial, order.getPartNo())
                            .eq(FiaInspStd::getStatus, "生效"));
            for (FiaInspStd old : stds) {
                // 旧标准停用(软更新)
                FiaInspStd upd = new FiaInspStd();
                upd.setId(old.getId());
                upd.setStatus("停用");
                fiaInspStdMapper.updateById(upd);
                // 新版本(草稿,待质量审核后生效)
                FiaInspStd next = new FiaInspStd();
                next.setOrgId(order.getOrgId());
                next.setCode(old.getCode());
                next.setMaterial(old.getMaterial());
                next.setProcName(old.getProcName());
                String oldVer = old.getStdVersion() == null ? "v0" : old.getStdVersion();
                int verNum = 0;
                String digits = oldVer.replaceAll("\\D", "");
                if (!digits.isEmpty()) {
                    verNum = Integer.parseInt(digits);
                }
                next.setStdVersion("v" + (verNum + 1));
                next.setStatus("草稿");
                next.setPrevVersionId(old.getId());
                fiaInspStdMapper.insert(next);
            }
        } catch (Exception e) {
            log.warn("变更联动FIA标准失败, changeId={}: {}", order.getId(), e.getMessage(), e);
        }
    }
}
