package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.config.StringArrayTypeHandler;
import lombok.Data;

/**
 * FIA 签名配置(每公司一行,代码规范§2.1 + 字典 E4/E5/E6)。
 * sign_methods: {password,handwriting,ca};sign_nodes: 两级/三级;sign_granularity: 整单/逐项。
 */
@Data
@TableName(value = "ops.fia_sign_config", autoResultMap = true)
public class FiaSignConfig {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField(value = "sign_methods", typeHandler = StringArrayTypeHandler.class)
    private String[] signMethods;

    @TableField("sign_nodes")
    private String signNodes;

    @TableField("sign_granularity")
    private String signGranularity;

    @TableField("lock_after_fail")
    private Integer lockAfterFail;

    @TableField("lock_minutes")
    private Integer lockMinutes;
}
