package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** SPC 测量值明细(隶属于一个子组,按 seq 排序) */
@Data
@TableName("ops.spc_measurement")
public class SpcMeasurement {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("subgroup_id")
    private String subgroupId;

    @TableField("subgroup_time")
    private LocalDateTime subgroupTime;

    private Integer seq;

    private BigDecimal value;
}
