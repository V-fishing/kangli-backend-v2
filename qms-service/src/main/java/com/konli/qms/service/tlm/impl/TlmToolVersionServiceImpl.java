package com.konli.qms.service.tlm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.tlm.entity.TlmToolVersion;
import com.konli.qms.domain.tlm.mapper.TlmToolVersionMapper;
import com.konli.qms.service.tlm.TlmToolVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TlmToolVersionServiceImpl implements TlmToolVersionService {

    private final TlmToolVersionMapper versionMapper;

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<TlmToolVersion> listByTool(String toolId) {
        LambdaQueryWrapper<TlmToolVersion> w = new LambdaQueryWrapper<>();
        w.eq(TlmToolVersion::getToolId, toolId);
        w.orderByDesc(TlmToolVersion::getChangedAt);
        w.orderByDesc(TlmToolVersion::getCreatedAt);
        return versionMapper.selectList(w);
    }

    @Override
    @Transactional
    public TlmToolVersion create(TlmToolVersion version) {
        // 组织: 优先当前登录组织, 回退到工装自身 org
        if (version.getOrgId() == null || version.getOrgId().isBlank()) {
            String org = curOrg();
            if (org == null && version.getToolId() != null) {
                TlmToolVersion probe = versionMapper.selectById(version.getToolId());
                if (probe != null) org = probe.getOrgId();
            }
            version.setOrgId(org);
        }
        // 版本号自动递增: 取该工装当前最大数字版本 +1, 格式 V{n}
        int max = 0;
        LambdaQueryWrapper<TlmToolVersion> w = new LambdaQueryWrapper<>();
        w.eq(TlmToolVersion::getToolId, version.getToolId());
        w.select(TlmToolVersion::getVersionNo);
        List<TlmToolVersion> existing = versionMapper.selectList(w);
        for (TlmToolVersion e : existing) {
            if (e.getVersionNo() != null && e.getVersionNo().startsWith("V")) {
                try {
                    int n = Integer.parseInt(e.getVersionNo().substring(1));
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        version.setVersionNo("V" + (max + 1));
        // 变更人: 当前登录用户名
        try {
            String uname = CompanyContext.get().username();
            if (uname != null && !uname.isBlank()) version.setChangedBy(uname);
        } catch (Exception ignored) {
        }
        versionMapper.insert(version);
        return version;
    }
}
