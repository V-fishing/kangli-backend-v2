package com.konli.qms.domain.fia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

/** 标准检测项模板(子表,无审计字段) */
@Data
@TableName("ops.fia_insp_std_item")
public class FiaInspStdItem {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("std_id")
    private String stdId;

    private Integer seq;

    @TableField("item_name")
    private String itemName;

    @TableField("is_ctq")
    private Boolean isCtq;

    @TableField("std_value")
    private String stdValue;

    private String tolerance;

    private String unit;

    @TableField("value_type")
    private String valueType;

    /** 推荐控制图类型集合(基础图码逗号分隔,如 'Xbar,R');数值默认 Xbar,R,枚举/文本默认 P。可为历史组合码,读取侧经 normalizeToBasic 兼容。 */
    @TableField("chart_types")
    private String chartTypes;

    @TableField("enum_values")
    private String enumValues;

    @TableField("pass_values")
    private String passValues;        // 合格值(逗号分隔);枚举型实测命中即判合格

    @TableField("upper_limit")
    private BigDecimal upperLimit;
    @TableField("lower_limit")
    private BigDecimal lowerLimit;
    @TableField("item_type")
    private String itemType;

    /** 软删标志(逻辑删,与 update 接口的明细替换策略一致;物理删会破坏 spc_param.fia_std_item_id 引用) */
    @TableLogic
    @TableField("is_deleted")
    private Boolean isDeleted;
}
