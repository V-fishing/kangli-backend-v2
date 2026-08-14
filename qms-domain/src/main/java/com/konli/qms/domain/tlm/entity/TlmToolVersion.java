package com.konli.qms.domain.tlm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 工装版本变更履历: 设计变更/升级记录(仅留痕可追溯, 版本号后端自动递增)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.tlm_tool_version")
public class TlmToolVersion extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("tool_id")
    private String toolId;

    @TableField("version_no")
    private String versionNo;     // V1, V2, V3 ... (后端自动递增)

    @TableField("change_type")
    private String changeType;    // DESIGN 设计变更 / UPGRADE 升级 / OTHER 其他

    @TableField("change_desc")
    private String changeDesc;

    @TableField("changed_by")
    private String changedBy;     // 变更人(姓名/工号)

    @TableField("changed_at")
    private LocalDate changedAt;  // 变更日期
}
