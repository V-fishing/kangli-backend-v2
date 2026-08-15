package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.service.spc.SpcParamService;
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

/** SPC 参数主数据 CRUD(规格限/子组大小/图表类型)。 */
@RestController
@RequestMapping("/api/v1/spc/params")
@RequiredArgsConstructor
public class SpcParamController {

    private final SpcParamService spcParamService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<List<SpcParam>> list(@RequestParam(required = false) String productName,
                                  @RequestParam(required = false) String procName,
                                  @RequestParam(required = false) String paramSource) {
        return R.ok(spcParamService.list(productName, procName, paramSource));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<PageResult<SpcParam>> page(@RequestParam(required = false) String productName,
                                        @RequestParam(required = false) String procName,
                                        @RequestParam(required = false) String paramSource,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return R.ok(spcParamService.listPage(productName, procName, paramSource, keyword, page, size));
    }

    @PostMapping("/from-fia-task")
    @PreAuthorize("hasAuthority('spc.param.create')")
    public R<List<SpcParam>> fromFiaTask(@RequestParam String taskId) {
        return R.ok(spcParamService.ensureFromFiaTask(taskId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<SpcParam> get(@PathVariable String id) {
        return R.ok(spcParamService.get(id));
    }

    @GetMapping("/by-std")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<List<SpcParam>> listByStd(@RequestParam String stdId) {
        return R.ok(spcParamService.listByStd(stdId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('spc.param.create')")
    public R<SpcParam> create(@RequestBody SpcParam param) {
        return R.ok(spcParamService.create(param));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.param.create')")
    public R<Void> update(@PathVariable String id, @RequestBody SpcParam param) {
        param.setId(id);
        spcParamService.update(param);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.param.delete')")
    public R<Void> delete(@PathVariable String id) {
        spcParamService.delete(id);
        return R.ok();
    }
}
