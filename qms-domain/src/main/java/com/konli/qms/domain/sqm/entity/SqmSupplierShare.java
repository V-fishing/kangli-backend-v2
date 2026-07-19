package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 供应商份额(按物料分配份额比例,关联等级)。
 * 无审计字段。
 */
@Data
@TableName("ops.sqm_supplier_share")
public class SqmSupplierShare {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("supplier_id")
    private String supplierId;

    @TableField("part_no")
    private String partNo;

    @TableField("share_ratio")
    private BigDecimal shareRatio;        // NUMERIC(5,2)

    @TableField("effective_date")
    private LocalDate effectiveDate;

    @TableField("change_reason")
    private String changeReason;

    @TableField("prev_ratio")
    private BigDecimal prevRatio;

    @TableField("linked_level")
    private String linkedLevel;           // CHAR(1) A/B/C/D
}
