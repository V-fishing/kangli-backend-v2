/**
 * 接口层(qms-api)。
 *
 * <p>按代码规范文档§2.1,每个业务模块包 {@code com.konli.qms.api.{module}} 下含:
 * <ul>
 *   <li>{@code controller/{Module}Controller}</li>
 *   <li>{@code dto/{Module}Request|Response}</li>
 * </ul></p>
 *
 * <p>URL 含 {@code /api/v1} 前缀(代码规范§3.4;前端 vite 代理不剥离 {@code /api});响应统一用 {@code R<T>={code,msg,data}}。</p>
 */
package com.konli.qms.api;
