package com.konli.qms.service.uop;

import com.konli.qms.domain.uop.entity.SysDict;

import java.util.List;

public interface DictService {

    /** 按类型查启用项(排序) */
    List<SysDict> listByType(String type);

    /** 全量(前端启动预加载) */
    List<SysDict> listAll();
}
