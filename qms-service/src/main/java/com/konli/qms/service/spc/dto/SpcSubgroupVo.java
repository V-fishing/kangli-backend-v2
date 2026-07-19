package com.konli.qms.service.spc.dto;

import com.konli.qms.domain.spc.entity.SpcMeasurement;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import lombok.Data;

import java.util.List;

/** 子组 + 测量值明细(详情返回) */
@Data
public class SpcSubgroupVo {
    private SpcSubgroup subgroup;
    private List<SpcMeasurement> measurements;
}
