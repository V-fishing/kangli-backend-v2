package com.konli.qms.service.patrol.dto;

import com.konli.qms.domain.patrol.entity.PatlRecord;
import com.konli.qms.domain.patrol.entity.PatlTask;
import lombok.Data;

import java.util.List;

/** 巡检任务 + 记录(详情返回) */
@Data
public class PatlTaskVo {
    private PatlTask task;
    private List<PatlRecord> records;
}
