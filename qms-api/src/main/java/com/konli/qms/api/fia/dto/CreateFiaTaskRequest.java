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

    @NotBlank
    private String stdId;          // 检验标准

    private String batchNo;
    private Boolean isUrgent;
}
