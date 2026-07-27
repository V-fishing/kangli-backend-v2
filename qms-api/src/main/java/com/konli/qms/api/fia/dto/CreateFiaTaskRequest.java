package com.konli.qms.api.fia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateFiaTaskRequest {

    @NotBlank
    private String orgId;          // 所属公司

    @NotBlank
    private String woNo;

    @NotBlank
    private String lineName;

    @NotBlank
    private String productName;

    @NotBlank
    private String procName;

    @NotBlank
    private String triggerType;    // -> fia_trigger_type.name

    private String stdId;          // 检验标准(可选,为空时按物料+工序/物料编码+供应商自动匹配)

    /** 来料批次驱动匹配键:物料编码 + 供应商ID;用于自动匹配标准库 */
    private String partNo;
    private String supplierId;
    private String lotId;

    private String batchNo;
    private Boolean isUrgent;

    /** 供应商送检信息(供应商/送货单/联系人等),统一归入任务备注,前端拼接后传入 */
    private String remark;
}
