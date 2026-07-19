package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalTime;

/** 定时日报配置(通用;无审计字段)。push_time 为 TIME 类型 -> LocalTime。 */
@Data
@TableName("ops.ncm_daily_report_config")
public class NcmDailyReportConfig {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 可空(全局配置时 org_id 为 null) */
    @TableField("org_id")
    private String orgId;

    /** TIME 类型 -> java.time.LocalTime */
    @TableField("push_time")
    private LocalTime pushTime;

    /** JSONB -> String 映射(不加 typeHandler) */
    @TableField("receivers")
    private String receivers;

    private Boolean enabled;
}
