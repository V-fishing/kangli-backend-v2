package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import com.konli.qms.domain.sqm.entity.SqmChangeStrictInspect;
import com.konli.qms.domain.sqm.mapper.SqmChangeOrderMapper;
import com.konli.qms.domain.sqm.mapper.SqmChangeStrictInspectMapper;
import com.konli.qms.service.sqm.SqmChangeStrictInspectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SqmChangeStrictInspectServiceImpl implements SqmChangeStrictInspectService {

    private final SqmChangeStrictInspectMapper sqmChangeStrictInspectMapper;
    private final SqmChangeOrderMapper sqmChangeOrderMapper;

    @Override
    public List<SqmChangeStrictInspect> list(String changeId) {
        LambdaQueryWrapper<SqmChangeStrictInspect> w = new LambdaQueryWrapper<>();
        if (changeId != null && !changeId.isBlank()) {
            w.eq(SqmChangeStrictInspect::getChangeId, changeId);
        }
        w.orderByAsc(SqmChangeStrictInspect::getSeq);
        return sqmChangeStrictInspectMapper.selectList(w);
    }

    @Override
    public SqmChangeStrictInspect get(String id) {
        return sqmChangeStrictInspectMapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmChangeStrictInspect create(SqmChangeStrictInspect inspect) {
        inspect.setStrictNo("SI-" + System.currentTimeMillis());
        if (inspect.getResult() == null) {
            inspect.setResult("待检");
        }
        if (inspect.getRestored() == null) {
            inspect.setRestored(false);
        }
        // 兜底填充 org_id(表 NOT NULL):手动新增批次时前端未传,从变更单取
        if (inspect.getOrgId() == null && inspect.getChangeId() != null) {
            SqmChangeOrder order = sqmChangeOrderMapper.selectById(inspect.getChangeId());
            if (order != null) {
                inspect.setOrgId(order.getOrgId());
            }
        }
        sqmChangeStrictInspectMapper.insert(inspect);
        return inspect;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SqmChangeStrictInspect createInNewTx(SqmChangeStrictInspect inspect) {
        return this.create(inspect);
    }

    @Override
    @Transactional
    public void restore(String id) {
        SqmChangeStrictInspect inspect = sqmChangeStrictInspectMapper.selectById(id);
        if (inspect == null) {
            throw new BusinessException(404, "加严检验记录不存在");
        }
        SqmChangeStrictInspect upd = new SqmChangeStrictInspect();
        upd.setId(id);
        upd.setRestored(true);
        sqmChangeStrictInspectMapper.updateById(upd);
    }
}
