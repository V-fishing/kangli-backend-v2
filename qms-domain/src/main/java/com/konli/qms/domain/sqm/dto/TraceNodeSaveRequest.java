package com.konli.qms.domain.sqm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 追溯节点录入请求(saveNode / attachComponent 共用)。
 * 放在 domain 层以便 service 与 api(controller) 共同引用, 避免 api->service 反向依赖。
 */
@Data
public class TraceNodeSaveRequest {

    private String orgId;
    private String rootLotId;         // 根节点挂载到来料批次; 有父节点时可从父节点推导
    private String nodeType;          // semi / ship / customer
    private String parentNodeId;      // 挂到哪个父节点下; 不传则视为树根
    private String nodeName;
    private String batchNo;
    private BigDecimal qty;
    private String unit;
    private LocalDate nodeDate;
    private String supplierId;
    private String remark;
    private String qualificationType; // 合格 / 资格直通 / 常规

    // ---- 产出明细(semi/ship) ----
    private String productName;
    private String materialCode;
    private String modelSpec;
    private String productionOrderNo;
    private LocalDate productionDate;
    private BigDecimal inspectQty;
    private String inspector;

    // ---- 药监合规(仅 ship) ----
    private String drugRegNo;
    private String perfInspectMethod;
    private String perfBatchNo;

    // ---- 客户出货(仅 customer) ----
    private String customerName;
    private String customerCode;
    private String customerOrderNo;
    private LocalDate shipDate;
    private String trackingNo;
    private String shipAddress;
    private String contactPerson;
    private String contactPhone;

    // ---- 一次性建树模式: 建头节点时直接带组成 ----
    private List<ComponentItem> components;

    @Data
    public static class ComponentItem {
        private String componentType;   // raw / semi
        private String sourceNodeId;    // semi: 引用已存在半成品节点, 拷贝其产品身份
        private String refNodeId;       // 引用已存在节点(不新建, 直接建 link), 用于多对多组成
        private String materialCode;    // raw: 物料编码(=批次号)
        private String materialName;    // raw: 物料名称
        private String specModel;       // raw: 规格型号
        private BigDecimal usageQty;    // 用量
        private String unit;
        private String processName;     // 工序
    }
}
