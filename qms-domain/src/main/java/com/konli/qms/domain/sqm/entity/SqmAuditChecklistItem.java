package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核检查项(现场审核打分条目),对应 ops.sqm_audit_checklist_item。
 * 首次保存时由执行服务惰性创建归属的审核记录(record_id)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_audit_checklist_item")
public class SqmAuditChecklistItem extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("record_id")
    private String recordId;

    private Integer seq;

    private String clause;

    @TableField("item_name")
    private String itemName;

    private String result;        // 符合/不符合/观察项

    private String evidence;

    @TableField("nc_id")
    private String ncId;          // 关联的不符合项(若本项判为不符合)
}
