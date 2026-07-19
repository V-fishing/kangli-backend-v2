package com.konli.qms.service.ncm.dto;

import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.entity.Qms8dStageDetail;
import lombok.Data;

import java.util.List;

/** 8D 报告 + 阶段明细(详情返回) */
@Data
public class EightDVo {
    private Qms8dReport report;
    private List<Qms8dStageDetail> stages;
}
