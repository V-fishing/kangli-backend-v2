package com.konli.qms.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 审计切面 — 拦截 @Auditable 标注的方法并持久化到 ops.sys_audit_log。
 * 实际落库委托给 {@link AuditLogRecorder}(单点写),操作人从 CompanyContext 取(由 JwtAuthenticationFilter 注入)。
 * 审计落库失败不阻断业务。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRecorder recorder;
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        String module   = auditable.module();
        String action   = auditable.action();
        String method   = pjp.getSignature().toShortString();
        long start = System.currentTimeMillis();

        Object result = null;
        String status = null;
        String error = null;
        try {
            result = pjp.proceed();
            status = "SUCCESS";
        } catch (Throwable e) {
            status = "FAIL";
            error = e.getMessage();
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - start;
            String recordId = "SUCCESS".equals(status) ? eval(auditable.recordExpr(), pjp, result) : null;
            String detail   = "SUCCESS".equals(status) ? eval(auditable.detailExpr(), pjp, result) : null;
            String recordNo = "SUCCESS".equals(status) ? eval(auditable.recordNoExpr(), pjp, result) : null;
            recorder.record(module, action, method, recordId, recordNo, detail, status, error, cost);
        }
        return result;
    }

    private String eval(String expr, ProceedingJoinPoint pjp, Object result) {
        if (expr == null || expr.isBlank()) return null;
        try {
            Method method = ((MethodSignature) pjp.getSignature()).getMethod();
            StandardEvaluationContext ctx = new StandardEvaluationContext();
            String[] names = DISCOVERER.getParameterNames(method);
            Object[] args  = pjp.getArgs();
            if (names != null) {
                for (int i = 0; i < names.length && i < args.length; i++) ctx.setVariable(names[i], args[i]);
            }
            for (int i = 0; i < args.length; i++) {
                ctx.setVariable("p" + i, args[i]);
                ctx.setVariable("a" + i, args[i]);
            }
            ctx.setVariable("result", result);
            Object val = PARSER.parseExpression(expr).getValue(ctx);
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
