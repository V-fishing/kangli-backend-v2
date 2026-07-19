package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.domain.ncm.entity.QmsCapaAction;
import com.konli.qms.domain.ncm.mapper.QmsCapaActionMapper;
import com.konli.qms.domain.ncm.mapper.QmsCapaMapper;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.ncm.dto.CapaVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NcmCapaServiceImpl implements NcmCapaService {

    private final QmsCapaMapper qmsCapaMapper;
    private final QmsCapaActionMapper qmsCapaActionMapper;

    @Override
    public List<QmsCapa> list() {
        return qmsCapaMapper.selectList(null);
    }

    @Override
    public CapaVo get(String id) {
        CapaVo vo = new CapaVo();
        vo.setCapa(qmsCapaMapper.selectById(id));
        vo.setActions(qmsCapaActionMapper.selectList(
                new LambdaQueryWrapper<QmsCapaAction>()
                        .eq(QmsCapaAction::getCapaId, id)
                        .orderByAsc(QmsCapaAction::getSeq)));
        return vo;
    }

    @Override
    @Transactional
    public QmsCapa create(QmsCapa capa) {
        capa.setCapaNo("CAPA-" + System.currentTimeMillis());
        capa.setStatus("待启动");
        if (capa.getProgress() == null) {
            capa.setProgress((short) 0);
        }
        qmsCapaMapper.insert(capa);
        return capa;
    }

    @Override
    @Transactional
    public void updateProgress(String capaId, Short progress) {
        QmsCapa capa = qmsCapaMapper.selectById(capaId);
        if (capa == null) {
            throw new BusinessException(404, "CAPA 不存在");
        }
        QmsCapa upd = new QmsCapa();
        upd.setId(capaId);
        upd.setProgress(progress);
        // progress=100 -> status="已验证"
        if (progress != null && progress == 100) {
            upd.setStatus("已验证");
        }
        qmsCapaMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void close(String capaId) {
        QmsCapa capa = qmsCapaMapper.selectById(capaId);
        if (capa == null) {
            throw new BusinessException(404, "CAPA 不存在");
        }
        QmsCapa upd = new QmsCapa();
        upd.setId(capaId);
        upd.setStatus("已关闭");
        qmsCapaMapper.updateById(upd);
    }
}
