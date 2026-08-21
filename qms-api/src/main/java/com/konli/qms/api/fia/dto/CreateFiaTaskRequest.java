package com.konli.qms.api.fia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateFiaTaskRequest {

    @NotBlank
    private String orgId;          // 所属公司

    /** 工单号(可空,为空时后端自动生成 WO-yyyyMMdd-XXX) */
    private String woNo;

    /** 产线(非必填,前端已删除此字段) */
    private String lineName;

    @NotBlank
    private String productName;

    /** 过程工序(来自 SpcProcess 工序管理) */
    @NotBlank
    private String procName;

    @NotBlank
    private String triggerType;    // -> fia_trigger_type.name

    /** 首件工序: material(物料)/semi(半成品)/product(成品) */
    @NotBlank
    private String category;

    private String stdId;          // 检验标准(可选,为空时按料号+category自动匹配)

    /** 选中的标准项 ID 列表;为空或不传则按标准全量生成检验项 */
    private List<String> stdItemIds;

    /** 来料批次驱动匹配键:物料编码 + 供应商ID;用于自动匹配标准库 */
    private String partNo;
    private String supplierId;
    private String lotId;

    private String batchNo;
    private Boolean isUrgent;

    /** 供应商送检信息(供应商/送货单/联系人等),统一归入任务备注,前端拼接后传入 */
    private String remark;

    /** 关联物料变更单 ID(可空)。非空时后端以变更单的 supplier_id/part_no 强制覆盖并置 source=SUPPLIER */
    private String changeId;

    /** 来源: FACTORY(产线首件)/SUPPLIER(供应商来料首件)/TOOLING(工装首件)，可空由后端按规则推导 */
    private String source;

    // ---- 完工检验补收字段(trigger_type='完工检验' 时使用,普通首件忽略) ----

    /** 选中的最近合格首件任务 ID(可空);非空时以该首件的工单号/料号带出生产字段 */
    private String eligibleFirstArticleId;

    /** 型号规格 */
    private String modelSpec;

    /** 生产日期(YYYY-MM-DD) */
    private String productionDate;

    /** 提交数量 */
    private java.math.BigDecimal submittedQty;

    /** 单位 */
    private String unit;

    /** 工厂编码 */
    private String plantCode;

    /** 工厂名称 */
    private String plantName;
}
