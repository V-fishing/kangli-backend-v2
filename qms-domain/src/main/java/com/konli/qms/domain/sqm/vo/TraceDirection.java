package com.konli.qms.domain.sqm.vo;

/**
 * 追溯方向。
 * FORWARD  = 正向(向下追溯):沿 parent→child,从本节点向下游/去向展开,直到物料(来料/关键件)。
 * BACKWARD = 反向(向上追溯):沿 child→parent,从本节点向上游/来源展开,直到成品顶端(可追加客户占位)。
 * ALL      = 全链路:同时返回下游去向树(tree)与上游来源树(upTree)。
 */
public enum TraceDirection {
    FORWARD,
    BACKWARD,
    ALL;

    /** 容错解析:null 或无法识别一律归为 ALL,保证旧调用默认全链路。 */
    public static TraceDirection of(String s) {
        if (s == null) return ALL;
        switch (s.trim().toUpperCase()) {
            case "FORWARD":
            case "DOWN":
            case "下":
            case "正向":
                return FORWARD;
            case "BACKWARD":
            case "UP":
            case "上":
            case "反向":
                return BACKWARD;
            default:
                return ALL;
        }
    }
}
