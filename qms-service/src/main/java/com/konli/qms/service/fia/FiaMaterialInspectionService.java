package com.konli.qms.service.fia;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.service.fia.dto.MaterialInspectionCreateRequest;
import com.konli.qms.service.fia.dto.MaterialInspectionUpdateRequest;
import com.konli.qms.service.fia.dto.MaterialInspectionVO;

import java.util.List;

/**
 * 物料检验(来料检验)独立模块:直读直写 MES qms.material_inspection(62 列全 text、无主键)。
 * 不复用首件主表/不建检验项明细/不接审批流;以 record_no 作为稳定行定位键。
 */
public interface FiaMaterialInspectionService {

    /** 物料编码下拉(MES 已存在的 material_code DISTINCT,支持关键字过滤)。 */
    List<String> listMesMaterialCodes(String orgId, String keyword);

    /** 分页列表(过滤 供应商/物料编码/检验申请号/记录编号/判定结论/关键字)。 */
    PageResult<MaterialInspectionVO> page(MaterialInspectionQuery query);

    /** 详情(按 record_no 定位)。 */
    MaterialInspectionVO get(String id);

    /** 建单(INSERT 新行;工厂/组织按当前用户组织自动带入)。 */
    String create(MaterialInspectionCreateRequest req);

    /** 保存检验汇总(检验员 + 判定结论 + 缺陷/处理方式)。 */
    void updateInspection(String id, MaterialInspectionUpdateRequest req);

    /** 保存数量信息与审核签核(表单直填)。 */
    void updateSignoff(String id, MaterialInspectionUpdateRequest req);

    /** 软删除(置 is_deleted='1',新建行归属隔离,不误删 MES 推送数据)。 */
    void softDelete(String id);
}
