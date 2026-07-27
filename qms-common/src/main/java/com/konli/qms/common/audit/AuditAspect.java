package com.konli.qms.common.audit;

import com.konli.qms.common.security.CompanyContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 审计切面 — 拦截 @Auditable 标注的方法并持久化到 ops.sys_audit_log。
 * 操作人从 CompanyContext 取(由 JwtAuthenticationFilter 注入)。
 * 审计落库失败不阻断业务。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final JdbcTemplate jdbc;
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        String module   = auditable.module();
        String action   = auditable.action();
        String method   = pjp.getSignature().toShortString();
        String operator = currentOperator();
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
            persist(module, action, method, operator, recordId, detail, status, error, cost);
        }
        return result;
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u != null ? u.username() : "anonymous";
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

    private void persist(String module, String action, String method, String operator,
                         String recordId, String detail, String status, String error, long cost) {
        try {
            jdbc.update(
                "INSERT INTO ops.sys_audit_log (module, action, method, operator_name, "
                + "record_id, detail, status, error, cost_ms, created_at) "
                + "VALUES (?,?,?,?,?::uuid,?,?,?,?,?)",
                module, action, method, operator, recordId, detail, status, error, cost, LocalDateTime.now());
        } catch (Exception ex) {
            log.warn("[AUDIT] 落库失败(不阻断业务): {}", ex.getMessage());
        }
        log.info("[AUDIT] {} | {} | {} | {}ms | {}", module, action, status, cost, operator);
    }
}
