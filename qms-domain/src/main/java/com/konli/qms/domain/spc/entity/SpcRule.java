package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** SPC 判异规则定义(如 Western Electric 8 条规则,org_id 可空表示全局规则) */
@Data
@TableName("ops.spc_rule")
public class SpcRule {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("rule_code")
    private String ruleCode;

    @TableField("rule_name")
    private String ruleName;

    private String level;

    @TableField("is_enabled")
    private Boolean isEnabled;

    @TableField("sort_no")
    private Integer sortNo;
}
