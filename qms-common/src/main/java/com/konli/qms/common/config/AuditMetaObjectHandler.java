package com.konli.qms.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.konli.qms.common.security.CompanyContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 审计字段自动填充(补齐代码规范文档缺失的工程化配置)。
 *
 * <p>配合 BaseEntity 的 {@code @TableField(fill=...)}:插入时填 createdAt/updatedAt/createdBy/updatedBy,
 * 更新时填 updatedAt/updatedBy。createdBy/updatedBy 取当前登录用户 ID(CompanyContext),无上下文则不填(列可空)。</p>
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String operator = currentOperator();
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        if (operator != null) {
            this.strictInsertFill(metaObject, "createdBy", String.class, operator);
            this.strictInsertFill(metaObject, "updatedBy", String.class, operator);
        }
        // 自动填充多租户关键字段 orgId(RLS):实体含 orgId 且未显式赋值时,取当前登录用户所属公司。
        // 兼容各业务 Entity 自行声明 orgId(未加 @TableField(fill)) 的情况,故用非严格填充。
        if (metaObject.hasGetter("orgId") && metaObject.hasSetter("orgId")
                && metaObject.getValue("orgId") == null) {
            CompanyContext.CurrentUser u = CompanyContext.get();
            // ROOT/全量管理员无归属公司,不要写入非法 uuid(否则 org_id 列插入 500)
            if (u != null && u.orgId() != null && !"ROOT".equals(u.orgId())) {
                metaObject.setValue("orgId", u.orgId());
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        String operator = currentOperator();
        if (operator != null) {
            this.strictUpdateFill(metaObject, "updatedBy", String.class, operator);
        }
    }

    /** 当前操作人 ID(登录用户 UUID);无上下文(系统/种子)返回 null,避免往 UUID 列写非法值 */
    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? null : u.userId();
    }
}
