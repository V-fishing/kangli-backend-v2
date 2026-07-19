package com.konli.qms.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一实体基类(代码规范文档§3.2 + §7.5 乐观锁)。
 *
 * <p>主键 UUIDv7(应用层 {@link IdType#ASSIGN_UUID});审计字段自动填充;
 * 软删除 {@code is_deleted};乐观锁 {@code version}(§7.5 要求,补齐 §3.2 缺口)。</p>
 *
 * <p>业务 Entity 继承本类后,按代码规范§3.3 自行追加 {@code org_id}(RLS 关键字段)等业务列。</p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;                    // UUIDv7

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;             // 操作人 ID

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableLogic
    private Boolean isDeleted;            // 软删除标志

    @Version
    private Integer version;              // 乐观锁(代码规范§7.5)
}
