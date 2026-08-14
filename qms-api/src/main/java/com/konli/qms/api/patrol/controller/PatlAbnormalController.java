package com.konli.qms.api.patrol.controller;

import com.konli.qms.api.patrol.dto.CloseAbnormalRequest;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.patrol.entity.PatlAbnormal;
import com.konli.qms.service.patrol.PatlTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 巡检异常:查询/关闭。patl.task.list / patl.task.create */
@RestController
@RequestMapping("/api/v1/patrol/abnormals")
@RequiredArgsConstructor
public class PatlAbnormalController {

    private final PatlTaskService patlTaskService;

    @GetMapping
    @PreAuthorize("hasAuthority('patl.task.list')")
    public R<List<PatlAbnormal>> list() {
        return R.ok(patlTaskService.listAbnormals());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('patl.task.list')")
    public R<PageResult<PatlAbnormal>> page(@RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return R.ok(patlTaskService.listAbnormalsPage(keyword, page, size));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('patl.task.create')")
    public R<Void> close(@PathVariable String id, @RequestBody CloseAbnormalRequest req) {
        patlTaskService.closeAbnormal(id, req.getHandleRemark());
        return R.ok();
    }
}
