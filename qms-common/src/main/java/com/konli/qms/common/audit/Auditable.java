package com.konli.qms.common.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计注解(代码规范文档§2.1/§3.4)。标注于 Controller 写操作,由 {@link AuditAspect} 拦截记录。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** 模块,如 "FIA" */
    String module();

    /** 动作,如 "CREATE" / "APPROVE" */
    String action();
}
