package com.konli.qms.common.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计注解(代码规范文档§2.1/§3.4)。标注于 Controller 写操作方法上,由 {@link AuditAspect} 拦截并持久化到 sys_audit_log。
 *
 * <p>用法:
 * <pre>{@code
 *   @Auditable(module = "FIA", action = "CREATE", recordExpr = "#result.id")
 *   public R<FiaTask> create(...) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** 模块,如 "FIA" / "SPC" / "NCM" */
    String module();

    /** 动作,如 "CREATE" / "UPDATE" / "APPROVE" / "CLOSE" / "DELETE" */
    String action();

    /** SpEL 表达式取被操作记录 ID,如 "#result.id" / "#id" / "#req.taskId"。为空则不记 recordId。 */
    String recordExpr() default "";

    /** 操作摘要,支持 SpEL,如 "'创建任务' + #req.woNo"。为空则不记。 */
    String detailExpr() default "";
}
