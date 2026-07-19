package com.konli.qms.service.spc.dto;

import com.konli.qms.domain.spc.entity.SpcControlLimit;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import lombok.Data;

import java.util.List;

/** 控制图数据(子组时间序列 + 当前激活控制限;无控制限时 limit=null,前端自适应)。 */
@Data
public class ControlChartVo {

    private List<SpcSubgroup> subgroups;

    private SpcControlLimit limit;
}
