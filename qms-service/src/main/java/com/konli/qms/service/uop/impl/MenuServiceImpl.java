package com.konli.qms.service.uop.impl;

import com.konli.qms.domain.uop.entity.SysMenu;
import com.konli.qms.domain.uop.mapper.SysMenuMapper;
import com.konli.qms.service.uop.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<SysMenu> list() {
        return sysMenuMapper.selectList(null);
    }

    @Override
    public void save(SysMenu menu) {
        if (menu.getId() == null) {
            sysMenuMapper.insert(menu);
        } else {
            sysMenuMapper.updateById(menu);
        }
    }

    @Override
    public void delete(String id) {
        sysMenuMapper.deleteById(id);
    }
}
