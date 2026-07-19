package com.konli.qms.service.patrol.dto;

import com.konli.qms.domain.patrol.entity.PatlRoute;
import lombok.Data;

import java.util.List;

/** 巡检路线 + 点位(含检查项,详情返回) */
@Data
public class PatlRouteVo {
    private PatlRoute route;
    private List<CheckpointVo> checkpoints;
}
