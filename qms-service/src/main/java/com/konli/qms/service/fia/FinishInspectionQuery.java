package com.konli.qms.service.fia;

import lombok.Data;

/**
 * 完工检验分页查询条件。
 */
@Data
public class FinishInspectionQuery {

    /** 当前组织 id(用于工厂归属与日志,不强制过滤 MES 推送数据) */
    private String orgId;

    /** 品类过滤:成品/半成品(空=全部) */
    private String category;

    /** 生产订单号(精确/前缀,空=不限) */
    private String productionOrderNo;

    /** 物料编码(精确/前缀,空=不限) */
    private String materialCode;

    /** 关键字(命中生产订单号/物料编码/产品名称,空=不限) */
    private String keyword;

    private int page = 1;
    private int size = 20;
}
