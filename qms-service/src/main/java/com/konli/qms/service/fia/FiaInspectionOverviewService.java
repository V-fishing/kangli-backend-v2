package com.konli.qms.service.fia;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.service.fia.dto.MergedInspectionVO;

/**
 * 完工检验 + 物料检验 合并视图(列表「全部」档):UNION ALL 直读 MES 两张表。
 */
public interface FiaInspectionOverviewService {

    /** 合并分页:物料编码 / 关键字(编号·订单·名称·供应商·批次)跨两表过滤,按 created_at 倒序。 */
    PageResult<MergedInspectionVO> page(int page, int size, String materialCode, String keyword);
}
