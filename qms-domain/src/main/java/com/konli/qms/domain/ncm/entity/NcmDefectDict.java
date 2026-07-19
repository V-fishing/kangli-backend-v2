package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 不良字典(标准不良项主数据,被缺陷记录引用)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.ncm_defect_dict")
public class NcmDefectDict extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    private String code;

    private String name;

    private String category;

    private String level;

    private String status;

    @TableField("reference_count")
    private Integer referenceCount;
}
