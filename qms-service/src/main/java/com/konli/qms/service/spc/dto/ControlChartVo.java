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

    /** 异常点标记(前端"异常点与判异规则命中"列表 / 控制图着色);无异常时为 null/空。 */
    private List<ControlChartMark> marks;

    /** 计数型控制图序列(P/NP/C/U),每种图类型一个 CountSeries;计量型图不填充(用 subgroups)。 */
    private List<CountSeries> countSeries;
}
