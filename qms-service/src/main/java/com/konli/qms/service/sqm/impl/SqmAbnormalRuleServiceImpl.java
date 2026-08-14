package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.sqm.entity.SqmAbnormalRule;
import com.konli.qms.domain.sqm.mapper.SqmAbnormalRuleMapper;
import com.konli.qms.service.sqm.SqmAbnormalRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 来料异常严重度判定规则。复用 ops.sqm_abnormal_rule 表(无 is_deleted 列,删除为物理删除)。 */
@Service
@RequiredArgsConstructor
public class SqmAbnormalRuleServiceImpl implements SqmAbnormalRuleService {

    private final SqmAbnormalRuleMapper mapper;

    @Override
    public List<SqmAbnormalRule> list() {
        LambdaQueryWrapper<SqmAbnormalRule> w = new LambdaQueryWrapper<>();
        if (!CompanyContext.isAdmin()) {
            // 非 admin: 看本组织规则 + 全局规则(org_id IS NULL, 历史 seed 数据未归属组织)
            String orgId = currentOrgId();
            if (orgId != null) {
                w.and(x -> x.eq(SqmAbnormalRule::getOrgId, orgId).or().isNull(SqmAbnormalRule::getOrgId));
            } else {
                w.isNull(SqmAbnormalRule::getOrgId);
            }
        }
        w.orderByDesc(SqmAbnormalRule::getUpdatedAt);
        return mapper.selectList(w);
    }

    @Override
    @Transactional
    public SqmAbnormalRule save(SqmAbnormalRule rule) {
        if (rule.getId() == null) {
            rule.setOrgId(currentOrgId());
            rule.setUpdatedAt(LocalDateTime.now());
            mapper.insert(rule);
        } else {
            SqmAbnormalRule existing = mapper.selectById(rule.getId());
            if (existing == null) {
                throw new BusinessException(404, "异常严重度规则不存在");
            }
            // 仅更新业务字段,保留 org 归属
            existing.setSevereMinQty(rule.getSevereMinQty());
            existing.setGeneralAccumDays(rule.getGeneralAccumDays());
            existing.setGeneralAccumQty(rule.getGeneralAccumQty());
            existing.setRemark(rule.getRemark());
            existing.setUpdatedAt(LocalDateTime.now());
            mapper.updateById(existing);
            rule = existing;
        }
        return rule;
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (mapper.selectById(id) == null) {
            throw new BusinessException(404, "异常严重度规则不存在");
        }
        mapper.deleteById(id);
    }

    private String currentOrgId() {
        com.konli.qms.common.security.CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = (u != null) ? u.orgId() : null;
        if (orgId == null || orgId.isBlank() || "ROOT".equals(orgId)) {
            return null;
        }
        return orgId;
    }
}
