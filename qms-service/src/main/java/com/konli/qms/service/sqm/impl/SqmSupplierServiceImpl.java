package com.konli.qms.service.sqm.impl;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.sqm.entity.SqmSupplier;
import com.konli.qms.domain.sqm.mapper.SqmSupplierMapper;
import com.konli.qms.service.sqm.SqmAbnormalService;
import com.konli.qms.service.sqm.SqmAuditService;
import com.konli.qms.service.sqm.SqmChangeService;
import com.konli.qms.service.sqm.SqmSupplierCertService;
import com.konli.qms.service.sqm.SqmSupplierPerformanceService;
import com.konli.qms.service.sqm.SqmSupplierService;
import com.konli.qms.service.sqm.SqmTraceService;
import com.konli.qms.service.sqm.dto.SqmSupplierOverviewVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqmSupplierServiceImpl implements SqmSupplierService {

    private final SqmSupplierMapper sqmSupplierMapper;
    private final JdbcTemplate jdbcTemplate;
    private final SqmSupplierCertService sqmSupplierCertService;
    private final SqmAuditService sqmAuditService;
    private final SqmSupplierPerformanceService sqmSupplierPerformanceService;
    private final SqmAbnormalService sqmAbnormalService;
    private final SqmChangeService sqmChangeService;
    private final SqmTraceService sqmTraceService;

    @Override
    public List<SqmSupplier> list() {
        return sqmSupplierMapper.selectList(null);
    }

    @Override
    public PageResult<SqmSupplier> listPage(String keyword, String level, String status, int page, int size) {
        LambdaQueryWrapper<SqmSupplier> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SqmSupplier::getName, keyword)
                    .or().like(SqmSupplier::getSupplierNo, keyword)
                    .or().like(SqmSupplier::getSupplierCode, keyword));
        }
        if (StringUtils.hasText(level)) {
            qw.eq(SqmSupplier::getLevel, level);
        }
        if (StringUtils.hasText(status)) {
            qw.eq(SqmSupplier::getStatus, status);
        }
        qw.orderByDesc(SqmSupplier::getCreatedAt);
        IPage<SqmSupplier> ip = sqmSupplierMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
    }

    @Override
    public SqmSupplier get(String id) {
        return sqmSupplierMapper.selectById(id);
    }

    @Override
    public SqmSupplierOverviewVo overview(String id) {
        SqmSupplier supplier = get(id);
        if (supplier == null) {
            throw new BusinessException(404, "供应商不存在");
        }
        SqmSupplierOverviewVo vo = new SqmSupplierOverviewVo();
        vo.setSupplier(supplier);
        vo.setCertCount(sqmSupplierCertService.list(id).size());
        vo.setAuditCount(sqmAuditService.listPlansPage(null, null, id, 1, 1).getTotal());
        vo.setPerformanceCount(sqmSupplierPerformanceService.listPage(id, null, 1, 1).getTotal());
        vo.setAbnormalCount(sqmAbnormalService.listAbnormalsPage(null, null, null, id, 1, 1).getTotal());
        vo.setChangeCount(sqmChangeService.listPage(null, null, id, 1, 1).getTotal());
        vo.setLotCount(sqmTraceService.listLotsPage(null, id, supplier.getOrgId(), 1, 1).getTotal());
        return vo;
    }

    @Override
    public SqmSupplier findByVenCode(String venCode) {
        if (venCode == null || venCode.isBlank()) {
            return null;
        }
        return sqmSupplierMapper.selectOne(
                new LambdaQueryWrapper<SqmSupplier>().eq(SqmSupplier::getVenCode, venCode.trim()));
    }

    @Override
    @Transactional
    public SqmSupplier create(SqmSupplier supplier) {
        supplier.setOrgId(currentOrgId());
        if (supplier.getSupplierNo() == null) {
            supplier.setSupplierNo("SUP-" + System.currentTimeMillis());
        }
        // 前端准入申请传 status='待审核',正常创建传 null -> 默认'启用'
        if (supplier.getStatus() == null || supplier.getStatus().isBlank()) {
            supplier.setStatus("启用");
        }
        if (supplier.getCreditCode() == null || supplier.getCreditCode().isBlank()) {
            supplier.setCreditCode(supplier.getSupplierCode() != null ? supplier.getSupplierCode() : "");
        }
        // certs:前端传 JSON.stringify 字符串,直接存即可(MyBatis-Plus + PG 驱动自动处理 JSONB 列)
        if (supplier.getCerts() != null && !supplier.getCerts().trim().startsWith("[")) {
            supplier.setCerts("[]"); // 非法格式兜底
        }
        sqmSupplierMapper.insert(supplier);
        return supplier;
    }

    @Override
    @Transactional
    public void update(SqmSupplier supplier) {
        if (supplier.getId() == null || sqmSupplierMapper.selectById(supplier.getId()) == null) {
            throw new BusinessException(404, "供应商不存在");
        }
        // MyBatis-Plus updateById 默认只更新非 null 字段(FieldStrategy.NOT_NULL),
        // 前端 Partial Update(如仅改 status)传入的 null 字段不会覆盖 DB 已有值,安全。
        sqmSupplierMapper.updateById(supplier);
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (sqmSupplierMapper.selectById(id) == null) {
            throw new BusinessException(404, "供应商不存在");
        }
        sqmSupplierMapper.deleteById(id);
    }

    private String currentOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = (u != null) ? u.orgId() : null;
        if (orgId == null || orgId.isBlank() || "ROOT".equals(orgId)) {
            return resolveDefaultOrgId();
        }
        return orgId;
    }

    private String resolveDefaultOrgId() {
        try {
            String id = jdbcTemplate.queryForObject(
                    "SELECT id::text FROM ops.sys_org WHERE org_code='MZ' LIMIT 1", String.class);
            if (id != null) {
                return id;
            }
        } catch (Exception ignored) {
            // 忽略
        }
        try {
            return jdbcTemplate.queryForObject("SELECT id::text FROM ops.sys_org LIMIT 1", String.class);
        } catch (Exception e) {
            log.warn("resolveDefaultOrgId failed: {}", e.getMessage());
            return null;
        }
    }
}
