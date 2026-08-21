package com.konli.qms.api.fia.controller;

import com.konli.qms.service.fia.dto.MaterialInspectionCreateRequest;
import com.konli.qms.service.fia.dto.MaterialInspectionUpdateRequest;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.fia.FiaMaterialInspectionService;
import com.konli.qms.service.fia.MaterialInspectionQuery;
import com.konli.qms.service.fia.dto.MaterialInspectionVO;
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
 * 物料检验(来料检验)独立模块:直读直写 MES qms.material_inspection(不复用首件主表/不建明细/不接审批流)。
 */
@RestController
@RequestMapping("/api/v1/fia/material-inspections")
@RequiredArgsConstructor
public class FiaMaterialInspectionController {

    private final FiaMaterialInspectionService materialInspectionService;

    /** 物料编码下拉(MES 已存在的 material_code DISTINCT,支持关键字过滤)。 */
    @GetMapping("/material-codes")
    @PreAuthorize("hasAuthority('fia.material.list')")
    public R<List<String>> materialCodes(@RequestParam(required = false) String keyword) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        return R.ok(materialInspectionService.listMesMaterialCodes(orgId, keyword));
    }

    /** 分页列表(过滤 供应商/物料编码/检验申请号/记录编号/判定结论/关键字)。 */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('fia.material.list')")
    public R<PageResult<MaterialInspectionVO>> page(@RequestParam(required = false) String supplierName,
                                                    @RequestParam(required = false) String materialCode,
                                                    @RequestParam(required = false) String inspectionRequestNo,
                                                    @RequestParam(required = false) String recordNo,
                                                    @RequestParam(required = false) String inspectionResult,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        MaterialInspectionQuery query = new MaterialInspectionQuery();
        query.setOrgId(orgId);
        query.setSupplierName(supplierName);
        query.setMaterialCode(materialCode);
        query.setInspectionRequestNo(inspectionRequestNo);
        query.setRecordNo(recordNo);
        query.setInspectionResult(inspectionResult);
        query.setKeyword(keyword);
        query.setPage(page);
        query.setSize(size);
        return R.ok(materialInspectionService.page(query));
    }

    /** 详情(按 record_no 行定位)。 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.material.list')")
    public R<MaterialInspectionVO> get(@PathVariable String id) {
        return R.ok(materialInspectionService.get(id));
    }

    /** 建单(INSERT 新行;工厂/组织按当前用户组织自动带入)。 */
    @PostMapping
    @PreAuthorize("hasAuthority('fia.material.create')")
    public R<String> create(@Valid @RequestBody MaterialInspectionCreateRequest req) {
        return R.ok(materialInspectionService.create(req));
    }

    /** 保存检验汇总(检验员 + 判定结论 + 缺陷/处理方式)。 */
    @PutMapping("/{id}/inspection")
    @PreAuthorize("hasAuthority('fia.material.edit')")
    public R<Void> updateInspection(@PathVariable String id, @RequestBody MaterialInspectionUpdateRequest req) {
        materialInspectionService.updateInspection(id, req);
        return R.ok();
    }

    /** 保存数量信息与审核签核(表单直填)。 */
    @PutMapping("/{id}/signoff")
    @PreAuthorize("hasAuthority('fia.material.edit')")
    public R<Void> updateSignoff(@PathVariable String id, @RequestBody MaterialInspectionUpdateRequest req) {
        materialInspectionService.updateSignoff(id, req);
        return R.ok();
    }

    /** 软删除。 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.material.delete')")
    public R<Void> delete(@PathVariable String id) {
        materialInspectionService.softDelete(id);
        return R.ok();
    }
}
