package com.konli.qms.api.fia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateInspStdRequest {

    @NotBlank
    private String orgId;          // 所属公司(admin 跨公司需指定)

    @NotBlank
    private String code;

    @NotBlank
    private String material;

    @NotBlank
    private String procName;

    private String aql;
    private String inspectLevel;
    private String samplePlan;
    private String ctqText;
    private String stdVersion;     // v1/v2/v3
    private String status;        // 默认 草稿

    private List<FiaStdItemRequest> items;
}
