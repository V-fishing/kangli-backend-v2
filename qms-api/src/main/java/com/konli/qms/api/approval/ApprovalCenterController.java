package com.konli.qms.api.approval;

import com.konli.qms.common.dto.PendingApprovalDTO;
import com.konli.qms.common.api.R;
import com.konli.qms.service.approval.ApprovalCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用审批中心接口。
 */
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalCenterController {

    private final ApprovalCenterService approvalCenterService;

    /**
     * 当前登录用户的待我审批列表(跨 FIA / NCM / SQM 聚合)。
     *
     * @param limit 可选上限:仅返回前 limit 条(服务端截断),不传则返回全部。
     */
    @PreAuthorize("hasAuthority('approval.center.pending')")
    @GetMapping("/pending")
    public R<List<PendingApprovalDTO>> pending(@RequestParam(required = false) Integer limit) {
        List<PendingApprovalDTO> list = approvalCenterService.myPending();
        if (limit != null && limit > 0 && list.size() > limit) {
            list = new ArrayList<>(list.subList(0, limit));
        }
        return R.ok(list);
    }
}
