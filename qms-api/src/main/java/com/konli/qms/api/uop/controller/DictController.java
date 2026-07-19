package com.konli.qms.api.uop.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.uop.entity.SysDict;
import com.konli.qms.service.uop.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通用字典(任何已认证用户可读,供下拉框/枚举展示)。
 * 前端 value=dict_key,label=dict_value。
 */
@RestController
@RequestMapping("/api/v1/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    /** 全量(前端启动预加载) */
    @GetMapping
    public R<List<SysDict>> listAll() {
        return R.ok(dictService.listAll());
    }

    /** 按类型 */
    @GetMapping("/{type}")
    public R<List<SysDict>> listByType(@PathVariable String type) {
        return R.ok(dictService.listByType(type));
    }
}
