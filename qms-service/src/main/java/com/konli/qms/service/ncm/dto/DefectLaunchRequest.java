package com.konli.qms.service.ncm.dto;

import lombok.Data;

import java.util.List;

/** 不良记录发起 8D/CAPA/CA 时的指派与通知请求体。 */
@Data
public class DefectLaunchRequest {

    /** 指定负责人(系统用户 ID,8D 发起时单选必填;团队由负责人在 D1 自行组建) */
    private String ownerUserId;

    /** 被指派人(系统用户 ID 列表) */
    private List<String> assigneeUserIds;

    /** 被指派角色(角色码列表) */
    private List<String> assignRoleCodes;

    /** 通知方式(渠道名列表,如 站内弹窗/钉钉;为空默认仅站内弹窗) */
    private List<String> notifyChannels;

    /** 指派备注 */
    private String remark;
}
