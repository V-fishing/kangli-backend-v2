package com.konli.qms.domain.sqm.vo;

import com.konli.qms.domain.sqm.entity.SqmTraceNode;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 节点完整详情（表 + 树共用）：主表全部字段 + 明细字段 + 供应商 + 组成关系
 */
@Data
public class TraceNodeFullVO {
    /** 节点主表全部字段 */
    private SqmTraceNode node;
    /** 明细表（原材料/成品/客户）字段，camelCase */
    private Map<String, Object> detail;
    /** 供应商名称 */
    private String supplierName;
    /** 正向（用于）：包含本节点的上层节点 */
    private List<TraceLinkRef> parents;
    /** 反向（组成）：本节点包含的下层节点 */
    private List<TraceLinkRef> children;
}
