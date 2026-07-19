package com.konli.qms.service.uop.impl;

import com.konli.qms.domain.uop.entity.SysOrg;
import com.konli.qms.domain.uop.mapper.SysOrgMapper;
import com.konli.qms.service.uop.OrgService;
import com.konli.qms.service.uop.dto.OrgTreeNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService {

    private final SysOrgMapper sysOrgMapper;

    @Override
    public List<SysOrg> list() {
        return sysOrgMapper.selectList(null);
    }

    @Override
    public List<OrgTreeNode> tree() {
        List<SysOrg> all = sysOrgMapper.selectList(null);
        Map<String, OrgTreeNode> nodeMap = new HashMap<>();
        for (SysOrg org : all) {
            OrgTreeNode node = new OrgTreeNode();
            node.setOrg(org);
            nodeMap.put(org.getId(), node);
        }
        List<OrgTreeNode> roots = new ArrayList<>();
        for (SysOrg org : all) {
            OrgTreeNode node = nodeMap.get(org.getId());
            String pid = org.getParentId();
            if (pid == null || !nodeMap.containsKey(pid)) {
                roots.add(node);
            } else {
                nodeMap.get(pid).getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public void save(SysOrg org) {
        if (org.getId() == null) {
            sysOrgMapper.insert(org);
        } else {
            sysOrgMapper.updateById(org);
        }
    }

    @Override
    public void delete(String id) {
        sysOrgMapper.deleteById(id);
    }
}
