package com.konli.qms.api.fia.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.service.fia.FiaInspectionOverviewService;
import com.konli.qms.service.fia.dto.MergedInspectionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 完工检验 + 物料检验 合并视图(列表「全部」档):UNION ALL 直读 MES 两张表。
 * 挂完工检验菜单权限 fia.finish.list。
 */
@RestController
@RequestMapping("/api/v1/fia/inspections")
@RequiredArgsConstructor
public class FiaInspectionOverviewController {

    private final FiaInspectionOverviewService overviewService;

    /** 合并分页(物料编码 / 关键字 跨两表过滤)。 */
    @GetMapping("/merged-page")
    @PreAuthorize("hasAuthority('fia.finish.list')")
    public R<PageResult<MergedInspectionVO>> mergedPage(@RequestParam(required = false) String materialCode,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return R.ok(overviewService.page(page, size, materialCode, keyword));
    }
}
