package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.Qms8dFishbone;

import java.util.List;

/** 8D 鱼骨图 CRUD(按 d8Id 分组)。ncm.8d.* */
public interface Qms8dFishboneService {

    /** 按指定 8D 报告列出鱼骨图根因。 */
    List<Qms8dFishbone> list(String d8Id);

    /** 统计指定 8D 报告下鱼骨图原因条数(用于 D4 推进校验)。 */
    long count(String d8Id);

    Qms8dFishbone create(Qms8dFishbone fishbone);

    void update(Qms8dFishbone fishbone);

    void delete(String id);
}
