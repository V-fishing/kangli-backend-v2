package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.DataScopeGuard;
import com.konli.qms.domain.fia.dto.CreateInspStdRequest;
import com.konli.qms.domain.fia.dto.FiaStdItemRequest;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.domain.fia.mapper.FiaInspStdItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdMapper;
import com.konli.qms.service.fia.InspStdService;
import com.konli.qms.service.fia.dto.InspStdVo;
import com.konli.qms.service.support.OrgIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InspStdServiceImpl implements InspStdService {

    private final FiaInspStdMapper fiaInspStdMapper;
    private final FiaInspStdItemMapper fiaInspStdItemMapper;
    private final OrgIdResolver orgIdResolver;

    @Override
    public List<FiaInspStd> list() {
        return fiaInspStdMapper.selectList(null);
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
                it.setIsDeleted(false);
                fiaInspStdItemMapper.insert(it);
            }
        }
    }

    @Override
    @Transactional
    public void delete(String id) {
        FiaInspStd existing = fiaInspStdMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "检验标准不存在");
        }
        DataScopeGuard.ensureOwner(existing.getOrgId());
        fiaInspStdMapper.deleteById(id);
        fiaInspStdItemMapper.delete(new LambdaQueryWrapper<FiaInspStdItem>().eq(FiaInspStdItem::getStdId, id));
    }
}
