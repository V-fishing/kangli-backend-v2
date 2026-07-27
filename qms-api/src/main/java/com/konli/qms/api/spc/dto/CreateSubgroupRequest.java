package com.konli.qms.api.spc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** SPC 子组录入请求(手动采集一组测量值,产出 xbar/rangeR)。 */
@Data
public class CreateSubgroupRequest {

    private String orgId;          // 所属公司(可选,未传则由后端从登录上下文注入)

    @NotBlank
    private String paramId;        // SPC 参数 ID

    private LocalDateTime subgroupTime;

    private String shift;          // 班次

    private String woNo;           // 工单号

    private String batchNo;        // 批次号

    private List<BigDecimal> values;   // 子组测量值(n 个)
}
