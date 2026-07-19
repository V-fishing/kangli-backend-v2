package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.domain.fia.mapper.FiaInspStdItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdMapper;
import com.konli.qms.service.fia.InspStdService;
import com.konli.qms.service.fia.dto.InspStdVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InspStdServiceImpl implements InspStdService {

    private final FiaInspStdMapper fiaInspStdMapper;
    private final FiaInspStdItemMapper fiaInspStdItemMapper;

    @Override
    public List<FiaInspStd> list() {
        return fiaInspStdMapper.selectList(null);
    }

    @Override
    public InspStdVo get(String id) {
        InspStdVo vo = new InspStdVo();
        vo.setStd(fiaInspStdMapper.selectById(id));
        vo.setItems(fiaInspStdItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspStdItem>().eq(FiaInspStdItem::getStdId, id).orderByAsc(FiaInspStdItem::getSeq)));
        return vo;
    }

    @Override
    @Transactional
    public FiaInspStd create(FiaInspStd std, List<FiaInspStdItem> items) {
        if (std.getStatus() == null) {
            std.setStatus("草稿");
        }
        fiaInspStdMapper.insert(std);
        if (items != null) {
            for (FiaInspStdItem item : items) {
                item.setStdId(std.getId());
                item.setOrgId(std.getOrgId());
                fiaInspStdItemMapper.insert(item);
            }
        }
        return std;
    }

    @Override
    public void update(FiaInspStd std) {
        fiaInspStdMapper.updateById(std);
    }

    @Override
    @Transactional
    public void delete(String id) {
        fiaInspStdMapper.deleteById(id);
        fiaInspStdItemMapper.delete(new LambdaQueryWrapper<FiaInspStdItem>().eq(FiaInspStdItem::getStdId, id));
    }
}
