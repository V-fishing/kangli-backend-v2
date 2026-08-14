package com.konli.qms.service.ncm.dto;

import com.konli.qms.domain.ncm.entity.Qms8dReport;
import lombok.Data;

/** 来料异常单发起 8D 的请求体:携带异常单信息(report)与指派负责人配置(launch)。 */
@Data
public class Abnormal8dLaunchRequest {
    /** 8D 报告主体(异常单号由后端回填,这里传 sourceRefId=异常单主键) */
    private Qms8dReport report;
    /** 指派负责人配置:ownerUserId 单选指定负责人,notifyChannels 通知渠道,remark 备注 */
    private DefectLaunchRequest launch;
}
