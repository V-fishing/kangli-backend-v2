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
}
