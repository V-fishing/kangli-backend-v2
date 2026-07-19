package com.konli.qms.service.patrol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.patrol.entity.PatlCheckItem;
import com.konli.qms.domain.patrol.entity.PatlCheckpoint;
import com.konli.qms.domain.patrol.entity.PatlRoute;
import com.konli.qms.domain.patrol.mapper.PatlCheckItemMapper;
import com.konli.qms.domain.patrol.mapper.PatlCheckpointMapper;
import com.konli.qms.domain.patrol.mapper.PatlRouteMapper;
import com.konli.qms.service.patrol.PatlRouteService;
import com.konli.qms.service.patrol.dto.CheckpointVo;
import com.konli.qms.service.patrol.dto.PatlRouteVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatlRouteServiceImpl implements PatlRouteService {

    private final PatlRouteMapper patlRouteMapper;
    private final PatlCheckpointMapper patlCheckpointMapper;
    private final PatlCheckItemMapper patlCheckItemMapper;

    @Override
    public List<PatlRoute> list() {
        return patlRouteMapper.selectList(null);
    }

    @Override
    public PatlRouteVo get(String id) {
        PatlRouteVo vo = new PatlRouteVo();
        vo.setRoute(patlRouteMapper.selectById(id));
        List<PatlCheckpoint> checkpoints = patlCheckpointMapper.selectList(
                new LambdaQueryWrapper<PatlCheckpoint>()
                        .eq(PatlCheckpoint::getRouteId, id)
                        .orderByAsc(PatlCheckpoint::getSeq));
        List<CheckpointVo> cvos = new ArrayList<>();
        for (PatlCheckpoint cp : checkpoints) {
            CheckpointVo cvo = new CheckpointVo();
            cvo.setCheckpoint(cp);
            cvo.setItems(patlCheckItemMapper.selectList(
                    new LambdaQueryWrapper<PatlCheckItem>()
                            .eq(PatlCheckItem::getCheckpointId, cp.getId())
                            .orderByAsc(PatlCheckItem::getSeq)));
            cvos.add(cvo);
        }
        vo.setCheckpoints(cvos);
        return vo;
    }

    @Override
    @Transactional
    public PatlRoute create(PatlRoute route, List<CheckpointVo> checkpoints) {
        if (route.getStatus() == null) {
            route.setStatus("启用");
        }
        patlRouteMapper.insert(route);
        if (checkpoints != null) {
            for (CheckpointVo cvo : checkpoints) {
                PatlCheckpoint cp = cvo.getCheckpoint();
                cp.setOrgId(route.getOrgId());
                cp.setRouteId(route.getId());
                if (cp.getNeedPhoto() == null) {
                    cp.setNeedPhoto(false);
                }
                patlCheckpointMapper.insert(cp);
                if (cvo.getItems() != null) {
                    for (PatlCheckItem item : cvo.getItems()) {
                        item.setOrgId(route.getOrgId());
                        item.setCheckpointId(cp.getId());
                        if (item.getIsRequired() == null) {
                            item.setIsRequired(true);
                        }
                        if (item.getCheckType() == null) {
                            item.setCheckType("enum");
                        }
                        patlCheckItemMapper.insert(item);
                    }
                }
            }
        }
        return route;
    }

    @Override
    public void update(PatlRoute route) {
        patlRouteMapper.updateById(route);
    }

    @Override
    @Transactional
    public void delete(String id) {
        // 级联删除:先查点位 -> 删每个点位的检查项 -> 删点位 -> 删路线
        List<PatlCheckpoint> checkpoints = patlCheckpointMapper.selectList(
                new LambdaQueryWrapper<PatlCheckpoint>().eq(PatlCheckpoint::getRouteId, id));
        for (PatlCheckpoint cp : checkpoints) {
            patlCheckItemMapper.delete(new LambdaQueryWrapper<PatlCheckItem>()
                    .eq(PatlCheckItem::getCheckpointId, cp.getId()));
        }
        patlCheckpointMapper.delete(new LambdaQueryWrapper<PatlCheckpoint>()
                .eq(PatlCheckpoint::getRouteId, id));
        patlRouteMapper.deleteById(id);
    }
}
