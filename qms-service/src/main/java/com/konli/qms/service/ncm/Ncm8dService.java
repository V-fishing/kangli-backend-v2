package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.service.ncm.dto.EightDVo;

import java.util.List;

/** 8D 报告:创建/查询/阶段推进(D1->D8)。ncm.8d.* */
public interface Ncm8dService {

    List<Qms8dReport> list();

    EightDVo get(String id);

    Qms8dReport create(Qms8dReport report);

    /** 推进到指定阶段(必须按 D1->D2->...->D8 顺序)。 */
    void advanceStage(String d8Id, String stageCode, String content, String owner);
}
