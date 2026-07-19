package com.konli.qms.service.fia.dto;

import com.konli.qms.domain.fia.entity.FiaInspItem;
import com.konli.qms.domain.fia.entity.FiaTask;
import lombok.Data;

import java.util.List;

/** 检验任务 + 检验项(详情返回) */
@Data
public class FiaTaskVo {
    private FiaTask task;
    private List<FiaInspItem> items;
}
