package com.konli.qms.service.sqm.dto;

import com.konli.qms.domain.sqm.entity.SqmSupplier;
import lombok.Data;

/** 供应商档案详情聚合 VO:基础信息 + 各关联维度计数(避免前端全量拉取再 filter 卡顿)。 */
@Data
public class SqmSupplierOverviewVo {
    private SqmSupplier supplier;   // 基础信息(复用 get(id))
    private long certCount;         // 资质证书数
    private long auditCount;        // 审核计划数
    private long performanceCount;  // 绩效记录数
    private long abnormalCount;     // 来料异常数
    private long changeCount;       // 物料变更数
    private long lotCount;          // 来料批次数
}
