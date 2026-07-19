package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 物料变更单(冻结收货,会签 + 一票否决)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_change_order")
public class SqmChangeOrder extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("change_no")
    private String changeNo;

    private String title;

    @TableField("supplier_id")
    private String supplierId;

    @TableField("part_no")
    private String partNo;

    @TableField("change_type")
    private String changeType;

    private String reason;

    private String applicant;

    @TableField("apply_date")
    private LocalDate applyDate;

    private String urgency;               // 高/中/低

    @TableField("strict_flag")
    private Boolean strictFlag;           // 变更后加严检验

    @TableField("risk_pre_mark")
    private String riskPreMark;           // 高/中/低

    private String source;                // 门户提报/主数据自动检测

    @TableField("receive_frozen")
    private Boolean receiveFrozen;        // 未关闭冻结收货

    private String status;
}
