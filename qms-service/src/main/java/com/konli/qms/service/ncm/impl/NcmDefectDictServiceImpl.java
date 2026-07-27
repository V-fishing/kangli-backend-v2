package com.konli.qms.service.ncm.impl;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.ncm.entity.NcmDefectDict;
import com.konli.qms.domain.ncm.mapper.NcmDefectDictMapper;
import com.konli.qms.domain.ncm.mapper.NcmDefectRecordMapper;
import com.konli.qms.service.ncm.NcmDefectDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NcmDefectDictServiceImpl implements NcmDefectDictService {

    private final NcmDefectDictMapper ncmDefectDictMapper;
    private final NcmDefectRecordMapper ncmDefectRecordMapper;

    @Override
    public List<NcmDefectDict> list() {
        List<NcmDefectDict> dicts = ncmDefectDictMapper.selectList(null);
        fillReferenceCount(dicts);
        return dicts;
    }

    @Override
    public NcmDefectDict get(String id) {
        NcmDefectDict dict = ncmDefectDictMapper.selectById(id);
        if (dict != null) fillReferenceCount(List.of(dict));
        return dict;
    }

    /** 实时统计每个字典被不良记录(defect_dict_code)引用的次数,避免维护冗余且易失真的计数。 */
    private void fillReferenceCount(List<NcmDefectDict> dicts) {
        if (dicts.isEmpty()) return;
        Map<String, Long> counts = ncmDefectRecordMapper.countByDictCode().stream()
            .collect(Collectors.toMap(
                m -> (String) m.get("code"),
                m -> ((Number) m.get("cnt")).longValue(),
                (a, b) -> a + b));
        dicts.forEach(d -> d.setReferenceCount((int) (long) counts.getOrDefault(d.getCode(), 0L)));
    }

    @Override
    @Transactional
    public NcmDefectDict create(NcmDefectDict dict) {
        if (dict.getStatus() == null) {
            dict.setStatus("启用");
        }
        if (dict.getReferenceCount() == null) {
            dict.setReferenceCount(0);
        }
        // 多租户:写入当前组织,使列表(按 org_id 过滤)能查回本人创建的字典。
        // 管理员(orgId=ROOT,非 UUID)保持 NULL,作为全局字典(管理员列表不过滤)。
        if (dict.getOrgId() == null) {
            CompanyContext.CurrentUser u = CompanyContext.get();
            if (u != null && u.orgId() != null && !"ROOT".equals(u.orgId())) {
                dict.setOrgId(u.orgId());
            }
        }
        ncmDefectDictMapper.insert(dict);
        return dict;
    }

    @Override
    public void update(NcmDefectDict dict) {
        ncmDefectDictMapper.updateById(dict);
    }

    @Override
    @Transactional
    public void delete(String id) {
        ncmDefectDictMapper.deleteById(id);
    }
}
