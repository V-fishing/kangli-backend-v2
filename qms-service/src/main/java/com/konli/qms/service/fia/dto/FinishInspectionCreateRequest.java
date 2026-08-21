package com.konli.qms.service.fia.dto;

import lombok.Data;

/**
 * 完工检验建单请求(直写 MES qms.finished_goods_inspection,无主键/唯一约束,后端 INSERT 新行)。
 * 工厂/组织按当前登录用户组织自动带入,前端可不传。
 */
@Data
public class FinishInspectionCreateRequest {

    /** 生产订单号(MES 生产订单号,如 MO0 或 WTM 前缀,必填,下拉选择) */
    private String productionOrderNo;

    /** 物料编码(必填,按物料带出型号规格/单位) */
    private String materialCode;

    /** 产品名称 */
    private String productName;

    /** 型号规格(按物料带出,可覆盖) */
    private String modelSpec;

    /** 生产批次/序列号 */
    private String prodBatchOrSn;

    /** 生产日期(YYYY-MM-DD) */
    private String productionDate;

    /** 类别:成品/半成品 */
    private String category;

    /** 加急 */
    private Boolean isUrgent;

    /** 是否委托 */
    private Boolean isEntrusted;

    /** 单位(按物料带出) */
    private String unit;

    /** 工厂编码(自动带入,可覆盖) */
    private String plantCode;

    /** 工厂名称(自动带入,可覆盖) */
    private String plantName;
}
