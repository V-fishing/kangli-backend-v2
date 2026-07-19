package com.konli.qms.service.fia;

import com.konli.qms.domain.fia.entity.FiaTriggerType;

import java.util.List;

/** 触发事件类型管理 */
public interface FiaTriggerTypeService {

    List<FiaTriggerType> list();

    FiaTriggerType create(FiaTriggerType triggerType);

    void update(FiaTriggerType triggerType);

    void delete(String id);

    /** 启用/停用 */
    void toggle(String id, boolean enabled);
}
