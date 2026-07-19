package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 追溯节点(自引用树,WITH RECURSIVE 正/逆向)。
 * root_lot_id -> 根来料批次;parent_node_id -> 父节点(自引用)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_trace_node")
public class SqmTraceNode extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("root_lot_id")
    private String rootLotId;

    @TableField("parent_node_id")
    private String parentNodeId;

    @TableField("node_type")
    private String nodeType;              // incoming/raw/semi/ship/customer

    @TableField("node_name")
    private String nodeName;

    @TableField("batch_no")
    private String batchNo;

    private BigDecimal qty;               // NUMERIC(14,2)

    private String unit;

    @TableField("node_date")
    private LocalDate nodeDate;

    @TableField("supplier_id")
    private String supplierId;

    private String remark;

    @TableField("tree_level")
    private Integer treeLevel;

    @TableField("is_valid")
    private String isValid;               // 是/否

    @TableField("invalid_by")
    private String invalidBy;

    @TableField("invalid_time")
    private LocalDateTime invalidTime;
}
