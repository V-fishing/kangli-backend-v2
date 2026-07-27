package com.konli.qms.service.sqm.dto;

import lombok.Data;

/** 单个会签人员定义(会签配置 JSON 的元素)。 */
@Data
public class AuditorDef {
    /** ASCII 稳定标识(用于业务回传匹配),如 zhiliang / caigou / sqe。 */
    private String role;
    /** 展示用名称,如 质量主管 / 采购 / SQE。 */
    private String label;
    /** 是否具一票否决权。 */
    private boolean veto;
}
