package com.konli.qms.service.spc.dto;

import lombok.Data;

/**
 * 控制图异常点标记(供前端"异常点与判异规则命中"列表 / 控制图着色)。
 * i:子组在 subgroups 列表中的下标(0-based);rule:命中规则编号(①-⑧);level:报警/预警。
 */
@Data
public class ControlChartMark {
    private Integer i;
    private String rule;
    private String level;
    private String range;
}
