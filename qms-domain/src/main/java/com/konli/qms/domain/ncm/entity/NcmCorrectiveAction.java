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

    /** 责任人用户 ID,支撑按当前登录用户聚合"我的任务"(V159 加) */
    @TableField("owner_user_id")
    private String ownerUserId;

    /** 责任人姓名(由 owner 关联 sys_user 解析,非持久化字段)。 */
    @TableField(exist = false)
    private String ownerName;

    @TableField("due_date")
    private LocalDate dueDate;

    private String status;

    private Short progress;
}
