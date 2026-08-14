package com.konli.qms.service.fia.dto;

import lombok.Data;

import java.math.BigDecimal;

/** CTQ 检验项 VO(供 SPC 参数关联选择器用,含父标准信息) */
@Data
public class CtqItemVo {
    /** FiaInspStdItem.id */
    private String id;
    /** 父标准编码 */
    private String stdCode;
    /** 父标准物料 */
    private String material;
    /** 父标准工序 */
    private String procName;
    /** 检验项名称 */
    private String itemName;
    /** 标准值 */
    private String stdValue;
    /** 公差 */
    private String tolerance;
    /** 单位 */
    private String unit;
    /** 规格上限 */
    private BigDecimal upperLimit;
    /** 规格下限 */
    private BigDecimal lowerLimit;
}
