package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmTraceNode;

import java.util.List;

/** 来料批次 + 追溯节点:创建/查询/追溯树。sqm.trace.* */
public interface SqmTraceService {

    List<SqmIncomingLot> listLots();

    SqmIncomingLot createLot(SqmIncomingLot lot);

    /** 查询某根来料批次的直接子节点。 */
    List<SqmTraceNode> listNodes(String rootLotId);

    SqmTraceNode createNode(SqmTraceNode node);

    /** 递归查整棵追溯树(扁平列表,按 treeLevel ASC)。 */
    List<SqmTraceNode> traceTree(String rootLotId);
}
