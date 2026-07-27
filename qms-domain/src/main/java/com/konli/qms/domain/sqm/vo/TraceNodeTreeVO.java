package com.konli.qms.domain.sqm.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 追溯树节点 VO(用于前端渲染嵌套树)。
 * 在 {@link com.konli.qms.domain.sqm.entity.SqmTraceNode} 基础上附加:
 *  - detail:按 nodeType 关联的明细表行(snake_case 列已转为 camelCase)
 *  - supplierName:supplierId 对应的供应商名称
 *  - children:子节点(后端已构建好树)
 */
@Data
public class TraceNodeTreeVO {

    private String id;
    private String rootLotId;
    private String parentNodeId;
    private String nodeType;
    private String nodeName;
    private String batchNo;
    private String materialCode;
    private BigDecimal qty;
    private String unit;
    private String nodeDate;
    private String supplierId;
    private String supplierName;
    private String remark;
    private Integer treeLevel;
    private String isValid;

    private Map<String, Object> detail;
    private List<TraceNodeTreeVO> children = new ArrayList<>();
}
