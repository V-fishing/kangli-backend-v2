package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import com.konli.qms.service.sqm.dto.SqmChangeOrderVo;

import java.util.List;

/** 物料变更单:创建/查询/提交/会签审批/关闭。sqm.change.* */
public interface SqmChangeService {

    List<SqmChangeOrder> list();

    SqmChangeOrderVo get(String id);

    SqmChangeOrder create(SqmChangeOrder order);

    /** 提交申请(待申请 -> 审批中)。 */
    void submit(String id);

    /**
     * 会签审批(质量/采购/研发并行 + 质量一票否决 + 试产串行)。
     *
     * @param id            变更单 ID
     * @param approvalRole  审批角色 quality/purchase/rd/trial
     * @param approved      是否通过
     * @param opinion       审批意见
     */
    void approve(String id, String approvalRole, boolean approved, String opinion);

    /** 关闭变更单。 */
    void close(String id);
}
