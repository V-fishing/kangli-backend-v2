package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 加严检验跟踪(连续3批合格恢复/不合格回滚)。
 * 无审计字段。
 */
@Data
@TableName("ops.sqm_change_strict_inspect")
public class SqmChangeStrictInspect {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("strict_no")
    private String strictNo;

    @TableField("change_id")
    private String changeId;

    @TableField("lot_id")
    private String lotId;

    @TableField("inspect_type")
    private String inspectType;           // 加严

    @TableField("aql_level")
    private String aqlLevel;

    private String result;                // 待检/合格/不合格

    @TableField("inspect_date")
    private LocalDate inspectDate;

    private Integer seq;

    @TableField("total_seq")
    private Integer totalSeq;

    private Boolean restored;             // 是否已恢复
}
