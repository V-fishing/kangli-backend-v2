package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmTraceNode;
import com.konli.qms.domain.sqm.mapper.SqmIncomingLotMapper;
import com.konli.qms.domain.sqm.mapper.SqmTraceNodeMapper;
import com.konli.qms.service.sqm.SqmTraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmTraceServiceImpl implements SqmTraceService {

    private final SqmIncomingLotMapper sqmIncomingLotMapper;
    private final SqmTraceNodeMapper sqmTraceNodeMapper;

    @Override
    public List<SqmIncomingLot> listLots() {
        return sqmIncomingLotMapper.selectList(null);
    }

    @Override
    @Transactional
    public SqmIncomingLot createLot(SqmIncomingLot lot) {
        if (lot.getLotNo() == null) {
            lot.setLotNo("LOT-" + System.currentTimeMillis());
        }
        if (lot.getInspectResult() == null) {
            lot.setInspectResult("待检");
        }
        sqmIncomingLotMapper.insert(lot);
        return lot;
    }

    @Override
    public List<SqmTraceNode> listNodes(String rootLotId) {
        return sqmTraceNodeMapper.selectList(
                new LambdaQueryWrapper<SqmTraceNode>()
                        .eq(SqmTraceNode::getRootLotId, rootLotId));
    }

    @Override
    @Transactional
    public SqmTraceNode createNode(SqmTraceNode node) {
        if (node.getTreeLevel() == null) {
            node.setTreeLevel(0);
        }
        if (node.getIsValid() == null) {
            node.setIsValid("是");
        }
        sqmTraceNodeMapper.insert(node);
        return node;
    }

    @Override
    public List<SqmTraceNode> traceTree(String rootLotId) {
        // 按 treeLevel ASC 返回扁平列表,前端构建树
        return sqmTraceNodeMapper.selectList(
                new LambdaQueryWrapper<SqmTraceNode>()
                        .eq(SqmTraceNode::getRootLotId, rootLotId)
                        .orderByAsc(SqmTraceNode::getTreeLevel));
    }
}
