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
    /** 详情来源: finished_goods_inspection / material_inspection / binding(critical_material_binding 兜底) / product_no(料号聚合)。前端如实标注, 不编造。 */
    private String detailSource;
    /** 源表缺失 material_barcode 字段(如绑定表 material_barcode 为空的来料/半成品子件), 节点仍展示但标注"无 material_barcode"。 */
    private Boolean noBarcode;
    private List<TraceNodeTreeVO> children = new ArrayList<>();
}
