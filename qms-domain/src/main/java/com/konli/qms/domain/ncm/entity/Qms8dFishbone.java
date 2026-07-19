package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 8D 鱼骨图(5M1E 根因,子表,无审计字段;按 sort_order 排序)。 */
@Data
@TableName("ops.qms_8d_fishbone")
public class Qms8dFishbone {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("d8_id")
    private String d8Id;

    private String problem;

    /** 5M1E 类别:人/机/料/法/环/测 */
    private String category;

    @TableField("cause_text")
    private String causeText;

    @TableField("sort_order")
    private Short sortOrder;
}
