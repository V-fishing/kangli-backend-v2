package com.konli.qms.service.tlm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.tlm.entity.TlmMaintPlan;
import com.konli.qms.domain.tlm.entity.TlmMaintRecord;
import com.konli.qms.domain.tlm.entity.TlmTooling;
import com.konli.qms.domain.tlm.mapper.TlmMaintPlanMapper;
import com.konli.qms.domain.tlm.mapper.TlmMaintRecordMapper;
import com.konli.qms.domain.tlm.mapper.TlmToolingMapper;
import com.konli.qms.service.tlm.TlmMaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TlmMaintServiceImpl implements TlmMaintService {

    private final TlmMaintPlanMapper planMapper;
    private final TlmMaintRecordMapper recordMapper;
    private final TlmToolingMapper toolingMapper;

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
        } catch (Exception e) {
            return null;
        }
    }

    private String curUser() {
        try {
            return CompanyContext.get().userId();
        } catch (Exception e) {
            return null;
        }
    }

    /** 解析组织: 优先当前登录组织, 回退到所选工装的组织(工装表含 org_id)。 */
    private String resolveOrg(String toolId) {
        String o = curOrg();
        if (o != null) return o;
        if (toolId != null && !toolId.isBlank()) {
            TlmTooling t = toolingMapper.selectById(toolId);
            if (t != null && t.getOrgId() != null) return t.getOrgId();
        }
        return null;
    }

    @Override
    public PageResult<TlmMaintPlan> planPage(String toolId, int page, int size) {
        LambdaQueryWrapper<TlmMaintPlan> w = new LambdaQueryWrapper<>();
        if (toolId != null && !toolId.isBlank()) w.eq(TlmMaintPlan::getToolId, toolId);
        w.orderByAsc(TlmMaintPlan::getNextDate);
        IPage<TlmMaintPlan> p = planMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<TlmMaintPlan>(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    @Transactional
    public TlmMaintPlan createPlan(TlmMaintPlan plan) {
        if (plan.getPlanNo() == null || plan.getPlanNo().isBlank()) {
            plan.setPlanNo("TLM-MP-" + System.currentTimeMillis());
        }
        if (plan.getOrgId() == null || plan.getOrgId().isBlank()) {
            plan.setOrgId(resolveOrg(plan.getToolId()));
        }
        String uid = curUser();
        if (uid != null) plan.setCreatedBy(uid);
        planMapper.insert(plan);
        return plan;
    }

    @Override
    @Transactional
    public TlmMaintPlan updatePlan(TlmMaintPlan plan) {
        planMapper.updateById(plan);
        return plan;
    }

    @Override
    @Transactional
    public void deletePlan(String id) {
        planMapper.deleteById(id);
    }

    @Override
    public List<TlmMaintRecord> recordList(String toolId) {
        LambdaQueryWrapper<TlmMaintRecord> w = new LambdaQueryWrapper<>();
        if (toolId != null && !toolId.isBlank()) w.eq(TlmMaintRecord::getToolId, toolId);
        w.orderByDesc(TlmMaintRecord::getMaintDate);
        return recordMapper.selectList(w);
    }

    @Override
    @Transactional
    public TlmMaintRecord createRecord(TlmMaintRecord record) {
        if (record.getOrgId() == null || record.getOrgId().isBlank()) {
            record.setOrgId(resolveOrg(record.getToolId()));
        }
        String uid = curUser();
        if (uid != null) record.setCreatedBy(uid);
        recordMapper.insert(record);
        return record;
    }
}
