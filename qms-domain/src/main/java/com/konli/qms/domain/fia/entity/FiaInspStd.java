package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 检验标准库(主数据,版本化) */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.fia_insp_std")
public class FiaInspStd extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    private String code;
    private String material;
    @TableField("proc_name")
    private String procName;
    private String aql;
    @TableField("inspect_level")
    private String inspectLevel;
    @TableField("sample_plan")
    private String samplePlan;
    @TableField("ctq_text")
    private String ctqText;
    @TableField("std_version")
    private String stdVersion;      // 业务版本 v1/v2/v3(避让乐观锁 version)
    private String status;          // 草稿/生效/停用
    @TableField("prev_version_id")
    private String prevVersionId;
}
