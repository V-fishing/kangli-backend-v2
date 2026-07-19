package com.konli.qms.domain.uop.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 代班委派(代码规范§2.1,对应 ops.sys_delegation) */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sys_delegation")
public class SysDelegation extends BaseEntity {

    @TableField("delegator_id")
    private String delegatorId;   // 委派人

    @TableField("delegatee_id")
    private String delegateeId;   // 被委派人

    @TableField("role_id")
    private String roleId;        // 委派角色

    @TableField("start_at")
    private LocalDateTime startAt;

    @TableField("end_at")
    private LocalDateTime endAt;

    private String status;        // 生效/已过期/已撤销
}
