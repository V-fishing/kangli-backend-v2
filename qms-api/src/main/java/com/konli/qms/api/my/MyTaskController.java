package com.konli.qms.api.my;

import com.konli.qms.common.api.R;
import com.konli.qms.common.dto.MyTaskDTO;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.my.MyTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 跨模块"我的任务"聚合接口:当前登录用户作为负责人/被指派且未闭环的任务。
 * 供 FIA 看板"我的任务"卡片与工作台任务中心复用。
 */
@RestController
@RequestMapping("/api/v1/my")
@RequiredArgsConstructor
public class MyTaskController {

    private final MyTaskService myTaskService;

    @GetMapping("/tasks")
    public R<List<MyTaskDTO>> list(
            @RequestParam(required = false, defaultValue = "0") Integer limit,
            @RequestParam(required = false, defaultValue = "false") Boolean includeClosed) {
        // 兜底:未登录(理论上被 JWT 过滤器拦截)返回空,避免 NPE
        if (CompanyContext.get() == null) {
            return R.ok(List.of());
        }
        return R.ok(myTaskService.myTasks(limit, includeClosed));
    }
}
