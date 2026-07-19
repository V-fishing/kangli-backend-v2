package com.konli.qms.service.uop;

import com.konli.qms.domain.uop.entity.SysOrg;
import com.konli.qms.service.uop.dto.OrgTreeNode;

import java.util.List;

public interface OrgService {
    List<SysOrg> list();
    List<OrgTreeNode> tree();
    void save(SysOrg org);
    void delete(String id);
}
