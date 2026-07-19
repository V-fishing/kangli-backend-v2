package com.konli.qms.service.fia.dto;

import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import lombok.Data;

import java.util.List;

/** 检验标准 + 检测项(详情返回) */
@Data
public class InspStdVo {
    private FiaInspStd std;
    private List<FiaInspStdItem> items;
}
