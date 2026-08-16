package com.konli.qms.service.sqm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmKeyPartSn;
import com.konli.qms.domain.sqm.entity.SqmTraceNode;
import com.konli.qms.domain.sqm.entity.SqmTraceProductDetail;
import com.konli.qms.domain.sqm.entity.SqmTraceRawDetail;
import com.konli.qms.domain.sqm.vo.TraceDirection;
import com.konli.qms.domain.sqm.vo.TraceDirectionNode;
import com.konli.qms.domain.sqm.vo.TraceFullTreeVO;
import com.konli.qms.domain.sqm.dto.TraceNodeSaveRequest;
import com.konli.qms.domain.sqm.vo.TraceNodeFullVO;
import com.konli.qms.domain.sqm.vo.TraceNodeSearchVO;

import java.util.List;

/** 来料批次 + 追溯节点:创建/查询/追溯树。sqm.trace.* */
public interface SqmTraceService {

    /** 来料批次列表,支持 keyword 模糊搜索（批次号/物料编码/物料名称）。
     *  supplierId/iqcPass 为可选过滤,用于前端"供应商→合格物料"级联下拉(只列已通过 IQC 的来料)。 */
    List<SqmIncomingLot> listLots(String keyword, String supplierId, Boolean iqcPass);

    /** 来料批次分页列表,支持 keyword 模糊搜索(批次号/物料编码/物料名称) + orgId 组织隔离。
     *  供追溯列表页大数据量分页加载,避免一次性返回全量导致前端卡顿。 */
    PageResult<SqmIncomingLot> listLotsPage(String keyword, String supplierId, String orgId, int page, int size);

    /** 按批次号查询来料批次(批次号全局唯一)。 */
    SqmIncomingLot getLotByLotNo(String lotNo);

    /**
     * 方案 B 源表分页: 按 type 路由三源表, 返回分页业务记录(含 bizKey / bizType 展示字段)。
     * type ∈ {material, semi, finished, all}:
     *  - material: qms.material_inspection, 搜索 material_code/material_name/material_barcode/material_batch_no
     *  - semi:     qms.finished_goods_inspection(category='半成品'), 搜索 prod_batch_or_sn/material_code/product_name
     *  - finished: qms.finished_goods_inspection(category='成品'),     搜索同上
     *  - all:     仅返回各类型的计数(供前端总表分 tab 显示), 不做跨表 UNION 排序
     * plantCode 用于组织隔离(三源表仅 plant_code 文本列), 为空表示全局视图。
     */
    PageResult<java.util.Map<String, Object>> sourcePage(String type, String keyword, String plantCode, String bizType, int page, int size);

    SqmIncomingLot createLot(SqmIncomingLot lot);

    /**
     * 按 组织 + 物料编码(优先) / 物料名称(兜底) + 供应商 查已存在的来料批次。
     * 用于首件合格免审联动去重:同产品已登记过来料追溯则直接复用,避免重复录入。
     */
    SqmIncomingLot findExistingLot(String orgId, String partNo, String partName, String supplierId);

    /**
     * 查询某来料批次对应的「来料」根节点。批量投料时,产出的半成品节点需要把
     * parentNodeId 指向该根节点,才能保证追溯树(parent_node_id 递归)把 来料→半成品 连起来。
     */
    SqmTraceNode getIncomingNode(String rootLotId);

    /** 查询某根来料批次的直接子节点。 */
    List<SqmTraceNode> listNodes(String rootLotId);

    /**
     * 全局追溯节点检索(不限定具体来料批次)。可按对象类型(nodeType)与关键字
     * (节点名/批次号模糊匹配)过滤,返回附带根批次号与供应商名的扁平列表。
     * 用于直接检索 半成品/物料/客户 等对象,而无需先选定某来料批次。
     */
    PageResult<TraceNodeSearchVO> searchNodes(String nodeType, String keyword, String orgId, int page, int size);

    SqmTraceNode createNode(SqmTraceNode node);

    /** 递归查整棵追溯树(扁平列表,按 treeLevel ASC)。 */
    List<SqmTraceNode> traceTree(String rootLotId);

    // ---- 追溯明细 ----

    /** 按追溯节点查询原料追溯明细。 */
    SqmTraceRawDetail getRawDetail(String nodeId);

    /** 原料追溯明细 upsert(按 nodeId:有则 update,无则 insert)。 */
    void saveRawDetail(SqmTraceRawDetail detail);

    /** 按追溯节点查询产品追溯明细。 */
    SqmTraceProductDetail getProductDetail(String nodeId);

    /** 产品追溯明细 upsert(按 nodeId:有则 update,无则 insert)。 */
    void saveProductDetail(SqmTraceProductDetail detail);

    // ---- 关键件 SN ----

    /** 列出某来料批次下的所有关键件 SN。 */
    List<SqmKeyPartSn> listKeyPartSn(String lotId);

    /** 新建关键件 SN。 */
    void createKeyPartSn(SqmKeyPartSn sn);

    /** 批量追溯录入:一次投料操作(支持混批),事务内建节点+投料详情+产出详情+SN+更新批次余量 */
    void batchCreate(SqmIncomingLot lot, List<SqmTraceNode> nodes,
                     List<SqmTraceRawDetail> rawDetails,
                     List<SqmTraceProductDetail> productDetails,
                     List<SqmKeyPartSn> sns);

    // ---- 递归树 ----

    /**
     * 用 WITH RECURSIVE 查询整棵追溯树(扁平列表,按 tree_level, node_date 排序)。
     * 与 {@link #traceTree(String)} 区别:本方法沿 parent_node_id 递归,可跨 root_lot_id 边界。
     */
    List<SqmTraceNode> traceTreeRecursive(String rootLotId);

    /**
     * 完整追溯树:返回已构建好嵌套 children 的根节点,并随每个节点附带其明细表
     * (raw/product/customer,列名已转 camelCase)与供应商名称。前端单接口即可渲染整棵追溯树。
     */
    TraceFullTreeVO getFullTraceTree(String rootLotId);

    // ---- 新录入模型(无来料批次依赖) ----

    /** 建一个产出/头节点(semi/ship/customer) + 明细, 可选一次性带组成。返回新建节点。 */
    SqmTraceNode saveNode(TraceNodeSaveRequest req);

    /** 给某节点挂一个组成(raw/semi), 返回新建子节点。 */
    SqmTraceNode attachComponent(String parentId, TraceNodeSaveRequest.ComponentItem item);

    /** 按树根节点 id 取完整嵌套树(无来料批次也能用)。 */
    TraceFullTreeVO getFullTraceTreeByRootNode(String rootNodeId);

    /** 以任意节点(来料/半成品/成品…)为根、仅向下展开其下游子树(正向/向前追溯)。 */
    TraceFullTreeVO getTraceTreeFromNode(String nodeId);

    /** 按树根节点 id 取扁平树。 */
    List<SqmTraceNode> traceTreeByRootNode(String rootNodeId);

    /** 列出当前组织的全部追溯树根节点(treeLevel=0), 供工作台选择打开。 */
    List<SqmTraceNode> listRoots(String orgId);

    /** 查询节点完整详情(主表全部字段 + 明细 + 供应商 + 组成关系)。 */
    TraceNodeFullVO getNodeDetail(String nodeId);

    /**
     * 按方向追溯:forward=正向(本节点被哪些上层节点使用,即"流向"),
     * backward=反向(本节点由哪些下层节点组成,即"来源"),其他值=全部连通分量。
     */
    List<TraceDirectionNode> traceDirection(String nodeId, String direction);

    /**
     * 方案 B: 以源表业务条码(来料 material_barcode / 成品 prod_batch_or_sn / 半成品 prod_batch_or_sn)为根,
     * 沿 sqm_trace_relation 递归其上下游连通分量, 回查三源表拼出完整嵌套追溯树。
     * 节点权威 = 三源表, 关系表仅存边。direction 控制返回方向(默认 ALL=全链路)。
     */
    TraceFullTreeVO traceMesTree(String barcode, String orgId);

    /** 同 {@link #traceMesTree(String, String)} 但按方向裁剪:forward=下游树, backward=上游树(含客户占位), all=双向。 */
    TraceFullTreeVO traceMesTree(String barcode, String orgId, TraceDirection direction);

    /**
     * 按批号查产品树:以来料批次号(material_batch_no)或成品/半成品批号(prod_batch_or_sn)反查命中条码,
     * 对每个命中条码调用 traceMesTree 并合并为森林(可能多棵根树)。direction 同 traceMesTree。
     */
    java.util.List<TraceFullTreeVO> traceByBatchNo(String batchNo, String orgId, TraceDirection direction);

    /**
     * 列出系统里真实存在过的生产工单号(MES 落地宽表 finished_goods_inspection.production_order_no
     * 与 critical_material_binding.work_order_no 并集去重)。
     * 供工装派工 / 不良登记等场景的"工单号"下拉,保证填写的是系统里真实存在过的生产工单。
     * keyword 可选(包含匹配,防一次性渲染 2.8 万条 DOM 卡死);limit 默认 200 上限 500。
     */
    List<String> listProductionOrders(String orgId, String keyword, Integer limit);

    /**
     * 按来料批次号(lotNo, 即 sqm_incoming_lot.lot_no, 对应源表 record_no)查 MES 追溯森林。
     * 先由 lotNo 在 sqm_incoming_lot 定位其 material_barcode 集合(一个批次号可能对应多条来料记录,
     * 故可能多棵根树),对每个条码调用 traceMesTree 并合并为森林。无对应条码时返回空森林。
     */
    java.util.List<TraceFullTreeVO> traceByLotNo(String lotNo, String orgId, TraceDirection direction);

    /**
     * 方案 B: 往 ops.sqm_trace_relation 插入一条 parent→child 的条码边。
     * 用于在前端物料表页签手动把关键件(keypart)/来料批(incoming)关联到半成品/成品。
     * orgId 在内部用写入型 resolve 解析(防伪造), 边已存在则幂等跳过, 防自环。
     */
    void saveRelation(String parentBarcode, String childBarcode, String relationType, String orgId);

    /**
     * 源表明细查询:按 sourceType 路由三源表,以业务条码(key)反查并复用 toDetailMap 返回全字段 Map。
     * sourceType ∈ {material, finished, semi, critical}:
     *  - material:  来料检验 qms.material_inspection, key=material_barcode(兼容 material_batch_no)
     *  - finished:  成品检验 qms.finished_goods_inspection(category='成品'), key=prod_batch_or_sn
     *  - semi:      半成品检验 qms.finished_goods_inspection(category='半成品'), key=prod_batch_or_sn
     *  - critical:  关键件绑定 qms.critical_material_binding, key=product_barcode(兼容 work_order_no)
     * 返回剔除别名列与审计列后的业务字段 Map,供前端分组卡片弹窗渲染。
     */
    java.util.Map<String, Object> getSourceDetail(String sourceType, String key);
}
