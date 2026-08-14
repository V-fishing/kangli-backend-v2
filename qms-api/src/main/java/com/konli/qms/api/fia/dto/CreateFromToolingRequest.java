package com.konli.qms.api.fia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 工装首件检验任务创建请求(人工入口)。
 * 由前端「新建检验任务 / 工装首件」分支或工装台账「创建首件」按钮调用。
 * 后端按工装档案 product_code + proc_name 自动匹配 FIA 检验标准，无需前端传 stdId。
 */
@Data
public class CreateFromToolingRequest {

    @NotBlank
    private String orgId;          // 所属公司

    @NotBlank
    private String toolId;         // 关联工装 ID(必填,写入 fia_task.tool_id)

    /** 触发类型(对应 fia_trigger_type.name),可空,默认"工装维修后" */
    private String triggerType;

    /** 生产批次号(必填,实现验证数据与生产批次绑定,不合格可回溯) */
    @NotBlank
    private String batchNo;

    private Boolean isUrgent;
    private String remark;
}
