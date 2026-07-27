package com.konli.qms.domain.sqm.dto;

import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;
import com.konli.qms.domain.sqm.entity.SqmAbnormalMeasure;
import com.konli.qms.domain.sqm.entity.SqmAbnormalBatchVerify;
import lombok.Data;

import java.util.List;

/** 异常整改持久化请求(V21):异常主体 + 整改措施 + 三批验证 */
@Data
public class AbnormalRectificationRequest {
    private SqmIncomingAbnormal abnormal;
    private List<SqmAbnormalMeasure> measures;
    private List<SqmAbnormalBatchVerify> batchVerifies;
}
