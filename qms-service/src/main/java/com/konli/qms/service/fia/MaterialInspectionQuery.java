package com.konli.qms.service.fia;

import lombok.Data;

/**
 * 物料检验(来料检验)分页查询条件。
 */
@Data
public class MaterialInspectionQuery {

    /** 当前组织 id(用于工厂归属与日志,不强制过滤 MES 推送数据) */
    private String orgId;

    /** 供应商名称(精确/前缀,空=不限) */
    private String supplierName;

    /** 物料编码(精确/前缀,空=不限) */
    private String materialCode;

    /** 检验申请号(空=不限) */
    private String inspectionRequestNo;

    /** 记录编号(来料批次 lot_no,空=不限) */
    private String recordNo;

    /** 判定结论:合格/不合格/让步接收等(空=不限) */
    private String inspectionResult;

    /** 关键字(命中物料编码/物料名称/供应商/批次号/记录编号,空=不限) */
    private String keyword;

    private int page = 1;
    private int size = 20;
}
