package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.service.ncm.dto.CapaVo;

import java.util.List;

/** CAPA:创建/查询/进度更新/关闭。ncm.capa.* */
public interface NcmCapaService {

    List<QmsCapa> list();

    CapaVo get(String id);

    QmsCapa create(QmsCapa capa);

    /** 更新进度,progress=100 时自动置为已验证。 */
    void updateProgress(String capaId, Short progress);

    /** 关闭 CAPA。 */
    void close(String capaId);
}
