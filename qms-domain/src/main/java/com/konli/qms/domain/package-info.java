/**
 * 数据访问层(qms-domain)。
 *
 * <p>按代码规范文档§2.1,每个业务模块包 {@code com.konli.qms.domain.{module}} 下含:
 * <ul>
 *   <li>{@code entity} - Entity(与表一一对应,继承 BaseEntity)</li>
 *   <li>{@code mapper} - MyBatis-Plus Mapper(由 qms-bootstrap 的 @MapperScan 扫描)</li>
 *   <li>{@code repository} - 复杂查询封装</li>
 * </ul></p>
 *
 * <p>一期模块:uop / fai / patrol / spc / nc / tool / sqa / ecm / lot / capa / glb</p>
 *
 * <p>多分公司隔离字段 {@code org_id}(公司=顶级 org),主键 UUIDv7,表位于 {@code ops.*} schema。</p>
 */
package com.konli.qms.domain;
