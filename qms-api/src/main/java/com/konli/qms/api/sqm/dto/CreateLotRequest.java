package com.konli.qms.api.sqm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 创建来料批次请求 */
@Data
public class CreateLotRequest {

    private String orgId;

    private String supplierId;

    private String partNo;

    private String partName;

    private BigDecimal qty;

    private String unit;

    private LocalDate incomingDate;

    private String inspectResult;

    private String inspectType;

    private String poNo;

    private Boolean isKeyPart;
}
