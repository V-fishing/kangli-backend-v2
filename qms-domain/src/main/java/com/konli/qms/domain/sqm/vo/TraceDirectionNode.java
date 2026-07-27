package com.konli.qms.domain.sqm.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单条追溯结果节点（正向/反向/全部），包含层级深度 depth（相对起始节点）
 */
@Data
public class TraceDirectionNode {
    private String id;
    private String nodeType;
    private String nodeName;
    private String batchNo;
    private BigDecimal qty;
    private String unit;
    private String nodeDate;
    private String supplierName;
    private String isValid;
    private Integer treeLevel;
    private Integer depth;
}
