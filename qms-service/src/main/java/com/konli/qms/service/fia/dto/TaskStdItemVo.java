package com.konli.qms.service.fia.dto;

import lombok.Data;

/** FIA 任务关联的标准项 VO(供 SPC 采集页查询) */
@Data
public class TaskStdItemVo {
    /** 检验项ID(fia_insp_item.id) */
    private String itemId;
    /** 标准项ID(fia_insp_std_item.id) */
    private String stdItemId;
    /** 检验项名称 */
    private String itemName;
    /** 标准值 */
    private String stdValue;
    /** 公差 */
    private String tolerance;
    /** 单位 */
    private String unit;
    /** 是否CTQ */
    private Boolean isCtq;
    /** 关联的 SPC 参数ID(已生成时) */
    private String spcParamId;
    /** 关联的 SPC 参数名称 */
    private String spcParamName;
}
