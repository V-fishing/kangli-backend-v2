package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.NcmCorrectiveAction;
import com.konli.qms.service.ncm.NcmCorrectiveActionService;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 纠正措施:创建/查询/进度更新/关闭。权限码 ncm.ca.* */
@RestController
@RequestMapping("/api/v1/ncm/corrective-actions")
@RequiredArgsConstructor
public class NcmCorrectiveActionController {

    private final NcmCorrectiveActionService ncmCorrectiveActionService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.ca.list')")
    public R<List<NcmCorrectiveAction>> list(@RequestParam(required = false) String defectNo) {
        if (StringUtils.hasText(defectNo)) {
            return R.ok(ncmCorrectiveActionService.listByDefectNo(defectNo));
        }
        return R.ok(ncmCorrectiveActionService.list());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('ncm.ca.list')")
    public R<PageResult<NcmCorrectiveAction>> page(@RequestParam(required = false) String defectNo,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return R.ok(ncmCorrectiveActionService.listPage(defectNo, status, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.ca.list')")
    public R<NcmCorrectiveAction> get(@PathVariable String id) {
        return R.ok(ncmCorrectiveActionService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.ca.create')")
    public R<NcmCorrectiveAction> create(@RequestBody NcmCorrectiveAction action) {
        return R.ok(ncmCorrectiveActionService.create(action));
    }

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('ncm.ca.create')")
    public R<Void> updateProgress(@PathVariable String id, @RequestParam Short progress) {
        ncmCorrectiveActionService.updateProgress(id, progress);
        return R.ok();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('ncm.ca.close')")
    public R<Void> close(@PathVariable String id) {
        ncmCorrectiveActionService.close(id);
        return R.ok();
    }

    /** 列表级改派责任人(更新责任人 + 推送被指派人任务中心)。 */
    @PostMapping("/{id}/reassign")
    @PreAuthorize("hasAuthority('ncm.ca.create')")
    public R<Void> reassign(@PathVariable String id, @RequestBody DefectLaunchRequest req) {
        ncmCorrectiveActionService.reassign(id, req);
        return R.ok();
    }
}
