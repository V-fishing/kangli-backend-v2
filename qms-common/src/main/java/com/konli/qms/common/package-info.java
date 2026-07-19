/**
 * 公共模块(qms-common):跨层基础设施。
 *
 * <p>按代码规范文档§2.1,含(stage-4 基础设施层落地):
 * <ul>
 *   <li>{@code base} - BaseEntity 统一实体基类(id/createdAt/updatedAt/createdBy/updatedBy/isDeleted)</li>
 *   <li>{@code audit} - Auditable 注解 + AuditAspect 审计切面</li>
 *   <li>{@code exception} - GlobalExceptionHandler 全局异常 + BusinessException</li>
 *   <li>{@code api} - R&lt;T&gt; 统一响应包装({@code code/msg/data},code=0 成功)</li>
 *   <li>{@code util} - 工具类</li>
 * </ul></p>
 */
package com.konli.qms.common;
