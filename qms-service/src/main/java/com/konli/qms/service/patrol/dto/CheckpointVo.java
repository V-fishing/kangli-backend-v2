package com.konli.qms.service.patrol.dto;

import com.konli.qms.domain.patrol.entity.PatlCheckItem;
import com.konli.qms.domain.patrol.entity.PatlCheckpoint;
import lombok.Data;

import java.util.List;

/** 巡检点位 + 检查项(详情/创建入参) */
@Data
public class CheckpointVo {
    private PatlCheckpoint checkpoint;
    private List<PatlCheckItem> items;
}
