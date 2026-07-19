package com.konli.qms.common.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 审计切面(代码规范文档§2.1)。拦截 {@link Auditable} 标注的写操作,记录模块/动作/操作人/耗时/结果。
 *
 * <p>当前 stage-4 仅落日志;后续 stage 接入 SecurityContext 取操作人 + 异步写入独立审计库。</p>
 */
@Slf4j
@Aspect
@Component
public class AuditAspect {

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        String operator = currentOperator();
        long start = System.currentTimeMillis();
        log.info("[AUDIT] module={}, action={}, method={}, operator={}",
                auditable.module(), auditable.action(), pjp.getSignature().toShortString(), operator);
        try {
            Object result = pjp.proceed();
            log.info("[AUDIT] module={}, action={}, cost={}ms, result=success",
                    auditable.module(), auditable.action(), System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            log.warn("[AUDIT] module={}, action={}, cost={}ms, result=fail, error={}",
                    auditable.module(), auditable.action(), System.currentTimeMillis() - start, e.getMessage());
            throw e;
        }
    }

    /** TODO stage:接入 Spring Security 取当前用户 ID */
    private String currentOperator() {
        return "system";
    }
}
