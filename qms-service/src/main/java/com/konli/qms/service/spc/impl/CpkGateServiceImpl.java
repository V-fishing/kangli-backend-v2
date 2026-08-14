package com.konli.qms.service.spc.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcGlobalConfig;
import com.konli.qms.service.spc.CpkGateService;
import com.konli.qms.service.spc.SpcGlobalConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * CPK 闸门实现:门槛取 SpcGlobalConfig.cpkSufficient(默认 1.33)。
 * 首件硬卡 / 抽样软告警共用判定。
 */
@Service
@RequiredArgsConstructor
public class CpkGateServiceImpl implements CpkGateService {

    private final SpcGlobalConfigService spcGlobalConfigService;

    private static final BigDecimal DEFAULT_SUFFICIENT = new BigDecimal("1.33");

    @Override
    public BigDecimal sufficientThreshold(String orgId) {
        try {
            SpcGlobalConfig c = spcGlobalConfigService.get(orgId);
            if (c != null && c.getCpkSufficient() != null) {
                return c.getCpkSufficient();
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_SUFFICIENT;
    }

    @Override
    public boolean isSufficient(String orgId, BigDecimal cpk) {
        if (cpk == null) {
            return false;
        }
        return cpk.compareTo(sufficientThreshold(orgId)) >= 0;
    }

    @Override
    public void assertFirstPiecePass(String orgId, BigDecimal cpk, String context) {
        if (!isSufficient(orgId, cpk)) {
            BigDecimal threshold = sufficientThreshold(orgId);
            throw new BusinessException(409,
                    String.format("首件 CPK=%.2f 未达放行门槛(%.2f),工单不可放行。%s",
                            cpk == null ? BigDecimal.ZERO : cpk, threshold,
                            context == null ? "" : context));
        }
    }
}
