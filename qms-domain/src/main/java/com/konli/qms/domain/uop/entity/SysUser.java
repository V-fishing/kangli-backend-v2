package com.konli.qms.domain.uop.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体(代码规范§3.3,对应 ops.sys_user)。
 * 仅映射登录所需字段;其余列(pwd_history/email/phone 等)由 DB 默认值或后续扩展。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sys_user")
public class SysUser extends BaseEntity {

    private String username;

    @TableField("password_hash")
    private String passwordHash;

    /** 归属组织(公司=顶级 org);为 null 表示跨公司管理员(dataScope=all) */
    @TableField("org_id")
    private String orgId;

    @TableField("real_name")
    private String realName;

    private String status;   // 启用/停用/锁定

    @TableField("fail_count")
    private Integer failCount;     // 连续密码错误

    @TableField(value = "lock_until", updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime lockUntil;   // 锁定至(ALWAYS:成功后写 null 重置)
}
