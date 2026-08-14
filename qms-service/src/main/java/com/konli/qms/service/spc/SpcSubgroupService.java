package com.konli.qms.service.spc;

import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.service.spc.dto.ControlChartVo;
import com.konli.qms.service.spc.dto.SpcHistogramVo;
import com.konli.qms.service.spc.dto.SpcSubgroupVo;

import java.math.BigDecimal;
import java.util.List;

/** SPC 子组录入(手动采集 n 个测量值,产出 xbar/rangeR)。spc.subgroup.* */
public interface SpcSubgroupService {

    List<SpcSubgroup> list();

    SpcSubgroupVo get(String id);

    SpcSubgroup create(SpcSubgroup subgroup, List<BigDecimal> values);

    /** 独立事务创建子组(联动场景:FIA->SPC 失败不回滚调用方主事务)。 */
    SpcSubgroup createInNewTx(SpcSubgroup subgroup, List<BigDecimal> values);

    /** 控制图数据:按时间正序的子组序列 + 当前激活控制限(无则 limit=null)。stage 可选 FIRST/ROUTINE/ALL;sampleTaskId 可选按抽样任务过滤。 */
    ControlChartVo getControlChart(String paramId, String startTime, String endTime, String stage, String sampleTaskId);

    /** 过程能力直方图:基于参数全量子组的均值(xbar)分箱聚合。stage 可选 FIRST/ROUTINE/ALL;sampleTaskId 可选按抽样任务过滤。 */
    SpcHistogramVo getHistogram(String paramId, String stage, String sampleTaskId);
}
