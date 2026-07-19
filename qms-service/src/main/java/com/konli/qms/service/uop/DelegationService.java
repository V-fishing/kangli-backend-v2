package com.konli.qms.service.uop;

import com.konli.qms.domain.uop.entity.SysDelegation;

import java.util.List;

public interface DelegationService {
    List<SysDelegation> list();
    void create(SysDelegation delegation);
    void revoke(String id);
}
