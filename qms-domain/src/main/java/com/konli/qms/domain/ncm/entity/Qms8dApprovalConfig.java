package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 8D 阶段审核配置(每个公司+每个阶段一条;UNIQUE(org_id, stage_code))。
 * 配置“该 D 阶段是否需要审核人签名才能进入下一阶段”以及指定签批人。
 * org_id 为 'ROOT' 表示跨公司管理员(无归属组织时)的配置。
 */
@Data
@TableName("ops.qms_8d_approval_config")
public class Qms8dApprovalConfig implements Serializable {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("stage_code")
    private String stageCode;

    @TableField("need_approval")
    private Boolean needApproval;

    @TableField("signer")
    private String signer;

    @TableField("sort_order")
    private Integer sortOrder;
}
