package com.konli.qms.service.spc;

import java.math.BigDecimal;

/**
 * SPC CPK 闸门(共用判定逻辑)。
 * 首件审批放行前硬卡(CPK 不达标不放行工单);抽样结案软告警(跌破阈值预警,不卡停产)。
 * 门槛取 SpcGlobalConfig.cpkSufficient(默认 1.33)。
 */
public interface CpkGateService {

    /**
     * 取达标门槛(CPK 阈值)。无配置则默认 1.33。
     * @param orgId 组织(用于取公司级配置,为空取全局默认)
     */
    BigDecimal sufficientThreshold(String orgId);

    /**
     * 判定是否达标。
     * @return true=CPK 达到门槛(达标/可放行);false=跌破门槛(不达标/需告警)
     */
    boolean isSufficient(String orgId, BigDecimal cpk);

    /**
     * 首件硬卡校验:达标返回 true;不达标抛 BusinessException(不放行工单)。
     * 仅用于首件(FIRST)放行前。
     */
    void assertFirstPiecePass(String orgId, BigDecimal cpk, String context);
}
