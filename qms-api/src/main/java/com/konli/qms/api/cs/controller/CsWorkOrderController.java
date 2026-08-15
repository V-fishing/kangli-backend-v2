package com.konli.qms.api.cs.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.cs.entity.CsWorkOrder;
import com.konli.qms.service.cs.CsWorkOrderService;
import com.konli.qms.service.uop.UserService;
import com.konli.qms.service.uop.dto.UserSelectVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;

/** 售后工单流程控制。cs.workorder.* */
@RestController
@RequestMapping("/api/v1/cs/work-orders")
@RequiredArgsConstructor
@Slf4j
public class CsWorkOrderController {

    private final CsWorkOrderService service;
    private final UserService userService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('cs.workorder.list')")
    public R<PageResult<CsWorkOrder>> page(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String woType,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) String priority,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return R.ok(service.page(keyword, woType, status, priority, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cs.workorder.list')")
    public R<CsWorkOrder> get(@PathVariable String id) {
        return R.ok(service.get(id));
    }

    /** 派单人员下拉(复用用户精简信息,前端 el-select)。 */
    @GetMapping("/assignable-users")
    @PreAuthorize("hasAuthority('cs.workorder.assign')")
    public R<List<UserSelectVo>> assignableUsers() {
        return R.ok(userService.listForSelect());
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('cs.workorder.list')")
    public R<Map<String, Object>> dashboard() {
        return R.ok(service.dashboard());
    }

    @GetMapping("/satisfaction-stats")
    @PreAuthorize("hasAuthority('cs.satisfaction.list')")
    public R<Map<String, Object>> satisfactionStats() {
        return R.ok(service.satisfactionStats());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cs.workorder.create')")
    public R<CsWorkOrder> create(@RequestBody CsWorkOrder order) {
        return R.ok(service.create(order));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('cs.workorder.edit')")
    public R<CsWorkOrder> update(@RequestBody CsWorkOrder order) {
        return R.ok(service.update(order));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cs.workorder.delete')")
    public R<Void> delete(@PathVariable String id) {
        service.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('cs.workorder.assign')")
    public R<Void> assign(@PathVariable String id,
                          @RequestParam String responsibleId,
                          @RequestParam(required = false) String responsibleName) {
        service.assign(id, responsibleId, responsibleName);
        return R.ok();
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('cs.workorder.assign')")
    public R<Void> complete(@PathVariable String id, @RequestParam(required = false) String handleDetail) {
        service.complete(id, handleDetail);
        return R.ok();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('cs.workorder.close')")
    public R<Void> close(@PathVariable String id,
                         @RequestParam(required = false) Integer satisfaction,
                         @RequestParam(required = false) String satisfactionComment) {
        service.close(id, satisfaction, satisfactionComment);
        return R.ok();
    }
}
