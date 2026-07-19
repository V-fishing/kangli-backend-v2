package com.konli.qms.service.patrol;

import com.konli.qms.domain.patrol.entity.PatlRoute;
import com.konli.qms.service.patrol.dto.CheckpointVo;
import com.konli.qms.service.patrol.dto.PatlRouteVo;

import java.util.List;

/** 巡检路线:路线+点位+检查项 配置。patl.route.list / patl.route.create */
public interface PatlRouteService {

    List<PatlRoute> list();

    /** 详情:路线 + 点位(每个点位含检查项,按 seq 排序)。 */
    PatlRouteVo get(String id);

    /** 创建路线(含点位与检查项)。 */
    PatlRoute create(PatlRoute route, List<CheckpointVo> checkpoints);

    void update(PatlRoute route);

    /** 级联删除:路线 + 点位 + 检查项。 */
    void delete(String id);
}
