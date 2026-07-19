package com.konli.qms.service.uop.dto;

import com.konli.qms.domain.uop.entity.SysOrg;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 组织树节点(含子节点) */
@Data
public class OrgTreeNode {
    private SysOrg org;
    private List<OrgTreeNode> children = new ArrayList<>();
}
