package com.konli.qms.service.uop;

import com.konli.qms.domain.uop.entity.SysMenu;

import java.util.List;

public interface MenuService {
    List<SysMenu> list();
    void save(SysMenu menu);
    void delete(String id);
}
