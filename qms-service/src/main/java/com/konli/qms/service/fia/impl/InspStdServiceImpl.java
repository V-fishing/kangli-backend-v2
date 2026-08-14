package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.DataScopeGuard;
import com.konli.qms.domain.fia.dto.CreateInspStdRequest;
import com.konli.qms.domain.fia.dto.FiaStdItemRequest;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.domain.fia.mapper.FiaInspStdItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdMapper;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.service.fia.InspStdService;
import com.konli.qms.service.fia.dto.CtqItemVo;
import com.konli.qms.service.fia.dto.InspStdVo;
import com.konli.qms.service.support.OrgIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspStdServiceImpl implements InspStdService {

    private final FiaInspStdMapper fiaInspStdMapper;
    private final FiaInspStdItemMapper fiaInspStdItemMapper;
    private final SpcParamMapper spcParamMapper;
    private final OrgIdResolver orgIdResolver;

    @Override
    public List<FiaInspStd> list() {
        return fiaInspStdMapper.selectList(null);
    }

    @Override
    public List<FiaInspStd> listByKeyword(String keyword, int limit) {
        LambdaQueryWrapper<FiaInspStd> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim();
            w.and(q -> q.like(FiaInspStd::getCode, k)
                    .or().like(FiaInspStd::getMaterial, k)
                    .or().like(FiaInspStd::getPartNo, k));
        }
        w.eq(FiaInspStd::getIsDeleted, false).orderByDesc(FiaInspStd::getCreatedAt);
        if (limit > 0) w.last("LIMIT " + limit);
        return fiaInspStdMapper.selectList(w);
    }

    @Override
    public PageResult<FiaInspStd> listPage(String keyword, int page, int size) {
        LambdaQueryWrapper<FiaInspStd> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim();
            w.and(q -> q.like(FiaInspStd::getCode, k)
                    .or().like(FiaInspStd::getMaterial, k)
                    .or().like(FiaInspStd::getPartNo, k));
        }
        w.eq(FiaInspStd::getIsDeleted, false).orderByDesc(FiaInspStd::getCreatedAt);
        IPage<FiaInspStd> ip = fiaInspStdMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
    }

    @Override
    public InspStdVo get(String id) {
        InspStdVo vo = new InspStdVo();
        vo.setStd(fiaInspStdMapper.selectById(id));
        vo.setItems(fiaInspStdItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspStdItem>().eq(FiaInspStdItem::getStdId, id)
                        .eq(FiaInspStdItem::getIsDeleted, false).orderByAsc(FiaInspStdItem::getSeq)));
        return vo;
    }

    @Override
    @Transactional
    public FiaInspStd create(FiaInspStd std, List<FiaInspStdItem> items) {
        // 组织 ID 统一解析：前端可能传 org_code(如 MZ) 或真实 UUID，统一转为真实 UUID 入库
        // (OrgIdResolver 已对普通用户强制使用上下文 orgId,防跨公司伪造)
        String orgId = orgIdResolver.resolve(std.getOrgId());
        std.setOrgId(orgId);
        if (std.getStatus() == null) {
            std.setStatus("草稿");
        }
        fiaInspStdMapper.insert(std);
        if (items != null) {
            for (FiaInspStdItem item : items) {
                item.setStdId(std.getId());
                item.setOrgId(orgId);
                item.setIsDeleted(false);
                fiaInspStdItemMapper.insert(item);
            }
        }
        return std;
    }

    @Override
    @Transactional
    public void update(String id, CreateInspStdRequest req) {
        FiaInspStd existing = fiaInspStdMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "检验标准不存在");
        }
        DataScopeGuard.ensureOwner(existing.getOrgId());
        // 组织 ID 统一解析：优先使用前端传入值（UUID 或 org_code），统一转为真实 UUID
        String orgId = orgIdResolver.resolve(req.getOrgId());
        FiaInspStd std = new FiaInspStd();
        std.setId(id);
        std.setOrgId(orgId);
        std.setCode(req.getCode());
        std.setMaterial(req.getMaterial());
        std.setProcName(req.getProcName());
        std.setSpcProcessId(req.getSpcProcessId());
        std.setAql(req.getAql());
        std.setInspectLevel(req.getInspectLevel());
        std.setSamplePlan(req.getSamplePlan());
        std.setCtqText(req.getCtqText());
        std.setStdVersion(req.getStdVersion());
        std.setStatus(req.getStatus());
        fiaInspStdMapper.updateById(std);
        // 重新关联检验项：先软删除旧项(保留被首件检验录入表 fia_insp_item 引用的历史数据, 避免物理删除触发外键异常), 再写入新项
        fiaInspStdItemMapper.update(null,
                new LambdaUpdateWrapper<FiaInspStdItem>()
                        .eq(FiaInspStdItem::getStdId, id)
                        .set(FiaInspStdItem::getIsDeleted, true));
        if (req.getItems() != null) {
            for (FiaStdItemRequest ir : req.getItems()) {
                FiaInspStdItem it = new FiaInspStdItem();
                it.setStdId(id);
                it.setOrgId(orgId);
                it.setSeq(ir.getSeq());
                it.setItemName(ir.getItemName());
                it.setIsCtq(ir.getIsCtq());
                it.setStdValue(ir.getStdValue());
                it.setTolerance(ir.getTolerance());
                it.setUnit(ir.getUnit());
                it.setValueType(ir.getValueType());
                it.setEnumValues(ir.getEnumValues());
                it.setPassValues(ir.getPassValues());
                it.setChartTypes(ir.getChartTypes());
                it.setIsDeleted(false);
                fiaInspStdItemMapper.insert(it);
            }
        }
    }

    @Override
    @Transactional
    public void changeStatus(String id, String status) {
        if (status == null || !(status.equals("生效") || status.equals("停用"))) {
            throw new BusinessException(400, "仅支持启用(生效)或停用状态切换");
        }
        FiaInspStd existing = fiaInspStdMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "检验标准不存在");
        }
        DataScopeGuard.ensureOwner(existing.getOrgId());
        if ("草稿".equals(existing.getStatus())) {
            throw new BusinessException(400, "草稿标准请先发布为生效状态，或直接删除");
        }
        fiaInspStdMapper.update(null,
                new LambdaUpdateWrapper<FiaInspStd>()
                        .eq(FiaInspStd::getId, id)
                        .set(FiaInspStd::getStatus, status));
    }

    @Override
    @Transactional
    public void delete(String id) {
        FiaInspStd existing = fiaInspStdMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "检验标准不存在");
        }
        DataScopeGuard.ensureOwner(existing.getOrgId());
        // 护栏1：仅草稿状态允许物理删除；生效/停用的标准只允许停用/启用切换，保留历史溯源
        if (!"草稿".equals(existing.getStatus())) {
            throw new BusinessException(400, "仅草稿标准可删除；生效/停用标准请使用停用/启用操作");
        }
        // 护栏2：若该标准的检验项已被 SPC 参数绑定，则拒绝删除，避免悬空外键破坏引用完整性
        List<FiaInspStdItem> items = fiaInspStdItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspStdItem>().eq(FiaInspStdItem::getStdId, id));
        if (!items.isEmpty()) {
            List<String> itemIds = items.stream().map(FiaInspStdItem::getId).toList();
            Long refCount = spcParamMapper.selectCount(
                    new LambdaQueryWrapper<SpcParam>()
                            .in(SpcParam::getFiaStdItemId, itemIds)
                            .eq(SpcParam::getIsDeleted, false));
            if (refCount != null && refCount > 0) {
                throw new BusinessException(400, "该标准的检验项已被 SPC 参数绑定(" + refCount + " 处)，无法删除，请先解除 SPC 参数关联或将其停用");
            }
        }
        // 标准头软删
        fiaInspStdMapper.deleteById(id);
        // 明细逻辑删(与 update 接口一致),保留 is_deleted=true 历史行供 FIA 检验记录/SPC 溯源
        fiaInspStdItemMapper.update(null,
                new LambdaUpdateWrapper<FiaInspStdItem>()
                        .eq(FiaInspStdItem::getStdId, id)
                        .set(FiaInspStdItem::getIsDeleted, true));
    }

    @Override
    public List<CtqItemVo> listCtqItems() {
        // 1. 查询所有生效标准
        List<FiaInspStd> activeStds = fiaInspStdMapper.selectList(
                new LambdaQueryWrapper<FiaInspStd>().eq(FiaInspStd::getStatus, "生效"));
        if (activeStds.isEmpty()) {
            return List.of();
        }
        List<String> activeStdIds = activeStds.stream().map(FiaInspStd::getId).toList();
        Map<String, FiaInspStd> stdMap = activeStds.stream()
                .collect(Collectors.toMap(FiaInspStd::getId, s -> s));

        // 2. 查询这些标准下的所有 CTQ 检验项
        List<FiaInspStdItem> ctqItems = fiaInspStdItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspStdItem>()
                        .in(FiaInspStdItem::getStdId, activeStdIds)
                        .eq(FiaInspStdItem::getIsCtq, true)
                        .eq(FiaInspStdItem::getIsDeleted, false));

        // 3. 组装 VO
        List<CtqItemVo> result = new ArrayList<>();
        for (FiaInspStdItem item : ctqItems) {
            FiaInspStd std = stdMap.get(item.getStdId());
            if (std == null) continue;
            CtqItemVo vo = new CtqItemVo();
            vo.setId(item.getId());
            vo.setStdCode(std.getCode());
            vo.setMaterial(std.getMaterial());
            vo.setProcName(std.getProcName());
            vo.setItemName(item.getItemName());
            vo.setStdValue(item.getStdValue());
            vo.setTolerance(item.getTolerance());
            vo.setUnit(item.getUnit());
            vo.setUpperLimit(item.getUpperLimit());
            vo.setLowerLimit(item.getLowerLimit());
            result.add(vo);
        }
        return result;
    }
}
