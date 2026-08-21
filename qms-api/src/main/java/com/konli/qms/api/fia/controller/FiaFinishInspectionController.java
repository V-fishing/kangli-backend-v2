package com.konli.qms.api.fia.controller;

import com.konli.qms.service.fia.dto.FinishInspectionCreateRequest;
import com.konli.qms.service.fia.dto.FinishInspectionUpdateRequest;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.fia.FiaFinishInspectionService;
import com.konli.qms.service.fia.FinishInspectionQuery;
import com.konli.qms.service.fia.dto.EligibleFirstArticleVO;
import com.konli.qms.service.fia.dto.FinishInspectionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 完工检验独立模块:直读直写 MES qms.finished_goods_inspection(不复用首件主表/不建明细/不接审批流)。
 * 首件绑定仅按 production_order_no 查询 fia_task 展示(软提示)。
 */
@RestController
@RequestMapping("/api/v1/fia/finish-inspections")
@RequiredArgsConstructor
public class FiaFinishInspectionController {

    private final FiaFinishInspectionService finishInspectionService;

    /** 生产订单号下拉(MES 已存在的 production_order_no DISTINCT,支持关键字过滤)。 */
    @GetMapping("/mes-production-orders")
    @PreAuthorize("hasAuthority('fia.finish.list')")
    public R<List<String>> mesProductionOrders(@RequestParam(required = false) String keyword) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        return R.ok(finishInspectionService.listMesProductionOrders(orgId, keyword));
    }

    /** 分页列表(过滤 category / 生产订单号 / 物料编码 / 关键字)。 */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('fia.finish.list')")
    public R<PageResult<FinishInspectionVO>> page(@RequestParam(required = false) String category,
                                                 @RequestParam(required = false) String productionOrderNo,
                                                 @RequestParam(required = false) String materialCode,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        FinishInspectionQuery query = new FinishInspectionQuery();
        query.setOrgId(orgId);
        query.setCategory(category);
        query.setProductionOrderNo(productionOrderNo);
        query.setMaterialCode(materialCode);
        query.setKeyword(keyword);
        query.setPage(page);
        query.setSize(size);
        return R.ok(finishInspectionService.page(query));
    }

    /** 详情(按行 ctid)。 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.finish.list')")
    public R<FinishInspectionVO> get(@PathVariable String id) {
        return R.ok(finishInspectionService.get(id));
    }

    /** 建单(INSERT 新行;工厂/组织按当前用户组织自动带入)。 */
    @PostMapping
    @PreAuthorize("hasAuthority('fia.finish.create')")
    public R<String> create(@Valid @RequestBody FinishInspectionCreateRequest req) {
        return R.ok(finishInspectionService.create(req));
    }

    /** 保存检验汇总(检验员 + 判定结论)。 */
    @PutMapping("/{id}/inspection")
    @PreAuthorize("hasAuthority('fia.finish.edit')")
    public R<Void> updateInspection(@PathVariable String id, @RequestBody FinishInspectionUpdateRequest req) {
        finishInspectionService.updateInspection(id, req);
        return R.ok();
    }

    /** 保存数量信息与审核签核(表单直填)。 */
    @PutMapping("/{id}/signoff")
    @PreAuthorize("hasAuthority('fia.finish.edit')")
    public R<Void> updateSignoff(@PathVariable String id, @RequestBody FinishInspectionUpdateRequest req) {
        finishInspectionService.updateSignoff(id, req);
        return R.ok();
    }

    /** 软删除。 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.finish.delete')")
    public R<Void> delete(@PathVariable String id) {
        finishInspectionService.softDelete(id);
        return R.ok();
    }

    /** 首件软绑定查询:按 production_order_no 查 fia_task 中同单且非完工检验的首件。 */
    @GetMapping("/{id}/first-articles")
    @PreAuthorize("hasAuthority('fia.finish.list')")
    public R<List<EligibleFirstArticleVO>> firstArticles(@PathVariable String id) {
        return R.ok(finishInspectionService.listBoundFirstArticles(id));
    }
}
