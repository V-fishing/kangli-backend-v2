package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.service.spc.dto.ControlChartVo;
import com.konli.qms.service.spc.dto.SpcSubgroupVo;

import java.math.BigDecimal;
import java.util.List;

/** SPC 子组录入(手动采集 n 个测量值,产出 xbar/rangeR)。spc.subgroup.* */
public interface SpcSubgroupService {

    List<SpcSubgroup> list();

    SpcSubgroupVo get(String id);

    SpcSubgroup create(SpcSubgroup subgroup, List<BigDecimal> values);

    /** 控制图数据:按时间正序的子组序列 + 当前激活控制限(无则 limit=null)。 */
    ControlChartVo getControlChart(String paramId, String startTime, String endTime);
}
