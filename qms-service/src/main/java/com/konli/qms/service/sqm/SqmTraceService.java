package com.konli.qms.service.sqm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmKeyPartSn;
import com.konli.qms.domain.sqm.entity.SqmTraceNode;
import com.konli.qms.domain.sqm.entity.SqmTraceProductDetail;
import com.konli.qms.domain.sqm.entity.SqmTraceRawDetail;
import com.konli.qms.domain.sqm.vo.TraceDirectionNode;
import com.konli.qms.domain.sqm.vo.TraceFullTreeVO;
import com.konli.qms.domain.sqm.dto.TraceNodeSaveRequest;
import com.konli.qms.domain.sqm.vo.TraceNodeFullVO;
import com.konli.qms.domain.sqm.vo.TraceNodeSearchVO;

import java.util.List;

/** 来料批次 + 追溯节点:创建/查询/追溯树。sqm.trace.* */
public interface SqmTraceService {

    List<SqmIncomingLot> listLots();

    /** 按批次号查询来料批次(批次号全局唯一)。 */
    SqmIncomingLot getLotByLotNo(String lotNo);

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
}
