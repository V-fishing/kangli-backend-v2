package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.dto.TraceNodeSaveRequest;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmKeyPartSn;
import com.konli.qms.domain.sqm.entity.SqmTraceNode;
import com.konli.qms.domain.sqm.entity.SqmTraceProductDetail;
import com.konli.qms.domain.sqm.entity.SqmTraceRawDetail;
import com.konli.qms.domain.sqm.vo.TraceDirection;
import com.konli.qms.domain.sqm.vo.TraceDirectionNode;
import com.konli.qms.domain.sqm.vo.TraceFullTreeVO;
import com.konli.qms.domain.sqm.vo.TraceNodeFullVO;
import com.konli.qms.domain.sqm.vo.TraceNodeSearchVO;
import com.konli.qms.service.sqm.SqmTraceService;
import com.konli.qms.service.support.OrgIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 追溯节点:创建/查询/追溯树。sqm.trace.list / sqm.trace.create */
@RestController
@RequestMapping("/api/v1/sqm")
@RequiredArgsConstructor
public class SqmTraceController {

    private final SqmTraceService sqmTraceService;
    private final OrgIdResolver orgIdResolver;

    // ---- 追溯树 ----

    @GetMapping("/trace/tree")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<SqmTraceNode>> traceTree(@RequestParam String rootLotId) {
        return R.ok(sqmTraceService.traceTree(rootLotId));
    }

    @PostMapping("/trace/nodes")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<SqmTraceNode> createNode(@RequestBody SqmTraceNode node) {
        return R.ok(sqmTraceService.createNode(node));
    }

    // ---- 来料批次(物料表): 列表 / 供应商来料入库 ----

    /**
     * 来料批次列表: 支持 supplierId + iqcPass 过滤, 供前端"供应商 → 已通过 IQC 合格物料"级联下拉。
     * 例: GET /lots?supplierId=xxx&iqcPass=true 返回该供应商名下全部已通过来料检验的批次。
     */
    @GetMapping("/lots")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<SqmIncomingLot>> listLots(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String supplierId,
                                            @RequestParam(required = false) Boolean iqcPass) {
        return R.ok(sqmTraceService.listLots(keyword, supplierId, iqcPass));
    }

    /**
     * 来料批次分页列表: 大数据量下以分页返回, 避免一次性拉取全部导致前端卡顿。
     * 供追溯列表页(总表/物料表)分页加载使用; keyword 模糊匹配批次号/物料编码/物料名称。
     */
    @GetMapping("/lots/page")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<PageResult<SqmIncomingLot>> listLotsPage(@RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String supplierId,
                                                     @RequestParam(required = false) String orgId,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return R.ok(sqmTraceService.listLotsPage(keyword, supplierId, orgId, page, size));
    }

    /**
     * 供应商来料入库: 建批次(sqm_incoming_lot) + 自动生成 incoming 追溯节点。
     * 这是「FIA 建批次 → 入库 → 追溯挂接 → 防超卖」闭环的入库入口。
     */
    @PostMapping("/lots")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<SqmIncomingLot> createLot(@RequestBody SqmIncomingLot lot) {
        return R.ok(sqmTraceService.createLot(lot));
    }

    /**
     * 全局追溯节点检索(分页):不限定具体来料批次,可按对象类型(半成品/物料/客户 等,
     * 支持逗号分隔的多个类型)与关键字(节点名/批次号/供应商)过滤。用于直接检索某类
     * 对象而无需先选来料批次,大数据量下以分页返回避免一次性拉取全部。
     */
    @GetMapping("/trace/nodes/search")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<PageResult<TraceNodeSearchVO>> searchNodes(
            @RequestParam(required = false) String nodeType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orgId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(sqmTraceService.searchNodes(nodeType, keyword, orgId, page, size));
    }

    @GetMapping("/trace/tree-recursive")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<SqmTraceNode>> traceTreeRecursive(@RequestParam String rootLotId) {
        return R.ok(sqmTraceService.traceTreeRecursive(rootLotId));
    }

    /** 完整追溯树(按来料批次):嵌套 children + 每节点明细 + 供应商名。 */
    @GetMapping("/trace/full-tree")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<TraceFullTreeVO> getFullTraceTree(@RequestParam String rootLotId) {
        return R.ok(sqmTraceService.getFullTraceTree(rootLotId));
    }

    // ---- 新录入模型(无来料批次依赖) ----

    /** 建一个产出/头节点(semi/ship/customer) + 明细, 可选一次性带组成。 */
    @PostMapping("/trace/nodes/save")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<SqmTraceNode> saveNode(@RequestBody TraceNodeSaveRequest req) {
        return R.ok(sqmTraceService.saveNode(req));
    }

    /** 给某节点挂一个组成(raw/semi), 返回新建子节点。 */
    @PostMapping("/trace/nodes/{parentId}/components")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<SqmTraceNode> attachComponent(@PathVariable String parentId,
                                            @RequestBody TraceNodeSaveRequest.ComponentItem item) {
        return R.ok(sqmTraceService.attachComponent(parentId, item));
    }

    /** 按树根节点 id 取完整嵌套树(无来料批次也能用)。 */
    @GetMapping("/trace/full-tree-by-root")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<TraceFullTreeVO> getFullTraceTreeByRootNode(@RequestParam String rootNodeId) {
        TraceFullTreeVO vo = sqmTraceService.getFullTraceTreeByRootNode(rootNodeId);
        vo.setRootNodeId(rootNodeId);
        return R.ok(vo);
    }

    /** 以任意节点(来料/半成品/成品…)为根、仅向下展开其下游子树。 */
    @GetMapping("/trace/tree-from-node")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<TraceFullTreeVO> getTraceTreeFromNode(@RequestParam String nodeId) {
        return R.ok(sqmTraceService.getTraceTreeFromNode(nodeId));
    }

    /** 按树根节点 id 取扁平树。 */
    @GetMapping("/trace/tree-by-root")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<SqmTraceNode>> traceTreeByRootNode(@RequestParam String rootNodeId) {
        return R.ok(sqmTraceService.traceTreeByRootNode(rootNodeId));
    }

    /** 列出当前组织的全部追溯树根节点, 供工作台选择打开。 */
    @GetMapping("/trace/roots")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<SqmTraceNode>> listRoots(@RequestParam String orgId) {
        return R.ok(sqmTraceService.listRoots(orgId));
    }

    // ---- 追溯明细:原料 ----

    @GetMapping("/trace/nodes/{nodeId}/raw-detail")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<SqmTraceRawDetail> getRawDetail(@PathVariable String nodeId) {
        return R.ok(sqmTraceService.getRawDetail(nodeId));
    }

    @PutMapping("/trace/nodes/{nodeId}/raw-detail")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<Void> saveRawDetail(@PathVariable String nodeId,
                                 @RequestBody SqmTraceRawDetail detail) {
        detail.setNodeId(nodeId);
        sqmTraceService.saveRawDetail(detail);
        return R.ok();
    }

    // ---- 追溯明细:产品 ----

    @GetMapping("/trace/nodes/{nodeId}/product-detail")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<SqmTraceProductDetail> getProductDetail(@PathVariable String nodeId) {
        return R.ok(sqmTraceService.getProductDetail(nodeId));
    }

    @PutMapping("/trace/nodes/{nodeId}/product-detail")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<Void> saveProductDetail(@PathVariable String nodeId,
                                     @RequestBody SqmTraceProductDetail detail) {
        detail.setNodeId(nodeId);
        sqmTraceService.saveProductDetail(detail);
        return R.ok();
    }

    // ---- 节点完整详情(表 + 树共用) ----

    @GetMapping("/trace/nodes/{nodeId}/detail")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<TraceNodeFullVO> getNodeDetail(@PathVariable String nodeId) {
        return R.ok(sqmTraceService.getNodeDetail(nodeId));
    }

    // ---- 按方向追溯(正向/反向/全部) ----

    @GetMapping("/trace/nodes/{nodeId}/direction")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<TraceDirectionNode>> traceDirection(@PathVariable String nodeId,
                                                      @RequestParam(defaultValue = "both") String direction) {
        return R.ok(sqmTraceService.traceDirection(nodeId, direction));
    }

    // ---- 关键件 SN ----

    @GetMapping("/key-part-sns")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<SqmKeyPartSn>> listKeyPartSn(@RequestParam String lotId) {
        return R.ok(sqmTraceService.listKeyPartSn(lotId));
    }

    // ---- 方案 B: 基于 sqm_trace_relation + 三源表的 MES 追溯树 ----

    /** 以源表业务条码为根查询 MES 导入数据的完整追溯树(节点详情取自三源表)。direction: forward/backward/all(默认 all)。 */
    @GetMapping("/trace/mes-tree")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<TraceFullTreeVO> getMesTraceTree(@RequestParam String barcode,
                                              @RequestParam(required = false, defaultValue = "ALL") String direction,
                                              @RequestParam(required = false) String orgId) {
        return R.ok(sqmTraceService.traceMesTree(barcode, orgId, TraceDirection.of(direction)));
    }

    /** 按批号查产品树: 批号可为来料批次号(material_batch_no)或成品/半成品批号(prod_batch_or_sn)。返回森林(可能多棵)。 */
    @GetMapping("/trace/mes-tree-by-batch")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<TraceFullTreeVO>> getMesTraceTreeByBatch(@RequestParam String batchNo,
                                                           @RequestParam(required = false, defaultValue = "ALL") String direction,
                                                           @RequestParam(required = false) String orgId) {
        return R.ok(sqmTraceService.traceByBatchNo(batchNo, orgId, TraceDirection.of(direction)));
    }

    /** 按来料批次号(lotNo)查 MES 追溯森林: 先由来料批次号定位源表业务条码集合, 再逐条码追溯合并森林。 */
    @GetMapping("/trace/mes-tree-by-lotno")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<TraceFullTreeVO>> getMesTraceTreeByLotNo(@RequestParam String lotNo,
                                                            @RequestParam(required = false, defaultValue = "ALL") String direction,
                                                            @RequestParam(required = false) String orgId) {
        return R.ok(sqmTraceService.traceByLotNo(lotNo, orgId, TraceDirection.of(direction)));
    }

    /** 方案 B: 往 sqm_trace_relation 手动插入一条 parent→child 条码边(物料表关联半成品/成品)。 */
    @PostMapping("/trace/mes-relation")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<Void> saveMesRelation(@RequestParam String parentBarcode,
                                   @RequestParam String childBarcode,
                                   @RequestParam String relationType,
                                   @RequestParam(required = false) String orgId) {
        sqmTraceService.saveRelation(parentBarcode, childBarcode, relationType, orgId);
        return R.ok();
    }

    @PostMapping("/key-part-sns")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<Void> createKeyPartSn(@RequestBody SqmKeyPartSn sn) {
        sqmTraceService.createKeyPartSn(sn);
        return R.ok();
    }

    // ---- 方案 B: 源表分页(三源表路由) ----

    /**
     * 源表分页: 按 type(material/semi/finished/all) 路由三源表, 返回分页业务记录。
     * type=all 时仅返回各类型计数 Map(供前端总表分 tab 显示); plantCode 用于组织隔离。
     */
    @GetMapping("/trace/source/page")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<PageResult<Map<String, Object>>> sourcePage(@RequestParam(defaultValue = "all") String type,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String plantCode,
                                                         @RequestParam(required = false) String bizType,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return R.ok(sqmTraceService.sourcePage(type, keyword, plantCode, bizType, page, size));
    }

    // ---- 源表全字段明细(供物料表/半成品表/成品表「详情」弹窗) ----

    /**
     * 源表全字段明细: 按 sourceType(material/finished/semi/critical) + 业务条码(key)反查三源表,
     * 返回剔除别名列与审计列后的业务字段 Map,供前端分组卡片弹窗展示全字段。
     */
    @GetMapping("/trace/source-detail")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<Map<String, Object>> getSourceDetail(@RequestParam String sourceType,
                                                  @RequestParam String key) {
        return R.ok(sqmTraceService.getSourceDetail(sourceType, key));
    }

    // ---- 生产工单号下拉(供工装派工 / 不良登记等工单号输入场景) ----

    /**
     * 生产工单号下拉数据源: 从 MES 落地宽表(finished_goods_inspection.production_order_no
     * 与 critical_material_binding.work_order_no)并集去重取真实存在过的工单号。
     * 工装派工/不良登记等场景的"工单号"字段由此下拉,避免手填游离工单号。
     */
    @GetMapping("/trace/production-orders")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<String>> listProductionOrders(@RequestParam(required = false) String orgId,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false, defaultValue = "200") Integer limit) {
        String resolved = orgIdResolver.resolveForQuery(orgId);
        return R.ok(sqmTraceService.listProductionOrders(resolved, keyword, limit));
    }
}
