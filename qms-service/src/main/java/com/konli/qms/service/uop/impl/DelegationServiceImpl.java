package com.konli.qms.service.uop.impl;

import com.konli.qms.domain.uop.entity.SysDelegation;
import com.konli.qms.domain.uop.mapper.SysDelegationMapper;
import com.konli.qms.service.uop.DelegationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DelegationServiceImpl implements DelegationService {

    private final SysDelegationMapper sysDelegationMapper;

    @Override
    public List<SysDelegation> list() {
        return sysDelegationMapper.selectList(null);
    }

    @Override
    public void create(SysDelegation delegation) {
        if (delegation.getStatus() == null) {
            delegation.setStatus("生效");
        }
        sysDelegationMapper.insert(delegation);
    }

    @Override
    public void revoke(String id) {
        SysDelegation d = new SysDelegation();
        d.setId(id);
        d.setStatus("已撤销");
        sysDelegationMapper.updateById(d);
    }
}
