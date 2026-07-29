package com.konli.qms.service.fia.impl;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.dto.TriggerTypeStat;
import com.konli.qms.domain.fia.entity.FiaTriggerType;
import com.konli.qms.domain.fia.mapper.FiaTriggerTypeMapper;
import com.konli.qms.service.fia.FiaTriggerTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FiaTriggerTypeServiceImpl implements FiaTriggerTypeService {

    private final FiaTriggerTypeMapper fiaTriggerTypeMapper;

    @Override
    public List<FiaTriggerType> list() {
        return fiaTriggerTypeMapper.selectList(null);
    }

    @Override
    public List<TriggerTypeStat> stats() {
        List<FiaTriggerType> types = list();
        List<Map<String, Object>> aggs = fiaTriggerTypeMapper.countByType();
        // 按 trigger_type 名称建立聚合索引(name 唯一对应一个触发类型)
        Map<String, Map<String, Object>> byName = new LinkedHashMap<>();
        Set<String> matched = new java.util.HashSet<>();
        for (Map<String, Object> row : aggs) {
            String name = row.get("triggerType") == null ? null : row.get("triggerType").toString();
            if (name != null) {
                byName.put(name, row);
            }
        }
        List<TriggerTypeStat> result = new ArrayList<>(types.size());
        for (FiaTriggerType t : types) {
            TriggerTypeStat s = new TriggerTypeStat();
            s.setId(t.getId());
            s.setName(t.getName());
            Map<String, Object> row = byName.get(t.getName());
            if (row != null) {
                fill(s, row);
                matched.add(t.getName());
            }
            result.add(s);
        }
        // 任务中使用了但不在已配置触发类型清单里的取值(数据不一致)，单独作为"未配置"行展示，避免统计遗漏
        for (Map<String, Object> row : aggs) {
            String name = row.get("triggerType") == null ? null : row.get("triggerType").toString();
            if (name != null && !matched.contains(name)) {
                TriggerTypeStat s = new TriggerTypeStat();
                s.setId("");
                s.setName(name + "（未配置）");
                fill(s, row);
                result.add(s);
            }
        }
        return result;
    }

    private void fill(TriggerTypeStat s, Map<String, Object> row) {
        s.setTaskCount(toLong(row.get("taskCount")));
        s.setQualifiedCount(toLong(row.get("qualifiedCount")));
        s.setUnqualifiedCount(toLong(row.get("unqualifiedCount")));
        s.setPendingCount(toLong(row.get("pendingCount")));
        s.setOverdueCount(toLong(row.get("overdueCount")));
    }

    private long toLong(Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(o.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    @Transactional
    public FiaTriggerType create(FiaTriggerType triggerType) {
        if (triggerType.getIsEnabled() == null) {
            triggerType.setIsEnabled(true);
        }
        // org_id 由当前登录用户上下文决定(真实 UUID),避免前端传业务代码导致 UUID 写入失败,
        // 同时保证与列表数据隔离过滤(DataScopeInterceptor)使用同一 org_id,新增后能查到。
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u != null && u.orgId() != null && !u.orgId().isBlank() && !"ROOT".equals(u.orgId())) {
            triggerType.setOrgId(u.orgId());
        } else {
            // 管理员(dataScope=all)或无具体组织时置空=全局,管理员列表不过滤仍可见
            triggerType.setOrgId(null);
        }
        fiaTriggerTypeMapper.insert(triggerType);
        return triggerType;
    }

    @Override
    public void update(FiaTriggerType triggerType) {
        fiaTriggerTypeMapper.updateById(triggerType);
    }

    @Override
    @Transactional
    public void delete(String id) {
        fiaTriggerTypeMapper.deleteById(id);
    }

    @Override
    public void toggle(String id, boolean enabled) {
        FiaTriggerType t = new FiaTriggerType();
        t.setId(id);
        t.setIsEnabled(enabled);
        fiaTriggerTypeMapper.updateById(t);
    }
}
