package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 分析方案(保存的筛选条件,JSONB 存储过滤 JSON)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.ncm_filter_scheme")
public class NcmFilterScheme extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("scheme_name")
    private String schemeName;

    @TableField("owner_id")
    private String ownerId;

    /** JSONB -> String 映射(不加 typeHandler) */
    @TableField("filter_json")
    private String filterJson;
}
