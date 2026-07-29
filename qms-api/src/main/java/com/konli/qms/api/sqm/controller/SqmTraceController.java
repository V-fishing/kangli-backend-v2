package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.dto.TraceNodeSaveRequest;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmKeyPartSn;
import com.konli.qms.domain.sqm.entity.SqmTraceNode;
import com.konli.qms.domain.sqm.entity.SqmTraceProductDetail;
import com.konli.qms.domain.sqm.entity.SqmTraceRawDetail;
import com.konli.qms.domain.sqm.vo.TraceDirectionNode;
import com.konli.qms.domain.sqm.vo.TraceFullTreeVO;
import com.konli.qms.domain.sqm.vo.TraceNodeFullVO;
import com.konli.qms.domain.sqm.vo.TraceNodeSearchVO;
import com.konli.qms.service.sqm.SqmTraceService;
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

/** 追溯节点:创建/查询/追溯树。sqm.trace.list / sqm.trace.create */
@RestController
@RequestMapping("/api/v1/sqm")
@RequiredArgsConstructor
public class SqmTraceController {

    private final SqmTraceService sqmTraceService;

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

    @GetMapping("/lots")
    @PreAuthorize("hasAuthority('sqm.trace.list')")
    public R<List<SqmIncomingLot>> listLots(@RequestParam(required = false) String keyword) {
        return R.ok(sqmTraceService.listLots(keyword));
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

    @PostMapping("/key-part-sns")
    @PreAuthorize("hasAuthority('sqm.trace.create')")
    public R<Void> createKeyPartSn(@RequestBody SqmKeyPartSn sn) {
        sqmTraceService.createKeyPartSn(sn);
        return R.ok();
    }
}
