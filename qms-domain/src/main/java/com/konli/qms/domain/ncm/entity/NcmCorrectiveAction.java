package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 纠正措施(对单条不良的快速纠正,可关联 8D/CAPA)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.ncm_corrective_action")
public class NcmCorrectiveAction extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("ca_no")
    private String caNo;

    @TableField("defect_no")
    private String defectNo;

    private String issue;

    private String owner;

    @TableField("due_date")
    private LocalDate dueDate;

    private String status;

    private Short progress;
}
