package com.konli.qms.api.spc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 新建抽样批次任务请求:工单号/料号/工序必填。支持一次为同一工单号(产品)创建多个参数任务。 */
@Data
public class CreateSampleTaskRequest {

    private String orgId;

    @NotBlank
    private String woNo;            // 工单号(前端生成,同一批多参数任务共享)

    @NotBlank
    private String partNo;          // 料号

    private String procName;        // 工序

    private String productName;     // 产品名称(可选)

    @NotNull
    private Integer targetCount;    // 目标批次数(录满自动结案,0=不限)

    /** SPC 参数 ID 列表:勾选多个参数时,为同一 woNo 循环创建多个抽样任务。
     *  为空时回退按工序自动匹配单个 chartable 参数(兼容旧单参数调用)。 */
    private List<String> paramIds;

    /** FIA 检验标准项 ID 列表:直接由 FIA 标准库检验项派生 SPC 参数(完全分离于 spc_param 维护)。
     *  与 paramIds 互斥使用——标准库检验项尚未落 spc_param 时传此字段,后端按 fiaStdItemId 自动建/复用参数。 */
    private List<String> fiaStdItemIds;

    // ── 对齐首件表单的业务元信息(可选) ──
    private String triggerType;     // 触发类型(量产/换线/客诉…)
    private String category;        // 品类 material/semi/product
    private String supplierId;      // 供应商 UUID(物料类必填)
    private String supplierName;    // 供应商名称
    private Boolean isUrgent;       // 加急
    private String remark;          // 备注
}
