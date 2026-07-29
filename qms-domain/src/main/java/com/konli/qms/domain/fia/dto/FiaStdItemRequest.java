package com.konli.qms.domain.fia.dto;

import lombok.Data;

@Data
public class FiaStdItemRequest {

    private Integer seq;
    private String itemName;
    private Boolean isCtq;
    private String stdValue;
    private String tolerance;
    private String unit;
    private String valueType;     // numeric/enum
    private String enumValues;    // "合格,不合格"
    private String passValues;    // 合格值(逗号分隔);枚举型实测命中即判合格
}
