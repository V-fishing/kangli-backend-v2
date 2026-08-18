package com.konli.qms.service.my;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.dto.MyTaskDTO;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.fia.mapper.FiaTaskMapper;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.domain.ncm.entity.NcmCorrectiveAction;
import com.konli.qms.domain.ncm.mapper.Qms8dReportMapper;
import com.konli.qms.domain.ncm.mapper.QmsCapaMapper;
import com.konli.qms.domain.ncm.mapper.NcmCorrectiveActionMapper;
import com.konli.qms.domain.patrol.entity.PatlTask;
import com.konli.qms.domain.patrol.mapper.PatlTaskMapper;
import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.QmsFmeaRisk;
import com.konli.qms.domain.sqm.mapper.SqmIncomingAbnormalMapper;
import com.konli.qms.domain.sqm.mapper.SqmAuditPlanMapper;
import com.konli.qms.domain.sqm.mapper.QmsFmeaRiskMapper;
import com.konli.qms.domain.cs.entity.CsWorkOrder;
import com.konli.qms.domain.cs.mapper.CsWorkOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 跨模块"我的任务"聚合:按当前登录用户聚合其作为负责人/被指派且未闭环的任务
 * (FIA 首件 / NCM 8D / 巡检 / NCM CAPA / NCM 纠正措施 / SQM 来料异常 / SQM 审核 / SQM FMEA)。
 * 供看板"我的任务"卡片与工作台任务中心复用。
 */
@Service
@RequiredArgsConstructor
public class MyTaskServiceImpl implements MyTaskService {

    private final FiaTaskMapper fiaTaskMapper;
    private final Qms8dReportMapper qms8dReportMapper;
    private final PatlTaskMapper patlTaskMapper;
    private final QmsCapaMapper qmsCapaMapper;
    private final NcmCorrectiveActionMapper ncmCorrectiveActionMapper;
    private final SqmIncomingAbnormalMapper sqmIncomingAbnormalMapper;
    private final SqmAuditPlanMapper sqmAuditPlanMapper;
    private final QmsFmeaRiskMapper qmsFmeaRiskMapper;
    private final CsWorkOrderMapper csWorkOrderMapper;

    @Override
    public List<MyTaskDTO> myTasks(Integer limit, Boolean includeClosed) {
        String userId = currentUserId();
        boolean closed = Boolean.TRUE.equals(includeClosed);
        List<MyTaskDTO> all = new ArrayList<>();
        all.addAll(fiaTasks(userId, closed));
        all.addAll(ncm8dTasks(userId, closed));
        all.addAll(patrolTasks(userId, closed));
        all.addAll(ncmCapaTasks(userId, closed));
        all.addAll(ncmCaTasks(userId, closed));
        all.addAll(sqmAbnormalTasks(userId, closed));
        all.addAll(sqmAuditTasks(userId, closed));
        all.addAll(sqmFmeaTasks(userId, closed));
        all.addAll(csWorkOrderTasks(userId, closed));
        // 按状态优先级 + 单号排序,保证看板展示稳定
        all.sort(Comparator.comparing(MyTaskDTO::getModule).thenComparing(
                t -> t.getBizNo() == null ? "" : t.getBizNo()));
        if (limit != null && limit > 0 && all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }

    private String currentUserId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? null : u.userId();
    }

    private List<MyTaskDTO> fiaTasks(String userId, boolean includeClosed) {
        if (userId == null) return List.of();
        LambdaQueryWrapper<FiaTask> w = new LambdaQueryWrapper<>();
        w.eq(FiaTask::getInspectorId, userId);
        if (!includeClosed) {
            w.ne(FiaTask::getStatus, "已完成");
        }
        w.orderByDesc(FiaTask::getCreatedAt);
        List<FiaTask> list = fiaTaskMapper.selectList(w);
        List<MyTaskDTO> res = new ArrayList<>();
        for (FiaTask t : list) {
            MyTaskDTO d = new MyTaskDTO();
            d.setModule("FIA");
            d.setTaskType("首件检验");
            d.setBizNo(t.getCode());
            d.setTitle(t.getProductName());
            d.setStatus(t.getStatus());
            d.setAssignee(null);
            d.setDueAt(t.getSlaDueAt());
            // 闭环时间: 优先批准时间, 兜底创建时间(首件无独立闭环字段)
            d.setClosedAt(t.getApprovedAt() != null ? t.getApprovedAt() : t.getCreatedAt());
            d.setUrl("/fia/tasks");
            res.add(d);
        }
        return res;
    }

    private List<MyTaskDTO> ncm8dTasks(String userId, boolean includeClosed) {
        if (userId == null) return List.of();
        LambdaQueryWrapper<Qms8dReport> w = new LambdaQueryWrapper<>();
        w.eq(Qms8dReport::getOwnerUserId, userId);
        if (!includeClosed) {
            w.ne(Qms8dReport::getStatus, "已关闭");
        }
        w.orderByDesc(Qms8dReport::getCreatedAt);
        List<Qms8dReport> list = qms8dReportMapper.selectList(w);
        List<MyTaskDTO> res = new ArrayList<>();
        for (Qms8dReport r : list) {
            MyTaskDTO d = new MyTaskDTO();
            d.setModule("NCM");
            d.setTaskType("8D报告");
            d.setBizNo(r.getD8No());
            d.setTitle(r.getIssue());
            d.setStatus(r.getStatus());
            d.setAssignee(r.getOwnerUserName());
            d.setDueAt(null);
            // 闭环时间: 8D 用独立 closeDate, 无则回退 updatedAt
            d.setClosedAt(r.getCloseDate() != null ? r.getCloseDate().atStartOfDay() : r.getUpdatedAt());
            d.setUrl("/ncm/8d-reports/" + r.getId());
            res.add(d);
        }
        return res;
    }

    private List<MyTaskDTO> patrolTasks(String userId, boolean includeClosed) {
        if (userId == null) return List.of();
        LambdaQueryWrapper<PatlTask> w = new LambdaQueryWrapper<>();
        w.eq(PatlTask::getInspectorId, userId);
        if (!includeClosed) {
            w.ne(PatlTask::getStatus, "已完成");
        }
        w.orderByDesc(PatlTask::getPlanTime);
        List<PatlTask> list = patlTaskMapper.selectList(w);
        List<MyTaskDTO> res = new ArrayList<>();
        for (PatlTask t : list) {
            MyTaskDTO d = new MyTaskDTO();
            d.setModule("PATROL");
            d.setTaskType("巡检任务");
            d.setBizNo(t.getTaskNo());
            d.setTitle(t.getShift());
            d.setStatus(t.getStatus());
            d.setAssignee(null);
            d.setDueAt(t.getPlanTime());
            // 闭环时间: 巡检用完成时间 finishTime, 无则回退 updatedAt
            d.setClosedAt(t.getFinishTime() != null ? t.getFinishTime() : t.getUpdatedAt());
            d.setUrl("/patrol/tasks");
            res.add(d);
        }
        return res;
    }

    private List<MyTaskDTO> ncmCapaTasks(String userId, boolean includeClosed) {
        if (userId == null) return List.of();
        LambdaQueryWrapper<QmsCapa> w = new LambdaQueryWrapper<>();
        w.eq(QmsCapa::getOwnerUserId, userId);
        if (!includeClosed) {
            w.ne(QmsCapa::getStatus, "已关闭");
        }
        w.orderByDesc(QmsCapa::getCreatedAt);
        List<QmsCapa> list = qmsCapaMapper.selectList(w);
        List<MyTaskDTO> res = new ArrayList<>();
        for (QmsCapa c : list) {
            MyTaskDTO d = new MyTaskDTO();
            d.setModule("NCM");
            d.setTaskType("CAPA");
            d.setBizNo(c.getCapaNo());
            d.setTitle(c.getIssue());
            d.setStatus(c.getStatus());
            d.setAssignee(c.getOwner());
            d.setDueAt(c.getDueDate() != null ? c.getDueDate().atStartOfDay() : null);
            // 闭环时间: CAPA 无独立闭环字段, 回退 updatedAt
            d.setClosedAt(c.getUpdatedAt());
            d.setUrl("/ncm/capas/" + c.getId());
            res.add(d);
        }
        return res;
    }

    private List<MyTaskDTO> ncmCaTasks(String userId, boolean includeClosed) {
        if (userId == null) return List.of();
        LambdaQueryWrapper<NcmCorrectiveAction> w = new LambdaQueryWrapper<>();
        w.eq(NcmCorrectiveAction::getOwnerUserId, userId);
        if (!includeClosed) {
            w.ne(NcmCorrectiveAction::getStatus, "已关闭");
        }
        w.orderByDesc(NcmCorrectiveAction::getCreatedAt);
        List<NcmCorrectiveAction> list = ncmCorrectiveActionMapper.selectList(w);
        List<MyTaskDTO> res = new ArrayList<>();
        for (NcmCorrectiveAction c : list) {
            MyTaskDTO d = new MyTaskDTO();
            d.setModule("NCM");
            d.setTaskType("纠正措施");
            d.setBizNo(c.getCaNo());
            d.setTitle(c.getIssue());
            d.setStatus(c.getStatus());
            d.setAssignee(c.getOwnerName());
            d.setDueAt(c.getDueDate() != null ? c.getDueDate().atStartOfDay() : null);
            // 闭环时间: 纠正措施无独立闭环字段, 回退 updatedAt
            d.setClosedAt(c.getUpdatedAt());
            d.setUrl("/ncm/corrective-actions/" + c.getId());
            res.add(d);
        }
        return res;
    }

    private List<MyTaskDTO> sqmAbnormalTasks(String userId, boolean includeClosed) {
        if (userId == null) return List.of();
        LambdaQueryWrapper<SqmIncomingAbnormal> w = new LambdaQueryWrapper<>();
        w.eq(SqmIncomingAbnormal::getHandlerId, userId);
        if (!includeClosed) {
            w.ne(SqmIncomingAbnormal::getStatus, "已关闭");
        }
        w.orderByDesc(SqmIncomingAbnormal::getCreatedAt);
        List<SqmIncomingAbnormal> list = sqmIncomingAbnormalMapper.selectList(w);
        List<MyTaskDTO> res = new ArrayList<>();
        for (SqmIncomingAbnormal a : list) {
            MyTaskDTO d = new MyTaskDTO();
            d.setModule("SQM");
            d.setTaskType("来料异常");
            d.setBizNo(a.getAbnormalNo());
            d.setTitle(a.getDescription());
            d.setStatus(a.getStatus());
            d.setAssignee(null);
            d.setDueAt(null);
            // 闭环时间: 来料异常用独立 closeDate, 无则回退 updatedAt
            d.setClosedAt(a.getCloseDate() != null ? a.getCloseDate().atStartOfDay() : a.getUpdatedAt());
            d.setUrl("/sqm/abnormals");
            res.add(d);
        }
        return res;
    }

    private List<MyTaskDTO> sqmAuditTasks(String userId, boolean includeClosed) {
        if (userId == null) return List.of();
        LambdaQueryWrapper<SqmAuditPlan> w = new LambdaQueryWrapper<>();
        w.eq(SqmAuditPlan::getAuditLeadUserId, userId);
        if (!includeClosed) {
            w.ne(SqmAuditPlan::getStatus, "已关闭");
        }
        w.orderByDesc(SqmAuditPlan::getCreatedAt);
        List<SqmAuditPlan> list = sqmAuditPlanMapper.selectList(w);
        List<MyTaskDTO> res = new ArrayList<>();
        for (SqmAuditPlan p : list) {
            MyTaskDTO d = new MyTaskDTO();
            d.setModule("SQM");
            d.setTaskType("供应商审核");
            d.setBizNo(p.getPlanNo());
            d.setTitle(p.getAuditType());
            d.setStatus(p.getStatus());
            d.setAssignee(p.getAuditLead());
            d.setDueAt(p.getPlanDate() != null ? p.getPlanDate().atStartOfDay() : null);
            // 闭环时间: 审核计划无独立闭环字段, 回退 updatedAt
            d.setClosedAt(p.getUpdatedAt());
            d.setUrl("/sqm/audits/plan/" + p.getId());
            res.add(d);
        }
        return res;
    }

    private List<MyTaskDTO> sqmFmeaTasks(String userId, boolean includeClosed) {
        if (userId == null) return List.of();
        LambdaQueryWrapper<QmsFmeaRisk> w = new LambdaQueryWrapper<>();
        w.eq(QmsFmeaRisk::getOwnerUserId, userId);
        if (!includeClosed) {
            w.ne(QmsFmeaRisk::getStatus, "已闭环");
        }
        w.orderByDesc(QmsFmeaRisk::getCreatedAt);
        List<QmsFmeaRisk> list = qmsFmeaRiskMapper.selectList(w);
        List<MyTaskDTO> res = new ArrayList<>();
        for (QmsFmeaRisk r : list) {
            MyTaskDTO d = new MyTaskDTO();
            d.setModule("SQM");
            d.setTaskType("FMEA风险");
            d.setBizNo(r.getRiskNo());
            d.setTitle(r.getFailureMode());
            d.setStatus(r.getStatus());
            d.setAssignee(r.getOwner());
            d.setDueAt(r.getTargetDate() != null ? r.getTargetDate().atStartOfDay() : null);
            // 闭环时间: FMEA 用独立 closeDate, 无则回退 updatedAt
            d.setClosedAt(r.getCloseDate() != null ? r.getCloseDate().atStartOfDay() : r.getUpdatedAt());
            d.setUrl("/sqm/fmea");
            res.add(d);
        }
        return res;
    }

    private List<MyTaskDTO> csWorkOrderTasks(String userId, boolean includeClosed) {
        if (userId == null) return List.of();
        LambdaQueryWrapper<CsWorkOrder> w = new LambdaQueryWrapper<>();
        w.eq(CsWorkOrder::getOwnerId, userId);
        if (!includeClosed) {
            w.ne(CsWorkOrder::getStatus, "CLOSED");
        }
        w.orderByDesc(CsWorkOrder::getCreatedAt);
        List<CsWorkOrder> list = csWorkOrderMapper.selectList(w);
        List<MyTaskDTO> res = new ArrayList<>();
        for (CsWorkOrder o : list) {
            MyTaskDTO d = new MyTaskDTO();
            d.setModule("CS");
            d.setTaskType("售后工单");
            d.setBizNo(o.getOrderNo());
            d.setTitle(o.getCustomerName());
            d.setStatus(o.getStatus());
            d.setAssignee(o.getOwnerName());
            d.setDueAt(o.getExpectTime());
            // 闭环时间: 售后工单无独立闭环字段, 回退 updatedAt
            d.setClosedAt(o.getUpdatedAt());
            d.setUrl("/cs/work-orders");
            res.add(d);
        }
        return res;
    }
}
