package com.konli.qms.domain.patrol.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 巡检点位(路线下有序点位)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.patl_checkpoint")
public class PatlCheckpoint extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("route_id")
    private String routeId;

    private Short seq;                  // 顺序

    @TableField("point_name")
    private String pointName;           // 点位名称(如:注塑机A区/焊接工位3)

    private String location;            // 位置描述

    @TableField("need_photo")
    private Boolean needPhoto;          // 是否必拍照片
}
