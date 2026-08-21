package com.konli.qms.service.fia;

import com.konli.qms.service.fia.dto.FinishInspectionCreateRequest;
import com.konli.qms.service.fia.dto.FinishInspectionUpdateRequest;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.service.fia.dto.EligibleFirstArticleVO;
import com.konli.qms.service.fia.dto.FinishInspectionVO;

import java.util.List;

/**
 * 完工检验独立模块:直读直写 MES qms.finished_goods_inspection(40 列全 text、无主键)。
 * 不复用首件主表/不建检验项明细/不接审批流;首件绑定仅按 production_order_no 查询展示(软提示)。
 */
public interface FiaFinishInspectionService {

    /** 生产订单号下拉(MES 已存在的 production_order_no DISTINCT,支持关键字过滤)。 */
    List<String> listMesProductionOrders(String orgId, String keyword);

    /** 分页列表(过滤 category / 生产订单号 / 物料编码 / 关键字)。 */
    PageResult<FinishInspectionVO> page(FinishInspectionQuery query);

    /** 详情(按行 id)。 */
    FinishInspectionVO get(String id);

    /** 建单(INSERT 新行;工厂/组织按当前用户组织自动带入)。 */
    String create(FinishInspectionCreateRequest req);

    /** 保存检验汇总(检验员 + 判定结论)。 */
    void updateInspection(String id, FinishInspectionUpdateRequest req);

    /** 保存数量信息与审核签核(表单直填)。 */
    void updateSignoff(String id, FinishInspectionUpdateRequest req);

    /** 软删除(置 is_deleted='1',新行归属隔离,不误删 MES 推送数据)。 */
    void softDelete(String id);

    /** 首件软绑定查询:按 production_order_no 查 fia_task 中同生产订单号且非完工检验的首件。 */
    List<EligibleFirstArticleVO> listBoundFirstArticles(String id);
}
