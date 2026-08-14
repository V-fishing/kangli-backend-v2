package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.ncm.entity.Qms8dApprovalConfig;
import com.konli.qms.domain.ncm.mapper.Qms8dApprovalConfigMapper;
import com.konli.qms.service.ncm.Ncm8dApprovalConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Ncm8dApprovalConfigServiceImpl implements Ncm8dApprovalConfigService {

    private static final String[] STAGES = {"D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8"};
    /** 未配置时的默认:D1(团队组建,需质量部审核)+ D3/D5/D7 需审批。 */
    private static final List<String> DEFAULT_APPROVAL = Arrays.asList("D1", "D3", "D5", "D7");

    private final Qms8dApprovalConfigMapper mapper;

    /** 解析归属组织:无组织/ROOT 统一归并为 'ROOT' 键,保证读写一致。 */
    private String resolveKey(String orgId) {
        return (orgId == null || orgId.isBlank() || "ROOT".equals(orgId)) ? "ROOT" : orgId;
    }

    private String currentOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String o = u != null ? u.orgId() : null;
        return resolveKey(o);
    }

    @Override
    public List<Qms8dApprovalConfig> getConfig() {
        String org = currentOrgId();
        List<Qms8dApprovalConfig> rows = mapper.selectList(
                new LambdaQueryWrapper<Qms8dApprovalConfig>()
                        .eq(Qms8dApprovalConfig::getOrgId, org)
                        .orderByAsc(Qms8dApprovalConfig::getSortOrder));
        Map<String, Qms8dApprovalConfig> map = rows.stream()
                .collect(Collectors.toMap(Qms8dApprovalConfig::getStageCode, r -> r, (a, b) -> a));
        List<Qms8dApprovalConfig> result = new ArrayList<>();
        for (int i = 0; i < STAGES.length; i++) {
            Qms8dApprovalConfig c = map.get(STAGES[i]);
            if (c == null) {
                c = new Qms8dApprovalConfig();
                c.setStageCode(STAGES[i]);
                c.setNeedApproval(DEFAULT_APPROVAL.contains(STAGES[i]));
                c.setSigner(null);
                c.setSortOrder(i + 1);
            }
            result.add(c);
        }
        return result;
    }

    @Override
    @Transactional
    public void saveConfig(List<Qms8dApprovalConfig> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(400, "配置不能为空");
        }
        String org = currentOrgId();
        mapper.delete(new LambdaQueryWrapper<Qms8dApprovalConfig>().eq(Qms8dApprovalConfig::getOrgId, org));
        for (Qms8dApprovalConfig it : items) {
            if (it.getStageCode() == null || !Arrays.asList(STAGES).contains(it.getStageCode())) {
                throw new BusinessException(400, "非法阶段编码: " + it.getStageCode());
            }
            int ord = Arrays.asList(STAGES).indexOf(it.getStageCode()) + 1;
            it.setId(null);
            it.setOrgId(org);
            it.setSortOrder(ord);
            if (it.getNeedApproval() == null) it.setNeedApproval(false);
            if (it.getSigner() != null) it.setSigner(it.getSigner().trim());
            mapper.insert(it);
        }
    }

    @Override
    public boolean needApproval(String orgId, String stageCode) {
        Qms8dApprovalConfig c = find(resolveKey(orgId), stageCode);
        if (c != null) return Boolean.TRUE.equals(c.getNeedApproval());
        return DEFAULT_APPROVAL.contains(stageCode);
    }

    @Override
    public String signerOf(String orgId, String stageCode) {
        Qms8dApprovalConfig c = find(resolveKey(orgId), stageCode);
        return c == null ? null : c.getSigner();
    }

    private Qms8dApprovalConfig find(String orgKey, String stageCode) {
        return mapper.selectOne(new LambdaQueryWrapper<Qms8dApprovalConfig>()
                .eq(Qms8dApprovalConfig::getOrgId, orgKey)
                .eq(Qms8dApprovalConfig::getStageCode, stageCode));
    }
}
