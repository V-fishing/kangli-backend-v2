package com.konli.qms.domain.sqm.vo;

import lombok.Data;

/**
 * 完整追溯树响应 VO。
 * tree 为已构建好的根节点(含其下整棵 children 树);
 * 调用方也可使用 flat 做扁平处理(本实现仅返回 tree)。
 */
@Data
public class TraceFullTreeVO {

    private String rootLotId;
    private String rootLotNo;
    private String rootNodeId;
    private Boolean isKeyPart;
    private TraceNodeTreeVO tree;

    /**
     * 上游组成树(仅 tree-from-node 接口填充):以选中节点为根,
     * children 为其上游来源(如 成品 ← 半成品 ← 来料),用于"同时展示上下游"。
     */
    private TraceNodeTreeVO upTree;
}
