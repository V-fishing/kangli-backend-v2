package com.konli.qms.api.spc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** SPC 子组录入请求(手动采集一组测量值,产出 xbar/rangeR)。 */
@Data
public class CreateSubgroupRequest {

    private String orgId;          // 所属公司(可选,未传则由后端从登录上下文注入)

    @NotBlank
    private String paramId;        // SPC 参数 ID

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime subgroupTime;

    /** @deprecated 班次字段已废弃,保留兼容历史请求 */
    @Deprecated
    private String shift;

    private String woNo;           // 工单号

    private String batchNo;        // 批次号

    private String taskId;         // 关联FIA任务ID(从 FIA 任务跳转采集时携带)

    private String sampleTaskId;   // 关联抽样批次任务ID(从抽样任务跳转采集时携带)

    /** SPC 数据阶段: FIRST=首件能力验证(点状); ROUTINE=量产过程监控(线状)。默认 ROUTINE。 */
    private String stage;

    private String productCode;    // 产品料号(从 FIA 任务带入)

    private List<BigDecimal> values;   // 子组测量值(n 个, 计量型 Xbar/R/S/I/MR 必填)

    /** 计数型 P/NP 图:子组不合格数(非计数型为 null) */
    private Integer nonconforming;

    /** 计数型 P/NP 图:子组检验总数(样本量 n, 非计数型为 null) */
    private Integer inspectN;

    /** 计数型 C/U 图:子组缺陷数(非计数型为 null) */
    private Integer defectCount;
}
