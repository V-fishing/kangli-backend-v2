package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 拦截配置(每公司一行,无审计字段) */
@Data
@TableName("ops.fia_intercept_config")
public class FiaInterceptConfig {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;                    // 可空(全局)

    @TableField("intercept_mode")
    private String interceptMode;            // 硬阻断/软提示

    @TableField("multi_trigger_mode")
    private String multiTriggerMode;         // 合并一张校验单/各开一张

    @TableField("sla_hours")
    private BigDecimal slaHours;

    @TableField("escalate_fail_count")
    private Integer escalateFailCount;
}
