package com.konli.qms.service.ncm.dto;

import com.konli.qms.domain.ncm.entity.QmsCapa;
import lombok.Data;

/** 来料异常单发起 CAPA 的请求体:携带 CAPA 信息(capa)与指派负责人配置(launch)。 */
@Data
public class AbnormalCapaLaunchRequest {
    /** CAPA 主体(abnormalId 指向来源异常单主键) */
    private QmsCapa capa;
    /** 指派负责人配置:ownerUserId 单选指定负责人,notifyChannels 通知渠道,remark 备注 */
    private DefectLaunchRequest launch;
}
