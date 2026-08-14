package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.sqm.entity.SqmAuditApproval;
import com.konli.qms.domain.sqm.entity.SqmAuditChecklistItem;
import com.konli.qms.domain.sqm.entity.SqmAuditNc;
import com.konli.qms.domain.sqm.entity.SqmAuditPhoto;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.SqmAuditRecord;
import com.konli.qms.domain.sqm.entity.SqmAuditWorkflowLog;
import com.konli.qms.domain.sqm.mapper.SqmAuditApprovalMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditChecklistItemMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditNcMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditPhotoMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditPlanMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditRecordMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditWorkflowLogMapper;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.sqm.SqmAuditExecuteService;
import com.konli.qms.service.sqm.SqmAuditReportArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 审核执行环节实现。复用既有会签机制(sqm_audit_approval)承载「执行结果复核」节点,
 * 进入执行页首次录入时惰性创建 sqm_audit_record(status=执行中)并回填计划。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SqmAuditExecuteServiceImpl implements SqmAuditExecuteService {

    private final SqmAuditPlanMapper planMapper;
    private final SqmAuditRecordMapper recordMapper;
    private final SqmAuditApprovalMapper approvalMapper;
    private final SqmAuditChecklistItemMapper checklistMapper;
    private final SqmAuditPhotoMapper photoMapper;
    private final SqmAuditNcMapper ncMapper;
    private final SqmAuditWorkflowLogMapper workflowLogMapper;
    private final SqmAuditReportArchiveService reportArchiveService;
    private final NotificationService notificationService;

    private String currentUser() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return (u != null && u.username() != null && !u.username().isEmpty()) ? u.username() : "系统";
    }

    private String resolveOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u != null && u.orgId() != null && !"ROOT".equals(u.orgId())) {
            return u.orgId();
        }
        return null;
    }

    @Override
    public ExecuteInit init(String planId) {
        SqmAuditPlan plan = planMapper.selectById(planId);
        if (plan == null) throw new BusinessException(404, "审核计划不存在");
        ExecuteInit init = new ExecuteInit();
        init.plan = plan;
        // 惰性建记录:计划处于进行中且无 record_id 时,首次进入执行页创建执行中记录
        if (plan.getRecordId() == null && "进行中".equals(plan.getStatus())) {
            SqmAuditRecord rec = new SqmAuditRecord();
            rec.setOrgId(plan.getOrgId());
            rec.setPlanId(plan.getId());
            rec.setSupplierId(plan.getSupplierId());
            rec.setAuditType(plan.getAuditType());
            rec.setAuditLead(plan.getAuditLead());
            rec.setAuditorTeam(plan.getActualAuditors() != null ? plan.getActualAuditors() : plan.getAuditorTeam());
            rec.setStatus("执行中");
            rec.setResult("待评定");
            rec.setNcCount(0);
            rec.setAuditDate(plan.getActualDate() != null ? plan.getActualDate() : LocalDate.now());
            rec.setRecordNo("AR-" + System.currentTimeMillis());
            recordMapper.insert(rec);
            plan.setRecordId(rec.getId());
            planMapper.updateById(plan);
            writeLog(planId, plan.getOrgId(), "execute_init", "进入执行", null);
            init.recordId = rec.getId();
        } else {
            init.recordId = plan.getRecordId();
        }
        // 查已有复核节点(幂等)
        SqmAuditApproval review = approvalMapper.selectOne(new LambdaQueryWrapper<SqmAuditApproval>()
                .eq(SqmAuditApproval::getAuditId, planId)
                .eq(SqmAuditApproval::getApprovalRole, "review"));
        init.review = review;
        return init;
    }

    @Override
    @Transactional
    public void saveChecklist(String recordId, List<SqmAuditChecklistItem> items) {
        if (recordId == null) throw new BusinessException(400, "审核记录不存在,无法保存检查项");
        SqmAuditRecord rec = recordMapper.selectById(recordId);
        if (rec == null) throw new BusinessException(404, "审核记录不存在");
        // 全量 upsert:删旧插入新,保证 seq 唯一约束
        checklistMapper.delete(new LambdaQueryWrapper<SqmAuditChecklistItem>()
                .eq(SqmAuditChecklistItem::getRecordId, recordId));
        int seq = 1;
        for (SqmAuditChecklistItem it : items) {
            if (it.getItemName() == null || it.getItemName().isBlank()) continue;
            it.setRecordId(recordId);
            it.setOrgId(rec.getOrgId());
            it.setSeq(seq++);
            if (it.getResult() == null || it.getResult().isBlank()) it.setResult("符合");
            checklistMapper.insert(it);
        }
        // 同步记录的不符合项数(检查项中判为不符合的)
        long ncCnt = items.stream().filter(i -> "不符合".equals(i.getResult())).count();
        rec.setNcCount((int) ncCnt);
        recordMapper.updateById(rec);
        writeLog(rec.getPlanId(), rec.getOrgId(), "checklist_saved", "保存检查项", "共 " + (seq - 1) + " 项,不符合 " + ncCnt + " 项");
    }

    @Override
    public List<SqmAuditChecklistItem> listChecklist(String recordId) {
        return checklistMapper.selectList(new LambdaQueryWrapper<SqmAuditChecklistItem>()
                .eq(SqmAuditChecklistItem::getRecordId, recordId)
                .orderByAsc(SqmAuditChecklistItem::getSeq));
    }

    @Override
    @Transactional
    public SqmAuditPhoto addPhoto(SqmAuditPhoto photo) {
        if (photo.getRecordId() == null) throw new BusinessException(400, "审核记录不存在,无法添加照片");
        SqmAuditRecord rec = recordMapper.selectById(photo.getRecordId());
        if (rec == null) throw new BusinessException(404, "审核记录不存在");
        photo.setOrgId(rec.getOrgId());
        if (photo.getShootTime() == null) photo.setShootTime(LocalDateTime.now());
        photoMapper.insert(photo);
        return photo;
    }

    @Override
    public List<SqmAuditPhoto> listPhotos(String recordId) {
        return photoMapper.selectList(new LambdaQueryWrapper<SqmAuditPhoto>()
                .eq(SqmAuditPhoto::getRecordId, recordId)
                .orderByDesc(SqmAuditPhoto::getShootTime));
    }

    @Override
    @Transactional
    public void removePhoto(String photoId) {
        photoMapper.deleteById(photoId);
    }

    @Override
    @Transactional
    public SqmAuditNc createNc(String recordId, SqmAuditNc nc) {
        SqmAuditRecord rec = recordMapper.selectById(recordId);
        if (rec == null) throw new BusinessException(404, "审核记录不存在");
        nc.setRecordId(recordId);
        nc.setOrgId(rec.getOrgId());
        nc.setSupplierId(rec.getSupplierId());
        if (nc.getNcNo() == null) nc.setNcNo("NC-" + System.currentTimeMillis());
        if (nc.getStatus() == null) nc.setStatus("待整改");
        if (nc.getDescription() == null || nc.getDescription().isBlank()) nc.setDescription("（未填写描述）");
        ncMapper.insert(nc);
        // 同步记录的累计不符合项数(闭环自动判定结论用)
        if (rec.getNcCount() == null) rec.setNcCount(0);
        rec.setNcCount(rec.getNcCount() + 1);
        recordMapper.updateById(rec);
        writeLog(rec.getPlanId(), rec.getOrgId(), "nc_added", "新增不符合项", nc.getNcNo() + " " + nc.getLevel());
        return nc;
    }

    @Override
    public List<SqmAuditNc> listNcs(String recordId) {
        return ncMapper.selectList(new LambdaQueryWrapper<SqmAuditNc>()
                .eq(SqmAuditNc::getRecordId, recordId));
    }

    @Override
    public List<SqmAuditWorkflowLog> workflowLog(String planId) {
        return workflowLogMapper.selectList(new LambdaQueryWrapper<SqmAuditWorkflowLog>()
                .eq(SqmAuditWorkflowLog::getPlanId, planId)
                .orderByAsc(SqmAuditWorkflowLog::getCreatedAt));
    }

    @Override
    @Transactional
    public SqmAuditApproval submitReview(String planId) {
        SqmAuditPlan plan = planMapper.selectById(planId);
        if (plan == null) throw new BusinessException(404, "审核计划不存在");
        // 幂等:已有 review 节点则不重复插入
        SqmAuditApproval exists = approvalMapper.selectOne(new LambdaQueryWrapper<SqmAuditApproval>()
                .eq(SqmAuditApproval::getAuditId, planId)
                .eq(SqmAuditApproval::getApprovalRole, "review"));
        if (exists != null) return exists;
        if (plan.getRecordId() == null) throw new BusinessException(400, "尚未录入执行数据,无法提交复核");
        // 前置校验:现场审核检查项必须已录入,否则不允许提交复核(生命周期顺序约束)
        long ck = checklistMapper.selectCount(new LambdaQueryWrapper<SqmAuditChecklistItem>()
                .eq(SqmAuditChecklistItem::getRecordId, plan.getRecordId()));
        if (ck == 0) throw new BusinessException(400, "请先完成现场审核(至少保存 1 项检查项)再提交复核");
        SqmAuditApproval review = new SqmAuditApproval();
        review.setId(UUID.randomUUID().toString());
        review.setOrgId(plan.getOrgId());
        review.setAuditId(planId);
        review.setApprovalRole("review");
        review.setRoleLabel("执行结果复核");
        review.setStatus("pending");
        review.setHasVeto(false);
        review.setSeqOrder(99);
        approvalMapper.insert(review);
        writeLog(planId, plan.getOrgId(), "review", "提交执行结果复核", null);
        return review;
    }

    /** 复核通过后闭环:计划置已完成 + 自动判定/人工兜底结论 + 自动归档(前端在 review 节点 approve 成功后调用)。 */
    @Transactional
    public void completeReview(String planId, String conclusion) {
        SqmAuditPlan plan = planMapper.selectById(planId);
        if (plan == null) throw new BusinessException(404, "审核计划不存在");
        if (plan.getRecordId() == null) throw new BusinessException(400, "尚未录入执行数据,无法闭环");
        SqmAuditApproval review = approvalMapper.selectOne(new LambdaQueryWrapper<SqmAuditApproval>()
                .eq(SqmAuditApproval::getAuditId, planId)
                .eq(SqmAuditApproval::getApprovalRole, "review"));
        if (review == null || !"done".equals(review.getStatus()))
            throw new BusinessException(409, "执行结果复核尚未通过,无法闭环");
        SqmAuditRecord rec = recordMapper.selectById(plan.getRecordId());
        if (rec != null && !"已完成".equals(rec.getStatus())) {
            // 结论:人工兜底优先,否则按不符合项数自动判定
            String finalConclusion = (conclusion != null && !conclusion.isBlank())
                    ? conclusion.strip()
                    : (rec.getNcCount() != null && rec.getNcCount() > 0 ? "有条件通过" : "通过");
            rec.setResult(finalConclusion);
            rec.setStatus("已完成");
            recordMapper.updateById(rec);
        }
        if (!"已完成".equals(plan.getStatus())) {
            plan.setStatus("已完成");
            planMapper.updateById(plan);
        }
        writeLog(planId, plan.getOrgId(), "archived", "复核通过,审核闭环", "结论:" + (rec != null ? rec.getResult() : "—"));
        final String rid = plan.getRecordId();
        if (rid != null) {
            try {
                reportArchiveService.generatePdf(rid);
            } catch (Exception e) {
                log.warn("审核记录复核后归档失败, recordId={}: {}", rid, e.getMessage());
            }
        }
    }

    private void writeLog(String planId, String orgId, String node, String action, String remark) {
        try {
            SqmAuditWorkflowLog log = new SqmAuditWorkflowLog();
            log.setPlanId(planId);
            log.setOrgId(orgId);
            log.setNode(node);
            log.setAction(action);
            log.setOperator(currentUser());
            log.setRemark(remark);
            log.setCreatedAt(LocalDateTime.now());
            workflowLogMapper.insert(log);
        } catch (Exception e) {
            log.warn("审核流程轨迹写入失败: {}", e.getMessage());
        }
    }
}
