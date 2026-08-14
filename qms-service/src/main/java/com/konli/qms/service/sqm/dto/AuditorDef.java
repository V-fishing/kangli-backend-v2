package com.konli.qms.service.sqm.dto;

import lombok.Data;

import java.util.List;

/** 单个会签人员定义(会签配置 JSON 的元素)。 */
@Data
public class AuditorDef {
    /** ASCII 稳定标识(用于业务回传匹配),如 zhiliang / caigou / sqe。 */
    private String role;
    /** 展示用名称,如 质量主管 / 采购 / SQE;多人会签时为多姓名用、分隔。 */
    private String label;
    /** 是否具一票否决权。 */
    private boolean veto;
    /** 指定审批人 user_id(ops.sys_user.id);为空表示不绑定特定人(任何有权限者均可签)。 */
    private String userId;
    /** 多人会签:同一节点可指定多名审批人(approver_id 以逗号串存储,OR 语义任一可签)。 */
    private List<String> userIds;
}
