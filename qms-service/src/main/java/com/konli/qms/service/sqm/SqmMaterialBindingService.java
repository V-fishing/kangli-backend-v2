package com.konli.qms.service.sqm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.service.sqm.dto.MaterialBindingCreateRequest;
import com.konli.qms.service.sqm.dto.MaterialBindingUpdateRequest;

import java.util.List;
import java.util.Map;

/**
 * MES 关键物料绑定管理: 直写 qms.critical_material_binding, 与 MES 推送同行同表。
 * 手动维护的绑定行会被 SqmTraceServiceImpl.traceMesTree 追溯树自动读取, 无需改造追溯逻辑。
 */
public interface SqmMaterialBindingService {

    /** 分页列表: keyword 模糊匹配父/子条码与名称, category/isActive 可选筛选。 */
    PageResult<Map<String, Object>> list(String keyword, String category, String isActive, int page, int size);

    /**
     * 绑定候选搜索(完工检验「绑定父子级」用):
     * role=child 搜子件(半成品 finished + 来料 material); role=parent 搜父级(成品/半成品 finished)。
     * 返回可直接落绑定行的候选(barcode/code/name/spec/category)。
     */
    List<Map<String, Object>> candidates(String role, String keyword, int limit);

    /** 新增绑定行: INSERT 业务列, 应用维护审计列, is_active=1/is_deleted=0。 */
    Map<String, Object> create(MaterialBindingCreateRequest req);

    /** 编辑绑定行: 仅改传入的字段, 定位键(product_barcode+material_barcode+category)不可改。 */
    Map<String, Object> update(String productBarcode, String materialBarcode, String category,
                               MaterialBindingUpdateRequest req);

    /** 停用绑定: is_active=0 + 停用操作人/时间。 */
    void deactivate(String productBarcode, String materialBarcode, String category);

    /** 软删绑定: is_deleted=1。 */
    void delete(String productBarcode, String materialBarcode, String category);
}
